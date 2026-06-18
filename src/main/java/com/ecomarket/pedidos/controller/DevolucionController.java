package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.ActualizarEstadoDevolucionRequest;
import com.ecomarket.pedidos.dto.ActualizarEstadoReclamacionRequest;
import com.ecomarket.pedidos.dto.CrearDevolucionRequest;
import com.ecomarket.pedidos.dto.CrearReclamacionRequest;
import com.ecomarket.pedidos.dto.DevolucionResponse;
import com.ecomarket.pedidos.dto.ReclamacionResponse;
import com.ecomarket.pedidos.model.Devolucion;
import com.ecomarket.pedidos.model.Reclamacion;
import com.ecomarket.pedidos.service.DevolucionService;
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
@RequestMapping("/api/pedidos")
@Tag(name = "Devoluciones y Reclamaciones", description = "Gestion post-venta: devoluciones y reclamaciones")
public class DevolucionController {

    private final DevolucionService devolucionService;

    public DevolucionController(DevolucionService devolucionService) {
        this.devolucionService = devolucionService;
    }

    @Operation(summary = "Crear una devolucion para una venta")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Devolucion creada",
                    content = @Content(schema = @Schema(implementation = DevolucionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Venta no encontrada", content = @Content)
    })
    @PostMapping("/ventas-pedido/{idVenta}/devoluciones")
    public ResponseEntity<DevolucionResponse> crearDevolucion(
            @Parameter(description = "ID de la venta", example = "1", required = true) @PathVariable Long idVenta,
            @Valid @RequestBody CrearDevolucionRequest request) {
        request.setIdVenta(idVenta);
        Devolucion devolucion = devolucionService.crearDevolucion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(devolucionService.toResponse(devolucion));
    }

    @Operation(summary = "Listar todas las devoluciones")
    @ApiResponse(responseCode = "200", description = "Listado de devoluciones",
            content = @Content(schema = @Schema(implementation = DevolucionResponse.class)))
    @GetMapping("/devoluciones")
    public ResponseEntity<List<DevolucionResponse>> listarDevoluciones() {
        List<DevolucionResponse> devoluciones = devolucionService.listarDevoluciones()
                .stream()
                .map(devolucionService::toResponse)
                .toList();
        return ResponseEntity.ok(devoluciones);
    }

    @Operation(summary = "Obtener una devolucion por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devolucion encontrada",
                    content = @Content(schema = @Schema(implementation = DevolucionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Devolucion no encontrada", content = @Content)
    })
    @GetMapping("/devoluciones/{id}")
    public ResponseEntity<DevolucionResponse> obtenerDevolucion(
            @Parameter(description = "ID de la devolucion", example = "1", required = true) @PathVariable Long id) {
        Devolucion devolucion = devolucionService.obtenerDevolucion(id);
        return ResponseEntity.ok(devolucionService.toResponse(devolucion));
    }

    @Operation(summary = "Actualizar estado de una devolucion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado",
                    content = @Content(schema = @Schema(implementation = DevolucionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Estado invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Devolucion no encontrada", content = @Content)
    })
    @PatchMapping("/devoluciones/{id}/estado")
    public ResponseEntity<DevolucionResponse> actualizarEstadoDevolucion(
            @Parameter(description = "ID de la devolucion", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoDevolucionRequest request) {
        Devolucion devolucion = devolucionService.actualizarEstadoDevolucion(id, request.getEstado());
        return ResponseEntity.ok(devolucionService.toResponse(devolucion));
    }

    @Operation(summary = "Crear una reclamacion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reclamacion creada",
                    content = @Content(schema = @Schema(implementation = ReclamacionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content)
    })
    @PostMapping("/reclamaciones")
    public ResponseEntity<ReclamacionResponse> crearReclamacion(
            @Valid @RequestBody CrearReclamacionRequest request) {
        Reclamacion reclamacion = devolucionService.crearReclamacion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(devolucionService.toResponse(reclamacion));
    }

    @Operation(summary = "Listar todas las reclamaciones")
    @ApiResponse(responseCode = "200", description = "Listado de reclamaciones",
            content = @Content(schema = @Schema(implementation = ReclamacionResponse.class)))
    @GetMapping("/reclamaciones")
    public ResponseEntity<List<ReclamacionResponse>> listarReclamaciones() {
        List<ReclamacionResponse> reclamaciones = devolucionService.listarReclamaciones()
                .stream()
                .map(devolucionService::toResponse)
                .toList();
        return ResponseEntity.ok(reclamaciones);
    }

    @Operation(summary = "Obtener una reclamacion por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reclamacion encontrada",
                    content = @Content(schema = @Schema(implementation = ReclamacionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Reclamacion no encontrada", content = @Content)
    })
    @GetMapping("/reclamaciones/{id}")
    public ResponseEntity<ReclamacionResponse> obtenerReclamacion(
            @Parameter(description = "ID de la reclamacion", example = "1", required = true) @PathVariable Long id) {
        Reclamacion reclamacion = devolucionService.obtenerReclamacion(id);
        return ResponseEntity.ok(devolucionService.toResponse(reclamacion));
    }

    @Operation(summary = "Actualizar estado de una reclamacion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado",
                    content = @Content(schema = @Schema(implementation = ReclamacionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Estado invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reclamacion no encontrada", content = @Content)
    })
    @PatchMapping("/reclamaciones/{id}/estado")
    public ResponseEntity<ReclamacionResponse> actualizarEstadoReclamacion(
            @Parameter(description = "ID de la reclamacion", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoReclamacionRequest request) {
        Reclamacion reclamacion = devolucionService.actualizarEstadoReclamacion(id, request.getEstado());
        return ResponseEntity.ok(devolucionService.toResponse(reclamacion));
    }
}