package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.CrearFacturaRequest;
import com.ecomarket.pedidos.dto.CrearVentaRequest;
import com.ecomarket.pedidos.dto.FacturaResponse;
import com.ecomarket.pedidos.dto.VentaResponse;
import com.ecomarket.pedidos.model.Factura;
import com.ecomarket.pedidos.model.Venta;
import com.ecomarket.pedidos.security.IdorValidator;
import com.ecomarket.pedidos.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Ventas y Facturacion", description = "Venta presencial, gestion de ventas y facturacion electronica")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @Operation(summary = "Registrar una venta presencial",
            description = "Registra una venta directa en tienda con uno o mas items.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Venta registrada",
                    content = @Content(schema = @Schema(implementation = VentaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Items invalidos o vacios", content = @Content)
    })
    @PostMapping("/presencial")
    public ResponseEntity<VentaResponse> registrarVentaPresencial(
            @Valid @RequestBody CrearVentaRequest request,
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol,
            @RequestHeader(value = "X-Id-Usuario", required = false) String idUsuario) {
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            IdorValidator.verificarAccesoEntidadCliente(request.getIdCliente(), rol, idUsuario);
        }
        Venta venta = ventaService.registrarVentaPresencial(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.toResponse(venta));
    }

    @Operation(summary = "Listar todas las ventas")
    @ApiResponse(responseCode = "200", description = "Listado de ventas",
            content = @Content(schema = @Schema(implementation = VentaResponse.class)))
    @GetMapping
    public ResponseEntity<List<VentaResponse>> listarVentas(
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol) {
        IdorValidator.verificarNoEsCliente(rol);
        List<VentaResponse> ventas = ventaService.listarVentas()
                .stream()
                .map(ventaService::toResponse)
                .toList();
        return ResponseEntity.ok(ventas);
    }

    @Operation(summary = "Obtener detalle de una venta por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venta encontrada",
                    content = @Content(schema = @Schema(implementation = VentaResponse.class))),
            @ApiResponse(responseCode = "400", description = "ID invalido", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<VentaResponse> obtenerVenta(
            @Parameter(description = "ID de la venta", example = "1", required = true) @PathVariable Long id,
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol,
            @RequestHeader(value = "X-Id-Usuario", required = false) String idUsuario) {
        Venta venta = ventaService.obtenerVenta(id);
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            IdorValidator.verificarAccesoEntidadCliente(venta.getIdCliente(), rol, idUsuario);
        }
        return ResponseEntity.ok(ventaService.toResponse(venta));
    }

    @Operation(summary = "Actualizar una venta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venta actualizada",
                    content = @Content(schema = @Schema(implementation = VentaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Venta o producto no encontrado", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<VentaResponse> actualizarVenta(
            @Parameter(description = "ID de la venta", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody CrearVentaRequest request,
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol,
            @RequestHeader(value = "X-Id-Usuario", required = false) String idUsuario) {
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            Venta existente = ventaService.obtenerVenta(id);
            IdorValidator.verificarAccesoEntidadCliente(existente.getIdCliente(), rol, idUsuario);
        }
        Venta venta = ventaService.actualizarVenta(id, request);
        return ResponseEntity.ok(ventaService.toResponse(venta));
    }

    @Operation(summary = "Eliminar una venta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Venta eliminada", content = @Content),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVenta(
            @Parameter(description = "ID de la venta", example = "1", required = true) @PathVariable Long id,
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol) {
        IdorValidator.verificarNoEsCliente(rol);
        ventaService.eliminarVenta(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Generar factura para una venta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Factura generada",
                    content = @Content(schema = @Schema(implementation = FacturaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Venta ya tiene factura", content = @Content)
    })
    @PostMapping("/{id}/factura")
    public ResponseEntity<FacturaResponse> generarFactura(
            @Parameter(description = "ID de la venta", example = "1", required = true) @PathVariable Long id,
            @RequestBody(required = false) CrearFacturaRequest request,
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol,
            @RequestHeader(value = "X-Id-Usuario", required = false) String idUsuario) {
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            Venta venta = ventaService.obtenerVenta(id);
            IdorValidator.verificarAccesoEntidadCliente(venta.getIdCliente(), rol, idUsuario);
        }
        CrearFacturaRequest req = request != null ? request : new CrearFacturaRequest();
        Factura factura = ventaService.generarFactura(id, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.toResponse(factura));
    }

    @Operation(summary = "Obtener factura por ID de factura")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factura encontrada",
                    content = @Content(schema = @Schema(implementation = FacturaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Factura no encontrada", content = @Content)
    })
    @GetMapping("/facturas/{idFactura}")
    public ResponseEntity<FacturaResponse> obtenerFactura(
            @Parameter(description = "ID de la factura", example = "1", required = true) @PathVariable Long idFactura,
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol,
            @RequestHeader(value = "X-Id-Usuario", required = false) String idUsuario) {
        Factura factura = ventaService.obtenerFactura(idFactura);
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            IdorValidator.verificarAccesoEntidadCliente(factura.getIdCliente(), rol, idUsuario);
        }
        return ResponseEntity.ok(ventaService.toResponse(factura));
    }
}