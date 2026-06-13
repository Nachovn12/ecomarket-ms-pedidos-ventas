package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.CuponDescuentoResponse;
import com.ecomarket.pedidos.model.CuponDescuento;
import com.ecomarket.pedidos.service.CuponDescuentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/pedidos/cupones")
@Tag(name = "Cupones", description = "Administracion de cupones de descuento")
public class CuponDescuentoController {

    private final CuponDescuentoService cuponDescuentoService;

    public CuponDescuentoController(CuponDescuentoService cuponDescuentoService) {
        this.cuponDescuentoService = cuponDescuentoService;
    }

    @Operation(summary = "Crear un nuevo cupon de descuento",
            description = "Registra un cupon. Valida codigo unico y fecha de vencimiento.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cupon creado",
                    content = @Content(schema = @Schema(implementation = CuponDescuentoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content),
            @ApiResponse(responseCode = "409", description = "Codigo de cupon duplicado", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EntityModel<CuponDescuentoResponse>> crearCupon(@Valid @RequestBody CuponDescuento cuponDescuento) {
        CuponDescuento creado = cuponDescuentoService.crearCupon(cuponDescuento);
        CuponDescuentoResponse response = cuponDescuentoService.toResponse(creado);
        EntityModel<CuponDescuentoResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(CuponDescuentoController.class).crearCupon(cuponDescuento)).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }
}