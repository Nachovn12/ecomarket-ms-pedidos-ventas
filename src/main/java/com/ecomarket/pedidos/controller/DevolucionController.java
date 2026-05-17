package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.CrearDevolucionRequest;
import com.ecomarket.pedidos.dto.CrearReclamacionRequest;
import com.ecomarket.pedidos.entity.Devolucion;
import com.ecomarket.pedidos.entity.Reclamacion;
import com.ecomarket.pedidos.service.DevolucionService;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/ventas")
public class DevolucionController {

    private final DevolucionService devolucionService;

    public DevolucionController(DevolucionService devolucionService) {
        this.devolucionService = devolucionService;
    }

    @PostMapping("/devoluciones")
    public ResponseEntity<EntityModel<Devolucion>> crearDevolucion(
            @Valid @RequestBody CrearDevolucionRequest request) {
        Devolucion devolucion = devolucionService.crearDevolucion(request);
        EntityModel<Devolucion> model = EntityModel.of(devolucion);
        model.add(linkTo(methodOn(DevolucionController.class)
                .crearDevolucion(request)).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @PostMapping("/reclamaciones")
    public ResponseEntity<EntityModel<Reclamacion>> crearReclamacion(
            @Valid @RequestBody CrearReclamacionRequest request) {
        Reclamacion reclamacion = devolucionService.crearReclamacion(request);
        EntityModel<Reclamacion> model = EntityModel.of(reclamacion);
        model.add(linkTo(methodOn(DevolucionController.class)
                .crearReclamacion(request)).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }
}