package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.ActualizarCantidadRequest;
import com.ecomarket.pedidos.dto.AgregarItemCarritoRequest;
import com.ecomarket.pedidos.dto.AplicarCuponRequest;
import com.ecomarket.pedidos.dto.AplicarCuponResponse;
import com.ecomarket.pedidos.dto.CarritoResponse;
import com.ecomarket.pedidos.dto.CrearCarritoRequest;
import com.ecomarket.pedidos.model.CarritoCompra;
import com.ecomarket.pedidos.service.CarritoService;
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
@RequestMapping("/api/pedidos/carritos")
@Tag(name = "Carritos", description = "Operaciones del carrito de compras: items, cantidades y cupones")
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @Operation(summary = "Crear un nuevo carrito", description = "Inicializa un carrito vacio para un cliente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Carrito creado",
                    content = @Content(schema = @Schema(implementation = CarritoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content)
    })
    @PostMapping
    public ResponseEntity<EntityModel<CarritoResponse>> crearCarrito(
            @Valid @RequestBody CrearCarritoRequest request) {
        CarritoCompra carrito = carritoService.crearCarrito(request.getIdCliente());
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(carrito));
    }

    @Operation(summary = "Listar todos los carritos")
    @ApiResponse(responseCode = "200", description = "Listado de carritos",
            content = @Content(schema = @Schema(implementation = CarritoResponse.class)))
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<CarritoResponse>>> listarCarritos() {
        List<EntityModel<CarritoResponse>> carritos = carritoService.listarCarritos()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        CollectionModel<EntityModel<CarritoResponse>> collection = CollectionModel.of(carritos);
        collection.add(linkTo(methodOn(CarritoController.class).listarCarritos()).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Obtener un carrito por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito encontrado",
                    content = @Content(schema = @Schema(implementation = CarritoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado", content = @Content)
    })
    @GetMapping("/{idCarrito}")
    public ResponseEntity<EntityModel<CarritoResponse>> obtenerCarrito(
            @Parameter(description = "ID del carrito", example = "1", required = true)
            @PathVariable Long idCarrito) {
        CarritoCompra carrito = carritoService.obtenerCarrito(idCarrito);
        return ResponseEntity.ok(toModel(carrito));
    }

    @Operation(summary = "Agregar un item al carrito",
            description = "Agrega un producto al carrito con cantidad y precio unitario. Valida stock.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item agregado",
                    content = @Content(schema = @Schema(implementation = CarritoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Stock insuficiente o datos invalidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado", content = @Content)
    })
    @PostMapping("/{idCarrito}/items")
    public ResponseEntity<EntityModel<CarritoResponse>> agregarItem(
            @Parameter(description = "ID del carrito", example = "1", required = true) @PathVariable Long idCarrito,
            @Valid @RequestBody AgregarItemCarritoRequest request) {
        CarritoCompra carrito = carritoService.agregarItem(idCarrito, request);
        return ResponseEntity.ok(toModel(carrito));
    }

    @Operation(summary = "Actualizar la cantidad de un item del carrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cantidad actualizada",
                    content = @Content(schema = @Schema(implementation = CarritoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Cantidad invalida o sin stock", content = @Content),
            @ApiResponse(responseCode = "404", description = "Item o carrito no encontrado", content = @Content)
    })
    @PutMapping("/{idCarrito}/items/{idItem}")
    public ResponseEntity<EntityModel<CarritoResponse>> actualizarCantidad(
            @Parameter(description = "ID del carrito", example = "1", required = true) @PathVariable Long idCarrito,
            @Parameter(description = "ID del item", example = "3", required = true) @PathVariable Long idItem,
            @Valid @RequestBody ActualizarCantidadRequest request) {
        CarritoCompra carrito = carritoService.actualizarCantidad(idCarrito, idItem, request);
        return ResponseEntity.ok(toModel(carrito));
    }

    @Operation(summary = "Eliminar un item del carrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item eliminado",
                    content = @Content(schema = @Schema(implementation = CarritoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item o carrito no encontrado", content = @Content)
    })
    @DeleteMapping("/{idCarrito}/items/{idItem}")
    public ResponseEntity<EntityModel<CarritoResponse>> eliminarItem(
            @Parameter(description = "ID del carrito", example = "1", required = true) @PathVariable Long idCarrito,
            @Parameter(description = "ID del item", example = "3", required = true) @PathVariable Long idItem) {
        CarritoCompra carrito = carritoService.eliminarItem(idCarrito, idItem);
        return ResponseEntity.ok(toModel(carrito));
    }

    @Operation(summary = "Aplicar un cupon de descuento al carrito",
            description = "Calcula el descuento segun el codigo y lo asocia al carrito.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cupon aplicado",
                    content = @Content(schema = @Schema(implementation = AplicarCuponResponse.class))),
            @ApiResponse(responseCode = "400", description = "Cupon invalido o expirado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Carrito o cupon no encontrado", content = @Content)
    })
    @PostMapping("/{idCarrito}/cupon")
    public ResponseEntity<EntityModel<AplicarCuponResponse>> aplicarCupon(
            @Parameter(description = "ID del carrito", example = "1", required = true) @PathVariable Long idCarrito,
            @Valid @RequestBody AplicarCuponRequest request) {
        AplicarCuponResponse response = carritoService.aplicarCupon(idCarrito, request.getCodigo());
        EntityModel<AplicarCuponResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(CarritoController.class).aplicarCupon(idCarrito, request)).withSelfRel());
        model.add(linkTo(methodOn(CarritoController.class).obtenerCarrito(idCarrito)).withRel("carrito"));
        return ResponseEntity.ok(model);
    }

    private EntityModel<CarritoResponse> toModel(CarritoCompra carrito) {
        CarritoResponse r = carritoService.toResponse(carrito);
        EntityModel<CarritoResponse> model = EntityModel.of(r);
        model.add(linkTo(methodOn(CarritoController.class).obtenerCarrito(carrito.getIdCarrito())).withSelfRel());
        model.add(linkTo(methodOn(CarritoController.class).listarCarritos()).withRel("carritos"));
        return model;
    }
}