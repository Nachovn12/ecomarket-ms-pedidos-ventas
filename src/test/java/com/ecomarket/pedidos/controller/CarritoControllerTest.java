package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.AgregarItemCarritoRequest;
import com.ecomarket.pedidos.dto.ActualizarCantidadRequest;
import com.ecomarket.pedidos.dto.AplicarCuponRequest;
import com.ecomarket.pedidos.dto.AplicarCuponResponse;
import com.ecomarket.pedidos.dto.CarritoResponse;
import com.ecomarket.pedidos.dto.CrearCarritoRequest;
import com.ecomarket.pedidos.exception.RecursoNoEncontradoException;
import com.ecomarket.pedidos.model.CarritoCompra;
import com.ecomarket.pedidos.model.EstadoCarrito;
import com.ecomarket.pedidos.service.CarritoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.eq;

// Tests HTTP del CarritoController. La logica esta en CarritoServiceTest.
@WebMvcTest(CarritoController.class)
class CarritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarritoService carritoService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private CarritoCompra carritoMock(Long idCarrito, Long idCliente, EstadoCarrito estado) {
        CarritoCompra c = new CarritoCompra();
        c.setIdCarrito(idCarrito);
        c.setIdCliente(idCliente);
        c.setEstado(estado);
        c.setSubtotal(0.0);
        c.setDescuentoAplicado(0.0);
        c.setTotal(0.0);
        c.setItems(new ArrayList<>());
        return c;
    }

    private CarritoResponse carritoResponseMock(Long idCarrito, Long idCliente, EstadoCarrito estado) {
        CarritoResponse r = new CarritoResponse();
        r.setIdCarrito(idCarrito);
        r.setIdCliente(idCliente);
        r.setEstado(estado);
        r.setSubtotal(0.0);
        r.setDescuentoAplicado(0.0);
        r.setTotal(0.0);
        r.setItems(new ArrayList<>());
        return r;
    }
    @Test
    void testListarTodos() throws Exception {
        CarritoCompra c1 = carritoMock(1L, 10L, EstadoCarrito.ACTIVO);
        CarritoCompra c2 = carritoMock(2L, 11L, EstadoCarrito.CONVERTIDO);
        when(carritoService.listarCarritos()).thenReturn(List.of(c1, c2));
        when(carritoService.toResponse(c1)).thenReturn(carritoResponseMock(1L, 10L, EstadoCarrito.ACTIVO));
        when(carritoService.toResponse(c2)).thenReturn(carritoResponseMock(2L, 11L, EstadoCarrito.CONVERTIDO));

        mockMvc.perform(get("/api/pedidos/carritos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].idCarrito", is(1)))
                .andExpect(jsonPath("$[0].estado", is("ACTIVO")))
                .andExpect(jsonPath("$[1].idCarrito", is(2)))
                .andExpect(jsonPath("$[1].estado", is("CONVERTIDO")));
    }

    @Test
    void testObtenerPorIdExistente() throws Exception {
        CarritoCompra c = carritoMock(1L, 10L, EstadoCarrito.ACTIVO);
        when(carritoService.obtenerCarrito(1L)).thenReturn(c);
        when(carritoService.toResponse(c)).thenReturn(carritoResponseMock(1L, 10L, EstadoCarrito.ACTIVO));

        mockMvc.perform(get("/api/pedidos/carritos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCarrito", is(1)))
                .andExpect(jsonPath("$.idCliente", is(10)))
                .andExpect(jsonPath("$.estado", is("ACTIVO")));
    }

    @Test
    void testObtenerPorIdNoExistente() throws Exception {
        when(carritoService.obtenerCarrito(99L))
                .thenThrow(new RecursoNoEncontradoException("Carrito no encontrado con id: 99"));

        mockMvc.perform(get("/api/pedidos/carritos/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCrearCarrito() throws Exception {
        Long idCliente = 10L;
        CarritoCompra c = carritoMock(1L, idCliente, EstadoCarrito.ACTIVO);
        when(carritoService.crearCarrito(idCliente)).thenReturn(c);
        when(carritoService.toResponse(c)).thenReturn(carritoResponseMock(1L, idCliente, EstadoCarrito.ACTIVO));

        CrearCarritoRequest req = new CrearCarritoRequest();
        req.setIdCliente(idCliente);

        mockMvc.perform(post("/api/pedidos/carritos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCarrito", is(1)))
                .andExpect(jsonPath("$.idCliente", is(10)))
                .andExpect(jsonPath("$.estado", is("ACTIVO")));
    }

    @Test
    void testAgregarItem() throws Exception {
        Long idCarrito = 1L;
        CarritoCompra c = carritoMock(idCarrito, 10L, EstadoCarrito.ACTIVO);
        c.setSubtotal(3980.0);
        c.setTotal(3980.0);
        when(carritoService.agregarItem(anyLong(), any(AgregarItemCarritoRequest.class))).thenReturn(c);
        when(carritoService.toResponse(c)).thenReturn(carritoResponseMock(idCarrito, 10L, EstadoCarrito.ACTIVO));

        AgregarItemCarritoRequest req = new AgregarItemCarritoRequest();
        req.setIdProducto(100L);
        req.setNombreProducto("Bolsa biodegradable");
        req.setCantidad(2);
        req.setPrecioUnitario(1990.0);
        req.setStockDisponible(50);

        mockMvc.perform(post("/api/pedidos/carritos/" + idCarrito + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCarrito", is(1)))
                .andExpect(jsonPath("$.estado", is("ACTIVO")));
    }

    @Test
    void testActualizarCantidadItem() throws Exception {
        Long idCarrito = 1L;
        Long idItem = 10L;
        com.ecomarket.pedidos.model.CarritoCompra carrito = new com.ecomarket.pedidos.model.CarritoCompra();
        carrito.setIdCarrito(idCarrito);
        carrito.setIdCliente(10L);
        carrito.setEstado(com.ecomarket.pedidos.model.EstadoCarrito.ACTIVO);
        carrito.recalcularTotales();
        CarritoResponse resp = new CarritoResponse();
        resp.setIdCarrito(idCarrito);
        resp.setIdCliente(10L);
        resp.setEstado(com.ecomarket.pedidos.model.EstadoCarrito.ACTIVO);
        when(carritoService.actualizarCantidad(eq(idCarrito), eq(idItem), any(ActualizarCantidadRequest.class))).thenReturn(carrito);
        when(carritoService.toResponse(carrito)).thenReturn(resp);

        ActualizarCantidadRequest req = new ActualizarCantidadRequest();
        req.setCantidad(3);
        req.setStockDisponible(10);

        String url = "/api/pedidos/carritos/1/items/10";
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(url).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCarrito", is(1)));
    }

    @Test
    void testEliminarItem() throws Exception {
        Long idCarrito = 1L;
        Long idItem = 10L;
        com.ecomarket.pedidos.model.CarritoCompra carrito = new com.ecomarket.pedidos.model.CarritoCompra();
        carrito.setIdCarrito(idCarrito);
        carrito.setIdCliente(10L);
        carrito.setEstado(com.ecomarket.pedidos.model.EstadoCarrito.ACTIVO);
        carrito.recalcularTotales();
        CarritoResponse resp = new CarritoResponse();
        resp.setIdCarrito(idCarrito);
        resp.setIdCliente(10L);
        resp.setEstado(com.ecomarket.pedidos.model.EstadoCarrito.ACTIVO);
        when(carritoService.eliminarItem(idCarrito, idItem)).thenReturn(carrito);
        when(carritoService.toResponse(carrito)).thenReturn(resp);

        String url = "/api/pedidos/carritos/1/items/10";
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCarrito", is(1)));
    }

    // --- Tests de caminos de error (Branch Coverage) ---

    @Test
    void testAgregarItem_carritoNoExistente_retorna404() throws Exception {
        // Regla: No se puede agregar un producto a un carrito que no existe.
        when(carritoService.agregarItem(anyLong(), any(AgregarItemCarritoRequest.class)))
                .thenThrow(new RecursoNoEncontradoException("Carrito no encontrado con id: 99"));

        AgregarItemCarritoRequest req = new AgregarItemCarritoRequest();
        req.setIdProducto(1L);
        req.setNombreProducto("Bolsa biodegradable");
        req.setCantidad(1);
        req.setPrecioUnitario(1990.0);
        req.setStockDisponible(10);

        mockMvc.perform(post("/api/pedidos/carritos/99/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAgregarItem_stockInsuficiente_retorna409() throws Exception {
        // Regla: Un cliente no puede agregar al carrito mas unidades que el stock disponible del producto.
        when(carritoService.agregarItem(anyLong(), any(AgregarItemCarritoRequest.class)))
                .thenThrow(new com.ecomarket.pedidos.exception.StockInsuficienteException(
                        "Stock insuficiente para producto id=100. Disponible: 3, solicitado: 10"));

        AgregarItemCarritoRequest req = new AgregarItemCarritoRequest();
        req.setIdProducto(100L);
        req.setNombreProducto("Bolsa biodegradable");
        req.setCantidad(10);
        req.setPrecioUnitario(1990.0);
        req.setStockDisponible(3);

        mockMvc.perform(post("/api/pedidos/carritos/1/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());
    }

    @Test
    void testActualizarCantidad_itemNoExistente_retorna404() throws Exception {
        // Regla: No se puede modificar un item que no pertenece al carrito del cliente.
        when(carritoService.actualizarCantidad(anyLong(), anyLong(), any(ActualizarCantidadRequest.class)))
                .thenThrow(new RecursoNoEncontradoException("Item no encontrado con id: 99 en carrito: 1"));

        ActualizarCantidadRequest req = new ActualizarCantidadRequest();
        req.setCantidad(2);
        req.setStockDisponible(10);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/pedidos/carritos/1/items/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testActualizarCantidad_cantidadNegativa_retorna400() throws Exception {
        // Regla: La cantidad de un item en el carrito debe ser mayor a cero.
        when(carritoService.actualizarCantidad(anyLong(), anyLong(), any(ActualizarCantidadRequest.class)))
                .thenThrow(new IllegalArgumentException("La cantidad debe ser mayor a cero"));

        ActualizarCantidadRequest req = new ActualizarCantidadRequest();
        req.setCantidad(-1);
        req.setStockDisponible(10);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .put("/api/pedidos/carritos/1/items/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testEliminarItem_itemNoExistente_retorna404() throws Exception {
        // Regla: No se puede eliminar un item que no existe en el carrito del cliente.
        when(carritoService.eliminarItem(anyLong(), anyLong()))
                .thenThrow(new RecursoNoEncontradoException("Item no encontrado con id: 99 en carrito: 1"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/api/pedidos/carritos/1/items/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAplicarCupon_cuponInvalido_retorna400() throws Exception {
        // Regla: Un cupon invalido, expirado o ya utilizado no debe aplicarse al carrito.
        when(carritoService.aplicarCupon(anyLong(), any()))
                .thenThrow(new IllegalArgumentException("Cupon ECOXXXX es invalido o ha expirado"));

        AplicarCuponRequest req = new AplicarCuponRequest();
        req.setCodigo("ECOXXXX");

        mockMvc.perform(post("/api/pedidos/carritos/1/cupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAplicarCupon_cuponValido_retorna200() throws Exception {
        // Regla: Un cliente con cupón válido recibe descuento en su carrito.
        AplicarCuponResponse cuponResp = new AplicarCuponResponse();
        cuponResp.setCodigo("ECO10");
        cuponResp.setSubtotal(5000.0);
        cuponResp.setDescuento(500.0);
        cuponResp.setTotalFinal(4500.0);
        when(carritoService.aplicarCupon(eq(1L), eq("ECO10"))).thenReturn(cuponResp);

        AplicarCuponRequest req = new AplicarCuponRequest();
        req.setCodigo("ECO10");

        mockMvc.perform(post("/api/pedidos/carritos/1/cupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is("ECO10")))
                .andExpect(jsonPath("$.descuento", is(500.0)));
    }
}
