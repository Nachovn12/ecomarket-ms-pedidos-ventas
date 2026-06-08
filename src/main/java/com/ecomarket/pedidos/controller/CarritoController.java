package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.ActualizarCantidadRequest;
import com.ecomarket.pedidos.dto.AgregarItemCarritoRequest;
import com.ecomarket.pedidos.dto.AplicarCuponRequest;
import com.ecomarket.pedidos.dto.AplicarCuponResponse;
import com.ecomarket.pedidos.dto.CrearCarritoRequest;
import com.ecomarket.pedidos.model.CarritoCompra;
import com.ecomarket.pedidos.service.CarritoService;
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
public class CarritoController {

    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @PostMapping
    public ResponseEntity<EntityModel<CarritoCompra>> crearCarrito(@Valid @RequestBody CrearCarritoRequest request) {
        CarritoCompra carrito = carritoService.crearCarrito(request.getIdCliente());
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(carrito));
    }

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<CarritoCompra>>> listarCarritos() {
        List<EntityModel<CarritoCompra>> carritos = carritoService.listarCarritos()
                .stream()
                .map(this::toModel)
                .collect(Collectors.toList());
        CollectionModel<EntityModel<CarritoCompra>> collection = CollectionModel.of(carritos);
        collection.add(linkTo(methodOn(CarritoController.class).listarCarritos()).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/{idCarrito}")
    public ResponseEntity<EntityModel<CarritoCompra>> obtenerCarrito(@PathVariable Long idCarrito) {
        CarritoCompra carrito = carritoService.obtenerCarrito(idCarrito);
        return ResponseEntity.ok(toModel(carrito));
    }

    @PostMapping("/{idCarrito}/items")
    public ResponseEntity<EntityModel<CarritoCompra>> agregarItem(
            @PathVariable Long idCarrito,
            @Valid @RequestBody AgregarItemCarritoRequest request) {
        CarritoCompra carrito = carritoService.agregarItem(idCarrito, request);
        return ResponseEntity.ok(toModel(carrito));
    }

    @PutMapping("/{idCarrito}/items/{idItem}")
    public ResponseEntity<EntityModel<CarritoCompra>> actualizarCantidad(
            @PathVariable Long idCarrito,
            @PathVariable Long idItem,
            @Valid @RequestBody ActualizarCantidadRequest request) {
        CarritoCompra carrito = carritoService.actualizarCantidad(idCarrito, idItem, request);
        return ResponseEntity.ok(toModel(carrito));
    }

    @DeleteMapping("/{idCarrito}/items/{idItem}")
    public ResponseEntity<EntityModel<CarritoCompra>> eliminarItem(
            @PathVariable Long idCarrito,
            @PathVariable Long idItem) {
        CarritoCompra carrito = carritoService.eliminarItem(idCarrito, idItem);
        return ResponseEntity.ok(toModel(carrito));
    }

    @PostMapping("/{idCarrito}/cupon")
    public ResponseEntity<EntityModel<AplicarCuponResponse>> aplicarCupon(
            @PathVariable Long idCarrito,
            @Valid @RequestBody AplicarCuponRequest request) {
        AplicarCuponResponse response = carritoService.aplicarCupon(idCarrito, request.getCodigo());
        EntityModel<AplicarCuponResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(CarritoController.class).aplicarCupon(idCarrito, request)).withSelfRel());
        model.add(linkTo(methodOn(CarritoController.class).obtenerCarrito(idCarrito)).withRel("carrito"));
        return ResponseEntity.ok(model);
    }

    private EntityModel<CarritoCompra> toModel(CarritoCompra carrito) {
        EntityModel<CarritoCompra> model = EntityModel.of(carrito);
        model.add(linkTo(methodOn(CarritoController.class).obtenerCarrito(carrito.getIdCarrito())).withSelfRel());
        model.add(linkTo(methodOn(CarritoController.class).listarCarritos()).withRel("carritos"));
        return model;
    }
}