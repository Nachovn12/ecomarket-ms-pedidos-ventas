package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.CancelarPedidoRequest;
import com.ecomarket.pedidos.dto.CrearPedidoRequest;
import com.ecomarket.pedidos.dto.CrearReclamacionRequest;
import com.ecomarket.pedidos.dto.PedidoResponse;
import com.ecomarket.pedidos.model.EstadoPedido;
import com.ecomarket.pedidos.model.HistorialPedido;
import com.ecomarket.pedidos.model.Pedido;
import com.ecomarket.pedidos.model.Reclamacion;
import com.ecomarket.pedidos.security.IdorValidator;
import com.ecomarket.pedidos.service.PedidoService;
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
import java.util.Map;

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
    public ResponseEntity<PedidoResponse> crearDesdeCarrito(
            @Parameter(description = "ID del carrito", example = "1", required = true) @PathVariable Long idCarrito,
            @Valid @RequestBody CrearPedidoRequest request,
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol,
            @RequestHeader(value = "X-Id-Usuario", required = false) String idUsuario) {
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            IdorValidator.verificarAccesoEntidadCliente(request.getIdCliente(), rol, idUsuario);
        }
        Pedido pedido = pedidoService.crearDesdeCarrito(idCarrito, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.toResponse(pedido));
    }

    @Operation(summary = "Listar todos los pedidos")
    @ApiResponse(responseCode = "200", description = "Listado de pedidos",
            content = @Content(schema = @Schema(implementation = PedidoResponse.class)))
    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarPedidos(
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol) {
        IdorValidator.verificarNoEsCliente(rol);
        List<PedidoResponse> pedidos = pedidoService.listarPedidos()
                .stream()
                .map(pedidoService::toResponse)
                .toList();
        return ResponseEntity.ok(pedidos);
    }

    @Operation(summary = "Obtener un pedido por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado",
                    content = @Content(schema = @Schema(implementation = PedidoResponse.class))),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content)
    })
    @GetMapping("/{idPedido}")
    public ResponseEntity<PedidoResponse> obtenerPedido(
            @Parameter(description = "ID del pedido", example = "1", required = true) @PathVariable Long idPedido,
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol,
            @RequestHeader(value = "X-Id-Usuario", required = false) String idUsuario) {
        Pedido pedido = pedidoService.obtenerPedido(idPedido);
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            IdorValidator.verificarAccesoEntidadCliente(pedido.getIdCliente(), rol, idUsuario);
        }
        return ResponseEntity.ok(pedidoService.toResponse(pedido));
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
    public ResponseEntity<PedidoResponse> actualizarPedido(
            @Parameter(description = "ID del pedido", example = "1", required = true) @PathVariable Long idPedido,
            @Valid @RequestBody CrearPedidoRequest request,
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol,
            @RequestHeader(value = "X-Id-Usuario", required = false) String idUsuario) {
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            Pedido existente = pedidoService.obtenerPedido(idPedido);
            IdorValidator.verificarAccesoEntidadCliente(existente.getIdCliente(), rol, idUsuario);
        }
        Pedido pedido = pedidoService.actualizarPedido(idPedido, request);
        return ResponseEntity.ok(pedidoService.toResponse(pedido));
    }

    @Operation(summary = "Eliminar un pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pedido eliminado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content)
    })
    @DeleteMapping("/{idPedido}")
    public ResponseEntity<Void> eliminarPedido(
            @Parameter(description = "ID del pedido", example = "1", required = true) @PathVariable Long idPedido,
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol) {
        IdorValidator.verificarNoEsCliente(rol);
        pedidoService.eliminarPedido(idPedido);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Consultar el estado actual de un pedido")
    @ApiResponse(responseCode = "200", description = "Estado del pedido")
    @GetMapping("/{idPedido}/estado")
    public ResponseEntity<Map<String, Object>> consultarEstado(
            @Parameter(description = "ID del pedido", example = "1", required = true) @PathVariable Long idPedido,
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol,
            @RequestHeader(value = "X-Id-Usuario", required = false) String idUsuario) {
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            Pedido pedido = pedidoService.obtenerPedido(idPedido);
            IdorValidator.verificarAccesoEntidadCliente(pedido.getIdCliente(), rol, idUsuario);
        }
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
            @Parameter(description = "ID del pedido", example = "1", required = true) @PathVariable Long idPedido,
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol,
            @RequestHeader(value = "X-Id-Usuario", required = false) String idUsuario) {
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            Pedido pedido = pedidoService.obtenerPedido(idPedido);
            IdorValidator.verificarAccesoEntidadCliente(pedido.getIdCliente(), rol, idUsuario);
        }
        return ResponseEntity.ok(pedidoService.listarHistorialPedido(idPedido));
    }

    @Operation(summary = "Obtener el historial de pedidos de un cliente")
    @ApiResponse(responseCode = "200", description = "Historial de pedidos del cliente",
            content = @Content(schema = @Schema(implementation = PedidoResponse.class)))
    @GetMapping("/clientes/{idCliente}/historial")
    public ResponseEntity<List<PedidoResponse>> historialCliente(
            @Parameter(description = "ID del cliente", example = "10", required = true) @PathVariable Long idCliente,
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol,
            @RequestHeader(value = "X-Id-Usuario", required = false) String idUsuario) {
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            IdorValidator.verificarAccesoEntidadCliente(idCliente, rol, idUsuario);
        }
        List<PedidoResponse> pedidos = pedidoService.historialCliente(idCliente)
                .stream()
                .map(pedidoService::toResponse)
                .toList();
        return ResponseEntity.ok(pedidos);
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
    public ResponseEntity<PedidoResponse> cancelarPedido(
            @Parameter(description = "ID del pedido", example = "1", required = true) @PathVariable Long idPedido,
            @RequestBody(required = false) CancelarPedidoRequest request,
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol,
            @RequestHeader(value = "X-Id-Usuario", required = false) String idUsuario) {
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            Pedido existente = pedidoService.obtenerPedido(idPedido);
            IdorValidator.verificarAccesoEntidadCliente(existente.getIdCliente(), rol, idUsuario);
        }
        String motivo = request != null ? request.getMotivo() : null;
        Pedido pedido = pedidoService.cancelarPedido(idPedido, motivo);
        return ResponseEntity.ok(pedidoService.toResponse(pedido));
    }

    @Operation(summary = "Crear una reclamacion asociada a un pedido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reclamacion creada",
                    content = @Content(schema = @Schema(implementation = Reclamacion.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado", content = @Content)
    })
    @PostMapping("/{idPedido}/reclamaciones")
    public ResponseEntity<Reclamacion> crearReclamacion(
            @Parameter(description = "ID del pedido", example = "1", required = true) @PathVariable Long idPedido,
            @Valid @RequestBody CrearReclamacionRequest request,
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol,
            @RequestHeader(value = "X-Id-Usuario", required = false) String idUsuario) {
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            Pedido existente = pedidoService.obtenerPedido(idPedido);
            IdorValidator.verificarAccesoEntidadCliente(existente.getIdCliente(), rol, idUsuario);
        }
        Reclamacion reclamacion = pedidoService.crearReclamacionPorPedido(idPedido, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reclamacion);
    }

    @Operation(summary = "Listar las reclamaciones de un pedido")
    @ApiResponse(responseCode = "200", description = "Listado de reclamaciones",
            content = @Content(schema = @Schema(implementation = Reclamacion.class)))
    @GetMapping("/{idPedido}/reclamaciones")
    public ResponseEntity<List<Reclamacion>> listarReclamaciones(
            @Parameter(description = "ID del pedido", example = "1", required = true) @PathVariable Long idPedido,
            @RequestHeader(value = "X-Rol-Usuario", required = false) String rol,
            @RequestHeader(value = "X-Id-Usuario", required = false) String idUsuario) {
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            Pedido pedido = pedidoService.obtenerPedido(idPedido);
            IdorValidator.verificarAccesoEntidadCliente(pedido.getIdCliente(), rol, idUsuario);
        }
        return ResponseEntity.ok(pedidoService.listarReclamacionesPorPedido(idPedido));
    }
}