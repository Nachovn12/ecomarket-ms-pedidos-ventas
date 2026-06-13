package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.CancelarPedidoRequest;
import com.ecomarket.pedidos.dto.CrearPedidoRequest;
import com.ecomarket.pedidos.dto.CrearReclamacionRequest;
import com.ecomarket.pedidos.dto.PedidoResponse;
import com.ecomarket.pedidos.model.EstadoPedido;
import com.ecomarket.pedidos.model.HistorialPedido;
import com.ecomarket.pedidos.model.Pedido;
import com.ecomarket.pedidos.model.Reclamacion;
import com.ecomarket.pedidos.service.PedidoService;
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
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Gestion del ciclo de vida de los pedidos: alta, consulta, actualizacion, cancelacion y trazabilidad")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @Operation(summary = "Crear un pedido desde un carrito",
            description = "Convierte un carrito activo en un pedido. Valida stock y descuenta inventario.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido creado",
                    content = @Content(schema = @Schema(implementation = PedidoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos o carrito vacio", content = @Content),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente", content = @Content)
    })
    @PostMapping("/desde-carrito/{idCarrito}")
    public ResponseEntity<EntityModel<PedidoResponse>> crearDesdeCarrito(
            @Parameter(description = "ID del carrito", example = "1", required = true) @PathVariable Long idCarrito,
            @Valid @RequestBody CrearPedidoRequest request) {
        Pedido pedido = pedidoService.crearDesdeCarrito(idCarrito, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toModel(pedidoService.toResponse(pedido)));
    }

    @Operation(summary = "Listar todos los pedidos")
    @ApiResponse(responseCode = "200", description = "Listado de pedidos",
            content = @Content(schema = @Schema(implementation = PedidoResponse.class)))
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<PedidoResponse>>> listarPedidos() {
        List<EntityModel<PedidoResponse>> pedidos = pedidoService.listarPedidos()
                .stream()
                .map(p -> toModel(pedidoService.toResponse(p)))
                .collect(Collectors.toList());
        CollectionModel<EntityModel<PedidoResponse>> collection = CollectionModel.of(pedidos);
        collection.add(linkTo(methodOn(PedidoController.class).listarPedidos()).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Obtener un pedido por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado",
                    content = @Content(schema = @Schema(implementation = PedidoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content)
    })
    @GetMapping("/{idPedido}")
    public ResponseEntity<EntityModel<PedidoResponse>> obtenerPedido(
            @Parameter(description = "ID del pedido", example = "1", required = true) @PathVariable Long idPedido) {
        Pedido pedido = pedidoService.obtenerPedido(idPedido);
        return ResponseEntity.ok(toModel(pedidoService.toResponse(pedido)));
    }

    @Operation(summary = "Actualizar un pedido existente",
            description = "Permite modificar direccion, observaciones o metodo de pago antes del despacho.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido actualizado",
                    content = @Content(schema = @Schema(implementation = PedidoResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Pedido no editable en su estado actual", content = @Content)
    })
    @PutMapping("/{idPedido}")
    public ResponseEntity<EntityModel<PedidoResponse>> actualizarPedido(
            @Parameter(description = "ID del pedido", example = "1", required = true) @PathVariable Long idPedido,
            @Valid @RequestBody CrearPedidoRequest request) {
        Pedido pedido = pedidoService.actualizarPedido(idPedido, request);
        return ResponseEntity.ok(toModel(pedidoService.toResponse(pedido)));
    }

    @Operation(summary = "Eliminar un pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pedido eliminado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content)
    })
    @DeleteMapping("/{idPedido}")
    public ResponseEntity<Void> eliminarPedido(
            @Parameter(description = "ID del pedido", example = "1", required = true) @PathVariable Long idPedido) {
        pedidoService.eliminarPedido(idPedido);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Consultar el estado actual de un pedido")
    @ApiResponse(responseCode = "200", description = "Estado del pedido")
    @GetMapping("/{idPedido}/estado")
    public ResponseEntity<Map<String, Object>> consultarEstado(
            @Parameter(description = "ID del pedido", example = "1", required = true) @PathVariable Long idPedido) {
        EstadoPedido estado = pedidoService.consultarEstado(idPedido);
        return ResponseEntity.ok(Map.of(
                "idPedido", idPedido,
                "estado", estado
        ));
    }

    @Operation(summary = "Obtener el historial de cambios de un pedido")
    @ApiResponse(responseCode = "200", description = "Historial del pedido",
            content = @Content(schema = @Schema(implementation = HistorialPedido.class)))
    @GetMapping("/{idPedido}/historial")
    public ResponseEntity<List<HistorialPedido>> listarHistorial(
            @Parameter(description = "ID del pedido", example = "1", required = true) @PathVariable Long idPedido) {
        return ResponseEntity.ok(pedidoService.listarHistorialPedido(idPedido));
    }

    @Operation(summary = "Obtener el historial de pedidos de un cliente")
    @ApiResponse(responseCode = "200", description = "Historial de pedidos del cliente",
            content = @Content(schema = @Schema(implementation = PedidoResponse.class)))
    @GetMapping("/clientes/{idCliente}/historial")
    public ResponseEntity<CollectionModel<EntityModel<PedidoResponse>>> historialCliente(
            @Parameter(description = "ID del cliente", example = "10", required = true) @PathVariable Long idCliente) {
        List<EntityModel<PedidoResponse>> pedidos = pedidoService.historialCliente(idCliente)
                .stream()
                .map(p -> toModel(pedidoService.toResponse(p)))
                .collect(Collectors.toList());
        CollectionModel<EntityModel<PedidoResponse>> collection = CollectionModel.of(pedidos);
        collection.add(linkTo(methodOn(PedidoController.class)
                .historialCliente(idCliente)).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @Operation(summary = "Cancelar un pedido",
            description = "Solo permite cancelar pedidos en estado PENDIENTE. Restaura el stock y registra el motivo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido cancelado",
                    content = @Content(schema = @Schema(implementation = PedidoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Pedido no se puede cancelar en su estado actual", content = @Content)
    })
    @PatchMapping("/{idPedido}/cancelar")
    public ResponseEntity<EntityModel<PedidoResponse>> cancelarPedido(
            @Parameter(description = "ID del pedido", example = "1", required = true) @PathVariable Long idPedido,
            @RequestBody(required = false) CancelarPedidoRequest request) {
        String motivo = request != null ? request.getMotivo() : null;
        Pedido pedido = pedidoService.cancelarPedido(idPedido, motivo);
        return ResponseEntity.ok(toModel(pedidoService.toResponse(pedido)));
    }

    @Operation(summary = "Crear una reclamacion asociada a un pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reclamacion creada",
                    content = @Content(schema = @Schema(implementation = Reclamacion.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content)
    })
    @PostMapping("/{idPedido}/reclamaciones")
    public ResponseEntity<EntityModel<Reclamacion>> crearReclamacion(
            @Parameter(description = "ID del pedido", example = "1", required = true) @PathVariable Long idPedido,
            @Valid @RequestBody CrearReclamacionRequest request) {
        Reclamacion reclamacion = pedidoService.crearReclamacionPorPedido(idPedido, request);
        EntityModel<Reclamacion> model = EntityModel.of(reclamacion);
        model.add(linkTo(methodOn(PedidoController.class)
                .listarReclamaciones(idPedido)).withRel("reclamaciones"));
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @Operation(summary = "Listar las reclamaciones de un pedido")
    @ApiResponse(responseCode = "200", description = "Listado de reclamaciones",
            content = @Content(schema = @Schema(implementation = Reclamacion.class)))
    @GetMapping("/{idPedido}/reclamaciones")
    public ResponseEntity<List<Reclamacion>> listarReclamaciones(
            @Parameter(description = "ID del pedido", example = "1", required = true) @PathVariable Long idPedido) {
        return ResponseEntity.ok(pedidoService.listarReclamacionesPorPedido(idPedido));
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
