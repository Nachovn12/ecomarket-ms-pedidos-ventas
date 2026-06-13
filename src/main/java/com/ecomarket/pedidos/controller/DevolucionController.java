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
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api")
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
    @PostMapping("/ventas/{idVenta}/devoluciones")
    public ResponseEntity<EntityModel<DevolucionResponse>> crearDevolucion(
            @Parameter(description = "ID de la venta", example = "1", required = true) @PathVariable Long idVenta,
            @Valid @RequestBody CrearDevolucionRequest request) {
        request.setIdVenta(idVenta);
        Devolucion devolucion = devolucionService.crearDevolucion(request);
        DevolucionResponse r = devolucionService.toResponse(devolucion);
        EntityModel<DevolucionResponse> model = EntityModel.of(r);
        model.add(linkTo(methodOn(DevolucionController.class)
                .obtenerDevolucion(devolucion.getIdDevolucion())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Listar todas las devoluciones")
    @ApiResponse(responseCode = "200", description = "Listado de devoluciones",
            content = @Content(schema = @Schema(implementation = DevolucionResponse.class)))
    @GetMapping("/devoluciones")
    public ResponseEntity<CollectionModel<EntityModel<DevolucionResponse>>> listarDevoluciones() {
        List<EntityModel<DevolucionResponse>> devoluciones = devolucionService.listarDevoluciones()
                .stream()
                .map(d -> {
                    DevolucionResponse r = devolucionService.toResponse(d);
                    EntityModel<DevolucionResponse> model = EntityModel.of(r);
                    model.add(linkTo(methodOn(DevolucionController.class)
                            .obtenerDevolucion(d.getIdDevolucion())).withSelfRel());
                    return model;
                })
                .collect(Collectors.toList());
        CollectionModel<EntityModel<DevolucionResponse>> collection = CollectionModel.of(devoluciones);
        collection.add(linkTo(methodOn(DevolucionController.class).listarDevoluciones()).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Obtener una devolucion por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devolucion encontrada",
                    content = @Content(schema = @Schema(implementation = DevolucionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Devolucion no encontrada", content = @Content)
    })
    @GetMapping("/devoluciones/{id}")
    public ResponseEntity<EntityModel<DevolucionResponse>> obtenerDevolucion(
            @Parameter(description = "ID de la devolucion", example = "1", required = true) @PathVariable Long id) {
        Devolucion devolucion = devolucionService.obtenerDevolucion(id);
        DevolucionResponse r = devolucionService.toResponse(devolucion);
        EntityModel<DevolucionResponse> model = EntityModel.of(r);
        model.add(linkTo(methodOn(DevolucionController.class)
                .obtenerDevolucion(id)).withSelfRel());
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Actualizar estado de una devolucion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado",
                    content = @Content(schema = @Schema(implementation = DevolucionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Estado invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Devolucion no encontrada", content = @Content)
    })
    @PatchMapping("/devoluciones/{id}/estado")
    public ResponseEntity<EntityModel<DevolucionResponse>> actualizarEstadoDevolucion(
            @Parameter(description = "ID de la devolucion", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoDevolucionRequest request) {
        Devolucion devolucion = devolucionService.actualizarEstadoDevolucion(id, request.getEstado());
        DevolucionResponse r = devolucionService.toResponse(devolucion);
        EntityModel<DevolucionResponse> model = EntityModel.of(r);
        model.add(linkTo(methodOn(DevolucionController.class)
                .obtenerDevolucion(id)).withSelfRel());
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Crear una reclamacion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reclamacion creada",
                    content = @Content(schema = @Schema(implementation = ReclamacionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content)
    })
    @PostMapping("/reclamaciones")
    public ResponseEntity<EntityModel<ReclamacionResponse>> crearReclamacion(
            @Valid @RequestBody CrearReclamacionRequest request) {
        Reclamacion reclamacion = devolucionService.crearReclamacion(request);
        ReclamacionResponse r = devolucionService.toResponse(reclamacion);
        EntityModel<ReclamacionResponse> model = EntityModel.of(r);
        model.add(linkTo(methodOn(DevolucionController.class)
                .obtenerReclamacion(reclamacion.getIdReclamacion())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Listar todas las reclamaciones")
    @ApiResponse(responseCode = "200", description = "Listado de reclamaciones",
            content = @Content(schema = @Schema(implementation = ReclamacionResponse.class)))
    @GetMapping("/reclamaciones")
    public ResponseEntity<CollectionModel<EntityModel<ReclamacionResponse>>> listarReclamaciones() {
        List<EntityModel<ReclamacionResponse>> reclamaciones = devolucionService.listarReclamaciones()
                .stream()
                .map(rec -> {
                    ReclamacionResponse r = devolucionService.toResponse(rec);
                    EntityModel<ReclamacionResponse> model = EntityModel.of(r);
                    model.add(linkTo(methodOn(DevolucionController.class)
                            .obtenerReclamacion(rec.getIdReclamacion())).withSelfRel());
                    return model;
                })
                .collect(Collectors.toList());
        CollectionModel<EntityModel<ReclamacionResponse>> collection = CollectionModel.of(reclamaciones);
        collection.add(linkTo(methodOn(DevolucionController.class).listarReclamaciones()).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Obtener una reclamacion por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reclamacion encontrada",
                    content = @Content(schema = @Schema(implementation = ReclamacionResponse.class))),
            @ApiResponse(responseCode = "404", description = "Reclamacion no encontrada", content = @Content)
    })
    @GetMapping("/reclamaciones/{id}")
    public ResponseEntity<EntityModel<ReclamacionResponse>> obtenerReclamacion(
            @Parameter(description = "ID de la reclamacion", example = "1", required = true) @PathVariable Long id) {
        Reclamacion reclamacion = devolucionService.obtenerReclamacion(id);
        ReclamacionResponse r = devolucionService.toResponse(reclamacion);
        EntityModel<ReclamacionResponse> model = EntityModel.of(r);
        model.add(linkTo(methodOn(DevolucionController.class)
                .obtenerReclamacion(id)).withSelfRel());
        return ResponseEntity.ok(model);
    }

    @Operation(summary = "Actualizar estado de una reclamacion")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado actualizado",
                    content = @Content(schema = @Schema(implementation = ReclamacionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Estado invalido", content = @Content),
            @ApiResponse(responseCode = "404", description = "Reclamacion no encontrada", content = @Content)
    })
    @PatchMapping("/reclamaciones/{id}/estado")
    public ResponseEntity<EntityModel<ReclamacionResponse>> actualizarEstadoReclamacion(
            @Parameter(description = "ID de la reclamacion", example = "1", required = true) @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoReclamacionRequest request) {
        Reclamacion reclamacion = devolucionService.actualizarEstadoReclamacion(id, request.getEstado());
        ReclamacionResponse r = devolucionService.toResponse(reclamacion);
        EntityModel<ReclamacionResponse> model = EntityModel.of(r);
        model.add(linkTo(methodOn(DevolucionController.class)
                .obtenerReclamacion(id)).withSelfRel());
        return ResponseEntity.ok(model);
    }
}