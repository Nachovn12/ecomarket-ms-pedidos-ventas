package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.CancelarPedidoRequest;
import com.ecomarket.pedidos.dto.CrearPedidoRequest;
import com.ecomarket.pedidos.dto.PedidoResponse;
import com.ecomarket.pedidos.entity.EstadoPedido;
import com.ecomarket.pedidos.entity.Pedido;
import com.ecomarket.pedidos.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping("/desde-carrito/{idCarrito}")
    public ResponseEntity<EntityModel<PedidoResponse>> crearDesdeCarrito(
            @PathVariable Long idCarrito,
            @Valid @RequestBody CrearPedidoRequest request) {
        Pedido pedido = pedidoService.crearDesdeCarrito(idCarrito, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(pedidoService.toResponse(pedido)));
    }

    @GetMapping("/{idPedido}")
    public ResponseEntity<EntityModel<PedidoResponse>> obtenerPedido(@PathVariable Long idPedido) {
        Pedido pedido = pedidoService.obtenerPedido(idPedido);
        return ResponseEntity.ok(toModel(pedidoService.toResponse(pedido)));
    }

    @GetMapping("/{idPedido}/estado")
    public ResponseEntity<Map<String, Object>> consultarEstado(@PathVariable Long idPedido) {
        EstadoPedido estado = pedidoService.consultarEstado(idPedido);
        return ResponseEntity.ok(Map.of(
                "idPedido", idPedido,
                "estado", estado
        ));
    }

    @GetMapping("/clientes/{idCliente}/historial")
    public ResponseEntity<CollectionModel<EntityModel<PedidoResponse>>> historialCliente(
            @PathVariable Long idCliente) {
        List<EntityModel<PedidoResponse>> pedidos = pedidoService.historialCliente(idCliente)
                .stream()
                .map(p -> toModel(pedidoService.toResponse(p)))
                .collect(Collectors.toList());
        CollectionModel<EntityModel<PedidoResponse>> collection = CollectionModel.of(pedidos);
        collection.add(linkTo(methodOn(PedidoController.class)
                .historialCliente(idCliente)).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @PatchMapping("/{idPedido}/cancelar")
    public ResponseEntity<EntityModel<PedidoResponse>> cancelarPedido(
            @PathVariable Long idPedido,
            @RequestBody(required = false) CancelarPedidoRequest request) {
        String motivo = request != null ? request.getMotivo() : null;
        Pedido pedido = pedidoService.cancelarPedido(idPedido, motivo);
        return ResponseEntity.ok(toModel(pedidoService.toResponse(pedido)));
    }

    private EntityModel<PedidoResponse> toModel(PedidoResponse response) {
        EntityModel<PedidoResponse> model = EntityModel.of(response);
        model.add(linkTo(methodOn(PedidoController.class)
                .obtenerPedido(response.getIdPedido())).withSelfRel());
        model.add(linkTo(methodOn(PedidoController.class)
                .consultarEstado(response.getIdPedido())).withRel("estado"));
        model.add(linkTo(methodOn(PedidoController.class)
                .historialCliente(response.getIdCliente())).withRel("historial"));
        return model;
    }
}