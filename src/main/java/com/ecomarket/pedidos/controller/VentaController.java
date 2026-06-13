package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.CrearFacturaRequest;
import com.ecomarket.pedidos.dto.CrearVentaRequest;
import com.ecomarket.pedidos.dto.FacturaResponse;
import com.ecomarket.pedidos.dto.VentaResponse;
import com.ecomarket.pedidos.model.Factura;
import com.ecomarket.pedidos.model.Venta;
import com.ecomarket.pedidos.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

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
    public ResponseEntity<EntityModel<VentaResponse>> registrarVentaPresencial(
            @Valid @RequestBody CrearVentaRequest request) {
        Venta venta = ventaService.registrarVentaPresencial(request);
        VentaResponse r = ventaService.toResponse(venta);
        EntityModel<VentaResponse> model = EntityModel.of(r);
        model.add(linkTo(methodOn(VentaController.class)
                .obtenerVenta(venta.getIdVenta())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Listar todas las ventas")
    @ApiResponse(responseCode = "200", description = "Listado de ventas",
            content = @Content(schema = @Schema(implementation = VentaResponse.class)))
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<VentaResponse>>> listarVentas() {
        List<EntityModel<VentaResponse>> ventas = ventaService.listarVentas()
                .stream()
                .map(v -> {
                    VentaResponse r = ventaService.toResponse(v);
                    EntityModel<VentaResponse> model = EntityModel.of(r);
                    model.add(linkTo(methodOn(VentaController.class)
                            .obtenerVenta(v.getIdVenta())).withSelfRel());
                    return model;
                })
                .collect(Collectors.toList());
        CollectionModel<EntityModel<VentaResponse>> collection = CollectionModel.of(ventas);
        collection.add(linkTo(methodOn(VentaController.class).listarVentas()).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Obtener una venta por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venta encontrada",
                    content = @Content(schema = @Schema(implementation = VentaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content)
    })
    @GetMapping("/{idVenta}")
    public ResponseEntity<EntityModel<VentaResponse>> obtenerVenta(
            @Parameter(description = "ID de la venta", example = "1", required = true) @PathVariable Long idVenta) {
        Venta venta = ventaService.obtenerVenta(idVenta);
        VentaResponse r = ventaService.toResponse(venta);
        EntityModel<VentaResponse> model = EntityModel.of(r);
        model.add(linkTo(methodOn(VentaController.class)
                .obtenerVenta(idVenta)).withSelfRel());
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Actualizar una venta existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Venta actualizada",
                    content = @Content(schema = @Schema(implementation = VentaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content)
    })
    @PutMapping("/{idVenta}")
    public ResponseEntity<EntityModel<VentaResponse>> actualizarVenta(
            @Parameter(description = "ID de la venta", example = "1", required = true) @PathVariable Long idVenta,
            @Valid @RequestBody CrearVentaRequest request) {
        Venta venta = ventaService.actualizarVenta(idVenta, request);
        VentaResponse r = ventaService.toResponse(venta);
        EntityModel<VentaResponse> model = EntityModel.of(r);
        model.add(linkTo(methodOn(VentaController.class)
                .obtenerVenta(idVenta)).withSelfRel());
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Eliminar una venta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Venta eliminada", content = @Content),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content)
    })
    @DeleteMapping("/{idVenta}")
    public ResponseEntity<Void> eliminarVenta(
            @Parameter(description = "ID de la venta", example = "1", required = true) @PathVariable Long idVenta) {
        ventaService.eliminarVenta(idVenta);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Generar la factura electronica de una venta",
            description = "Genera el documento tributario electronico para la venta indicada.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Factura generada",
                    content = @Content(schema = @Schema(implementation = FacturaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "La venta ya tiene factura asociada", content = @Content)
    })
    @PostMapping("/{idVenta}/factura")
    public ResponseEntity<EntityModel<FacturaResponse>> generarFactura(
            @Parameter(description = "ID de la venta", example = "1", required = true) @PathVariable Long idVenta,
            @RequestBody(required = false) CrearFacturaRequest request) {
        if (request == null) request = new CrearFacturaRequest();
        Factura factura = ventaService.generarFactura(idVenta, request);
        FacturaResponse r = ventaService.toResponse(factura);
        EntityModel<FacturaResponse> model = EntityModel.of(r);
        model.add(linkTo(methodOn(VentaController.class)
                .obtenerFactura(factura.getIdFactura())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Obtener una factura por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Factura encontrada",
                    content = @Content(schema = @Schema(implementation = FacturaResponse.class))),
            @ApiResponse(responseCode = "404", description = "Factura no encontrada", content = @Content)
    })
    @GetMapping("/facturas/{idFactura}")
    public ResponseEntity<EntityModel<FacturaResponse>> obtenerFactura(
            @Parameter(description = "ID de la factura", example = "1", required = true) @PathVariable Long idFactura) {
        Factura factura = ventaService.obtenerFactura(idFactura);
        FacturaResponse r = ventaService.toResponse(factura);
        EntityModel<FacturaResponse> model = EntityModel.of(r);
        model.add(linkTo(methodOn(VentaController.class)
                .obtenerFactura(idFactura)).withSelfRel());
        return ResponseEntity.ok(model);
    }
}