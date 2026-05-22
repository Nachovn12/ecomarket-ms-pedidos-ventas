package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.ActualizarEstadoDevolucionRequest;
import com.ecomarket.pedidos.dto.ActualizarEstadoReclamacionRequest;
import com.ecomarket.pedidos.dto.CrearDevolucionRequest;
import com.ecomarket.pedidos.dto.CrearReclamacionRequest;
import com.ecomarket.pedidos.entity.Devolucion;
import com.ecomarket.pedidos.entity.Reclamacion;
import com.ecomarket.pedidos.service.DevolucionService;
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
public class DevolucionController {

    private final DevolucionService devolucionService;

    public DevolucionController(DevolucionService devolucionService) {
        this.devolucionService = devolucionService;
    }

    // ─── DEVOLUCIONES ──────────────────────────────────────────────────────────

    @PostMapping("/ventas/{idVenta}/devoluciones")
    public ResponseEntity<EntityModel<Devolucion>> crearDevolucion(
            @PathVariable Long idVenta,
            @Valid @RequestBody CrearDevolucionRequest request) {
        request.setIdVenta(idVenta);
        Devolucion devolucion = devolucionService.crearDevolucion(request);
        EntityModel<Devolucion> model = EntityModel.of(devolucion);
        model.add(linkTo(methodOn(DevolucionController.class)
                .obtenerDevolucion(devolucion.getIdDevolucion())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @GetMapping("/devoluciones")
    public ResponseEntity<CollectionModel<EntityModel<Devolucion>>> listarDevoluciones() {
        List<EntityModel<Devolucion>> devoluciones = devolucionService.listarDevoluciones()
                .stream()
                .map(d -> {
                    EntityModel<Devolucion> model = EntityModel.of(d);
                    model.add(linkTo(methodOn(DevolucionController.class)
                            .obtenerDevolucion(d.getIdDevolucion())).withSelfRel());
                    return model;
                })
                .collect(Collectors.toList());
        CollectionModel<EntityModel<Devolucion>> collection = CollectionModel.of(devoluciones);
        collection.add(linkTo(methodOn(DevolucionController.class).listarDevoluciones()).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/devoluciones/{id}")
    public ResponseEntity<EntityModel<Devolucion>> obtenerDevolucion(@PathVariable Long id) {
        Devolucion devolucion = devolucionService.obtenerDevolucion(id);
        EntityModel<Devolucion> model = EntityModel.of(devolucion);
        model.add(linkTo(methodOn(DevolucionController.class)
                .obtenerDevolucion(id)).withSelfRel());
        return ResponseEntity.ok(model);
    }

    @PatchMapping("/devoluciones/{id}/estado")
    public ResponseEntity<EntityModel<Devolucion>> actualizarEstadoDevolucion(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoDevolucionRequest request) {
        Devolucion devolucion = devolucionService.actualizarEstadoDevolucion(id, request.getEstado());
        EntityModel<Devolucion> model = EntityModel.of(devolucion);
        model.add(linkTo(methodOn(DevolucionController.class)
                .obtenerDevolucion(id)).withSelfRel());
        return ResponseEntity.ok(model);
    }

    // ─── RECLAMACIONES ─────────────────────────────────────────────────────────

    @PostMapping("/reclamaciones")
    public ResponseEntity<EntityModel<Reclamacion>> crearReclamacion(
            @Valid @RequestBody CrearReclamacionRequest request) {
        Reclamacion reclamacion = devolucionService.crearReclamacion(request);
        EntityModel<Reclamacion> model = EntityModel.of(reclamacion);
        model.add(linkTo(methodOn(DevolucionController.class)
                .obtenerReclamacion(reclamacion.getIdReclamacion())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @GetMapping("/reclamaciones")
    public ResponseEntity<CollectionModel<EntityModel<Reclamacion>>> listarReclamaciones() {
        List<EntityModel<Reclamacion>> reclamaciones = devolucionService.listarReclamaciones()
                .stream()
                .map(r -> {
                    EntityModel<Reclamacion> model = EntityModel.of(r);
                    model.add(linkTo(methodOn(DevolucionController.class)
                            .obtenerReclamacion(r.getIdReclamacion())).withSelfRel());
                    return model;
                })
                .collect(Collectors.toList());
        CollectionModel<EntityModel<Reclamacion>> collection = CollectionModel.of(reclamaciones);
        collection.add(linkTo(methodOn(DevolucionController.class).listarReclamaciones()).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/reclamaciones/{id}")
    public ResponseEntity<EntityModel<Reclamacion>> obtenerReclamacion(@PathVariable Long id) {
        Reclamacion reclamacion = devolucionService.obtenerReclamacion(id);
        EntityModel<Reclamacion> model = EntityModel.of(reclamacion);
        model.add(linkTo(methodOn(DevolucionController.class)
                .obtenerReclamacion(id)).withSelfRel());
        return ResponseEntity.ok(model);
    }

    @PatchMapping("/reclamaciones/{id}/estado")
    public ResponseEntity<EntityModel<Reclamacion>> actualizarEstadoReclamacion(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoReclamacionRequest request) {
        Reclamacion reclamacion = devolucionService.actualizarEstadoReclamacion(id, request.getEstado());
        EntityModel<Reclamacion> model = EntityModel.of(reclamacion);
        model.add(linkTo(methodOn(DevolucionController.class)
                .obtenerReclamacion(id)).withSelfRel());
        return ResponseEntity.ok(model);
    }
}