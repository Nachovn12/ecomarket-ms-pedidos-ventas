package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.CrearPedidoRequest;
import com.ecomarket.pedidos.dto.PedidoResponse;
import com.ecomarket.pedidos.exception.RecursoNoEncontradoException;
import com.ecomarket.pedidos.model.EstadoPedido;
import com.ecomarket.pedidos.model.MetodoPago;
import com.ecomarket.pedidos.model.Pedido;
import com.ecomarket.pedidos.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Tests HTTP del PedidoController. La logica esta en PedidoServiceTest.
@WebMvcTest(PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PedidoService pedidoService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Pedido pedidoMock(Long idPedido, Long idCliente, EstadoPedido estado,
                                Double subtotal, Double iva, Double total) {
        Pedido p = new Pedido();
        p.setIdPedido(idPedido);
        p.setIdCliente(idCliente);
        p.setEstado(estado);
        p.setMetodoPago(MetodoPago.TARJETA);
        p.setSubtotal(subtotal);
        p.setDescuento(0.0);
        p.setIva(iva);
        p.setTotal(total);
        p.setDireccionEntrega("Av. Siempre Viva 742, Santiago");
        p.setObservaciones("Test");
        return p;
    }

    private PedidoResponse pedidoResponseMock(Long idPedido, Long idCliente, EstadoPedido estado,
                                                Double subtotal, Double iva, Double total) {
        PedidoResponse r = new PedidoResponse();
        r.setIdPedido(idPedido);
        r.setIdCliente(idCliente);
        r.setEstado(estado);
        r.setMetodoPago(MetodoPago.TARJETA);
        r.setSubtotal(subtotal);
        r.setDescuento(0.0);
        r.setIva(iva);
        r.setTotal(total);
        r.setDireccionEntrega("Av. Siempre Viva 742, Santiago");
        r.setObservaciones("Test");
        return r;
    }
    @Test
    void testListarTodos() throws Exception {
        Pedido p1 = pedidoMock(1L, 10L, EstadoPedido.PENDIENTE, 1000.0, 190.0, 1000.0);
        Pedido p2 = pedidoMock(2L, 11L, EstadoPedido.EN_PREPARACION, 2000.0, 380.0, 2000.0);
        when(pedidoService.listarPedidos()).thenReturn(List.of(p1, p2));
        when(pedidoService.toResponse(p1)).thenReturn(pedidoResponseMock(1L, 10L, EstadoPedido.PENDIENTE, 1000.0, 190.0, 1000.0));
        when(pedidoService.toResponse(p2)).thenReturn(pedidoResponseMock(2L, 11L, EstadoPedido.EN_PREPARACION, 2000.0, 380.0, 2000.0));

        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].idPedido", is(1)))
                .andExpect(jsonPath("$[0].estado", is("PENDIENTE")))
                .andExpect(jsonPath("$[1].idPedido", is(2)));
    }

    @Test
    void testObtenerPorIdExistente() throws Exception {
        Pedido p = pedidoMock(1L, 10L, EstadoPedido.PENDIENTE, 1000.0, 190.0, 1000.0);
        when(pedidoService.obtenerPedido(1L)).thenReturn(p);
        when(pedidoService.toResponse(p)).thenReturn(pedidoResponseMock(1L, 10L, EstadoPedido.PENDIENTE, 1000.0, 190.0, 1000.0));

        mockMvc.perform(get("/api/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPedido", is(1)))
                .andExpect(jsonPath("$.idCliente", is(10)))
                .andExpect(jsonPath("$.estado", is("PENDIENTE")))
                .andExpect(jsonPath("$.subtotal", is(1000.0)))
                .andExpect(jsonPath("$.iva", is(190.0)));
    }

    @Test
    void testObtenerPorIdNoExistente() throws Exception {
        when(pedidoService.obtenerPedido(99L))
                .thenThrow(new RecursoNoEncontradoException("Pedido no encontrado: 99"));

        mockMvc.perform(get("/api/pedidos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCrearDesdeCarrito() throws Exception {
        Long idCarrito = 1L;
        Pedido p = pedidoMock(99L, 10L, EstadoPedido.PENDIENTE, 1000.0, 190.0, 1000.0);
        PedidoResponse resp = pedidoResponseMock(99L, 10L, EstadoPedido.PENDIENTE, 1000.0, 190.0, 1000.0);
        when(pedidoService.crearDesdeCarrito(eq(idCarrito), any(CrearPedidoRequest.class)))
                .thenReturn(p);
        when(pedidoService.toResponse(p)).thenReturn(resp);

        CrearPedidoRequest req = new CrearPedidoRequest();
        req.setIdCliente(10L);
        req.setMetodoPago(MetodoPago.TARJETA);
        req.setDireccionEntrega("Av. Siempre Viva 742, Santiago");
        req.setObservaciones("Test");

        mockMvc.perform(post("/api/pedidos/desde-carrito/" + idCarrito)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idPedido", is(99)))
                .andExpect(jsonPath("$.estado", is("PENDIENTE")));
    }

    @Test
    void testCancelarPedido() throws Exception {
        Long idPedido = 1L;
        Pedido cancelado = pedidoMock(idPedido, 10L, EstadoPedido.CANCELADO, 1000.0, 190.0, 1000.0);
        cancelado.setObservaciones("Cancelado: Cliente solicito cancelacion");
        PedidoResponse respCancelado = pedidoResponseMock(idPedido, 10L, EstadoPedido.CANCELADO, 1000.0, 190.0, 1000.0);
        respCancelado.setObservaciones("Cancelado: Cliente solicito cancelacion");
        when(pedidoService.cancelarPedido(eq(idPedido), any())).thenReturn(cancelado);
        when(pedidoService.toResponse(cancelado)).thenReturn(respCancelado);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/pedidos/" + idPedido + "/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Cliente solicito cancelacion\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPedido", is(1)))
                .andExpect(jsonPath("$.estado", is("CANCELADO")));
    }

    @Test
    void testActualizarPedido() throws Exception {
        Pedido actualizado = pedidoMock(1L, 10L, EstadoPedido.PENDIENTE, 1000.0, 190.0, 1000.0);
        PedidoResponse resp = pedidoResponseMock(1L, 10L, EstadoPedido.PENDIENTE, 1000.0, 190.0, 1000.0);
        when(pedidoService.actualizarPedido(eq(1L), any(CrearPedidoRequest.class))).thenReturn(actualizado);
        when(pedidoService.toResponse(actualizado)).thenReturn(resp);

        CrearPedidoRequest req = new CrearPedidoRequest();
        req.setIdCliente(10L);
        req.setMetodoPago(MetodoPago.TARJETA);
        req.setDireccionEntrega("");
        req.setObservaciones("");

        String url = "/api/pedidos/1";
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPedido", is(1)));
    }

    @Test
    void testEliminarPedido() throws Exception {
        doNothing().when(pedidoService).eliminarPedido(1L);

        String url = "/api/pedidos/1";
        mockMvc.perform(delete(url))
                .andExpect(status().isNoContent());
    }

    @Test
    void testConsultarEstado() throws Exception {
        when(pedidoService.consultarEstado(1L)).thenReturn(EstadoPedido.EN_PREPARACION);

        String url = "/api/pedidos/1/estado";
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("EN_PREPARACION")));
    }

    @Test
    void testListarHistorial() throws Exception {
        com.ecomarket.pedidos.model.HistorialPedido h1 = new com.ecomarket.pedidos.model.HistorialPedido();
        h1.setIdHistorial(1L);
        h1.setIdPedido(1L);
        h1.setEstadoAnterior(EstadoPedido.PENDIENTE);
        h1.setEstadoNuevo(EstadoPedido.EN_PREPARACION);
        when(pedidoService.listarHistorialPedido(1L)).thenReturn(List.of(h1));

        String url = "/api/pedidos/1/historial";
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void testHistorialCliente() throws Exception {
        Pedido p1 = pedidoMock(1L, 10L, EstadoPedido.PENDIENTE, 1000.0, 190.0, 1000.0);
        PedidoResponse r1 = pedidoResponseMock(1L, 10L, EstadoPedido.PENDIENTE, 1000.0, 190.0, 1000.0);
        when(pedidoService.historialCliente(10L)).thenReturn(List.of(p1));
        when(pedidoService.toResponse(p1)).thenReturn(r1);

        String url = "/api/pedidos/clientes/10/historial";
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idPedido", is(1)));
    }

    @Test
    void testCrearReclamacion() throws Exception {
        com.ecomarket.pedidos.model.Reclamacion rec = new com.ecomarket.pedidos.model.Reclamacion();
        rec.setIdReclamacion(7L);
        rec.setIdPedido(1L);
        rec.setIdCliente(10L);
        rec.setMotivo("Producto danado");
        when(pedidoService.crearReclamacionPorPedido(eq(1L), any(com.ecomarket.pedidos.dto.CrearReclamacionRequest.class))).thenReturn(rec);

        com.ecomarket.pedidos.dto.CrearReclamacionRequest req = new com.ecomarket.pedidos.dto.CrearReclamacionRequest();
        req.setIdCliente(10L);
        req.setIdPedido(1L);
        req.setMotivo("Producto danado");

        String url = "/api/pedidos/1/reclamaciones";
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idReclamacion", is(7)));
    }

    // --- Tests de caminos de error (Branch Coverage) ---

    @Test
    void testCrearDesdeCarrito_carritoNoExistente_retorna404() throws Exception {
        // Regla: No se puede generar un pedido desde un carrito que no existe en el sistema.
        Long idCarrito = 99L;
        when(pedidoService.crearDesdeCarrito(eq(idCarrito), any(CrearPedidoRequest.class)))
                .thenThrow(new RecursoNoEncontradoException("Carrito no encontrado con id: 99"));

        CrearPedidoRequest req = new CrearPedidoRequest();
        req.setIdCliente(10L);
        req.setMetodoPago(MetodoPago.TARJETA);
        req.setDireccionEntrega("Av. Siempre Viva 742, Santiago");

        mockMvc.perform(post("/api/pedidos/desde-carrito/" + idCarrito)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCancelarPedido_estadoNoPermitido_retorna409() throws Exception {
        // Regla: Solo se pueden cancelar pedidos en estado PENDIENTE.
        // Un pedido ya ENTREGADO no puede ser cancelado.
        Long idPedido = 5L;
        when(pedidoService.cancelarPedido(eq(idPedido), any()))
                .thenThrow(new IllegalStateException(
                        "Solo se pueden cancelar pedidos en estado PENDIENTE. Estado actual: ENTREGADO"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/pedidos/" + idPedido + "/cancelar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"intento invalido\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void testActualizarPedido_estadoCancelado_retorna400() throws Exception {
        // Regla: No se pueden modificar pedidos que ya fueron cancelados.
        when(pedidoService.actualizarPedido(eq(99L), any(CrearPedidoRequest.class)))
                .thenThrow(new IllegalArgumentException(
                        "No se puede modificar un pedido en estado CANCELADO"));

        CrearPedidoRequest req = new CrearPedidoRequest();
        req.setIdCliente(10L);
        req.setMetodoPago(MetodoPago.EFECTIVO);
        req.setDireccionEntrega("");

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/pedidos/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testConsultarEstado_pedidoNoExistente_retorna400() throws Exception {
        // Regla: No se puede consultar el estado de un pedido que no existe.
        when(pedidoService.consultarEstado(99L))
                .thenThrow(new IllegalArgumentException("Pedido no encontrado con id: 99"));

        mockMvc.perform(get("/api/pedidos/99/estado"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCrearDesdeCarrito_stockInsuficiente_retorna409() throws Exception {
        // Regla: Si al confirmar el pedido un producto no tiene stock suficiente, se rechaza la operacion.
        Long idCarrito = 1L;
        when(pedidoService.crearDesdeCarrito(eq(idCarrito), any(CrearPedidoRequest.class)))
                .thenThrow(new com.ecomarket.pedidos.exception.StockInsuficienteException(
                        "Stock insuficiente para 'Bolsa biodegradable'. Disponible: 1, requerido: 5"));

        CrearPedidoRequest req = new CrearPedidoRequest();
        req.setIdCliente(10L);
        req.setMetodoPago(MetodoPago.TARJETA);
        req.setDireccionEntrega("Av. Siempre Viva 742, Santiago");

        mockMvc.perform(post("/api/pedidos/desde-carrito/" + idCarrito)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void testCancelarPedido_sinMotivo_retorna200() throws Exception {
        // Regla: Si el cliente cancela el pedido pero no provee un motivo explícito,
        // el sistema lo permite y asume motivo null (o predeterminado en el service).
        Long idPedido = 1L;
        Pedido cancelado = pedidoMock(idPedido, 10L, EstadoPedido.CANCELADO, 1000.0, 190.0, 1000.0);
        PedidoResponse respCancelado = pedidoResponseMock(idPedido, 10L, EstadoPedido.CANCELADO, 1000.0, 190.0, 1000.0);
        
        when(pedidoService.cancelarPedido(eq(idPedido), eq(null))).thenReturn(cancelado);
        when(pedidoService.toResponse(cancelado)).thenReturn(respCancelado);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/pedidos/" + idPedido + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("CANCELADO")));
    }

    @Test
    void testListarReclamaciones_deUnPedido() throws Exception {
        // Regla: El cliente o empleado puede consultar todas las reclamaciones asociadas a un pedido específico.
        com.ecomarket.pedidos.model.Reclamacion rec = new com.ecomarket.pedidos.model.Reclamacion();
        rec.setIdReclamacion(7L);
        rec.setIdPedido(1L);
        rec.setMotivo("Producto llego aplastado");
        
        when(pedidoService.listarReclamacionesPorPedido(1L)).thenReturn(List.of(rec));

        mockMvc.perform(get("/api/pedidos/1/reclamaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].motivo", is("Producto llego aplastado")));
    }
}
