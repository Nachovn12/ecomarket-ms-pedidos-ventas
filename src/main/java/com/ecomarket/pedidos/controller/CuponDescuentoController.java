package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.AplicarCuponRequest;
import com.ecomarket.pedidos.dto.AplicarCuponResponse;
import com.ecomarket.pedidos.service.CuponDescuentoService;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/pedidos")
public class CuponDescuentoController {

    private final CuponDescuentoService cuponDescuentoService;

    public CuponDescuentoController(CuponDescuentoService cuponDescuentoService) {
        this.cuponDescuentoService = cuponDescuentoService;
    }

    @PostMapping("/carritos/{idCarrito}/cupon")
    public ResponseEntity<EntityModel<AplicarCuponResponse>> aplicarCuponCarrito(
            @PathVariable Long idCarrito,
            @Valid @RequestBody AplicarCuponRequest request
    ) {
        Double subtotalSimulado = 10000.0;
        // Aquí usamos request.getCodigo() que ahora sí existirá
        AplicarCuponResponse response = cuponDescuentoService.aplicarCupon(request.getCodigo(), subtotalSimulado);

        EntityModel<AplicarCuponResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(CuponDescuentoController.class).aplicarCuponCarrito(idCarrito, request)).withSelfRel());

        return ResponseEntity.ok(model);
    }
}