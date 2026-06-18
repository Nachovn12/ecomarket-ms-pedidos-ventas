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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<CarritoResponse> crearCarrito(
            @Valid @RequestBody CrearCarritoRequest request) {
        CarritoCompra carrito = carritoService.crearCarrito(request.getIdCliente());
        return ResponseEntity.status(HttpStatus.CREATED).body(carritoService.toResponse(carrito));
    }

    @Operation(summary = "Listar todos los carritos")
    @ApiResponse(responseCode = "200", description = "Listado de carritos",
            content = @Content(schema = @Schema(implementation = CarritoResponse.class)))
    @GetMapping
    public ResponseEntity<List<CarritoResponse>> listarCarritos() {
        List<CarritoResponse> carritos = carritoService.listarCarritos()
                .stream()
                .map(carritoService::toResponse)
                .toList();
        return ResponseEntity.ok(carritos);
    }

    @Operation(summary = "Obtener un carrito por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Carrito encontrado",
                    content = @Content(schema = @Schema(implementation = CarritoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado", content = @Content)
    })
    @GetMapping("/{idCarrito}")
    public ResponseEntity<CarritoResponse> obtenerCarrito(
            @Parameter(description = "ID del carrito", example = "1", required = true)
            @PathVariable Long idCarrito) {
        CarritoCompra carrito = carritoService.obtenerCarrito(idCarrito);
        return ResponseEntity.ok(carritoService.toResponse(carrito));
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
    public ResponseEntity<CarritoResponse> agregarItem(
            @Parameter(description = "ID del carrito", example = "1", required = true) @PathVariable Long idCarrito,
            @Valid @RequestBody AgregarItemCarritoRequest request) {
        CarritoCompra carrito = carritoService.agregarItem(idCarrito, request);
        return ResponseEntity.ok(carritoService.toResponse(carrito));
    }

    @Operation(summary = "Actualizar la cantidad de un item del carrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cantidad actualizada",
                    content = @Content(schema = @Schema(implementation = CarritoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Cantidad invalida o sin stock", content = @Content),
            @ApiResponse(responseCode = "404", description = "Item o carrito no encontrado", content = @Content)
    })
    @PutMapping("/{idCarrito}/items/{idItem}")
    public ResponseEntity<CarritoResponse> actualizarCantidad(
            @Parameter(description = "ID del carrito", example = "1", required = true) @PathVariable Long idCarrito,
            @Parameter(description = "ID del item", example = "3", required = true) @PathVariable Long idItem,
            @Valid @RequestBody ActualizarCantidadRequest request) {
        CarritoCompra carrito = carritoService.actualizarCantidad(idCarrito, idItem, request);
        return ResponseEntity.ok(carritoService.toResponse(carrito));
    }

    @Operation(summary = "Eliminar un item del carrito")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item eliminado",
                    content = @Content(schema = @Schema(implementation = CarritoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Item o carrito no encontrado", content = @Content)
    })
    @DeleteMapping("/{idCarrito}/items/{idItem}")
    public ResponseEntity<CarritoResponse> eliminarItem(
            @Parameter(description = "ID del carrito", example = "1", required = true) @PathVariable Long idCarrito,
            @Parameter(description = "ID del item", example = "3", required = true) @PathVariable Long idItem) {
        CarritoCompra carrito = carritoService.eliminarItem(idCarrito, idItem);
        return ResponseEntity.ok(carritoService.toResponse(carrito));
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
    public ResponseEntity<AplicarCuponResponse> aplicarCupon(
            @Parameter(description = "ID del carrito", example = "1", required = true) @PathVariable Long idCarrito,
            @Valid @RequestBody AplicarCuponRequest request) {
        AplicarCuponResponse response = carritoService.aplicarCupon(idCarrito, request.getCodigo());
        return ResponseEntity.ok(response);
    }
}