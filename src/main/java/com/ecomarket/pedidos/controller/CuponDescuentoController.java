package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.model.CuponDescuento;
import com.ecomarket.pedidos.service.CuponDescuentoService;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/pedidos/cupones")
public class CuponDescuentoController {

    private final CuponDescuentoService cuponDescuentoService;

    public CuponDescuentoController(CuponDescuentoService cuponDescuentoService) {
        this.cuponDescuentoService = cuponDescuentoService;
    }

    @PostMapping
    public ResponseEntity<EntityModel<CuponDescuento>> crearCupon(@Valid @RequestBody CuponDescuento cuponDescuento) {
        CuponDescuento creado = cuponDescuentoService.crearCupon(cuponDescuento);
        EntityModel<CuponDescuento> model = EntityModel.of(creado);
        model.add(linkTo(methodOn(CuponDescuentoController.class).crearCupon(cuponDescuento)).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }
}