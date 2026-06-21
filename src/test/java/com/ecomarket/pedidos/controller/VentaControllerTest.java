package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.CrearVentaRequest;
import com.ecomarket.pedidos.dto.FacturaResponse;
import com.ecomarket.pedidos.dto.ItemVentaRequest;
import com.ecomarket.pedidos.dto.VentaResponse;
import com.ecomarket.pedidos.model.MetodoPago;
import com.ecomarket.pedidos.model.Venta;
import com.ecomarket.pedidos.service.VentaService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

// Tests HTTP del VentaController. La logica de venta esta en VentaServiceTest.
@WebMvcTest(VentaController.class)
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VentaService ventaService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Venta ventaMock(Long idVenta, Long idCliente, Double subtotal, Double iva, Double total) {
        Venta v = new Venta();
        v.setIdVenta(idVenta);
        v.setIdCliente(idCliente);
        v.setMetodoPago(MetodoPago.EFECTIVO);
        v.setSubtotal(subtotal);
        v.setDescuento(0.0);
        v.setIva(iva);
        v.setTotal(total);
        v.setObservaciones("Cliente retira en tienda");
        return v;
    }

    private VentaResponse ventaResponseMock(Long idVenta, Long idCliente,
                                             Double subtotal, Double iva, Double total) {
        VentaResponse r = new VentaResponse();
        r.setIdVenta(idVenta);
        r.setIdCliente(idCliente);
        r.setMetodoPago(MetodoPago.EFECTIVO);
        r.setSubtotal(subtotal);
        r.setDescuento(0.0);
        r.setIva(iva);
        r.setTotal(total);
        r.setObservaciones("Cliente retira en tienda");
        return r;
    }

    private CrearVentaRequest ventaRequest(Long idCliente) {
        CrearVentaRequest req = new CrearVentaRequest();
        req.setIdCliente(idCliente);
        req.setMetodoPago(MetodoPago.EFECTIVO);
        ItemVentaRequest it = new ItemVentaRequest();
        it.setIdProducto(1L);
        it.setNombreProducto("Bolsa biodegradable");
        it.setCantidad(2);
        it.setPrecioUnitario(1990.0);
        req.setItems(List.of(it));
        return req;
    }
    @Test
    void testListarTodos() throws Exception {
        Venta v1 = ventaMock(1L, 10L, 3980.0, 756.2, 3980.0);
        Venta v2 = ventaMock(2L, 11L, 1000.0, 190.0, 1000.0);
        when(ventaService.listarVentas()).thenReturn(List.of(v1, v2));
        when(ventaService.toResponse(v1)).thenReturn(ventaResponseMock(1L, 10L, 3980.0, 756.2, 3980.0));
        when(ventaService.toResponse(v2)).thenReturn(ventaResponseMock(2L, 11L, 1000.0, 190.0, 1000.0));

        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].idVenta", is(1)))
                .andExpect(jsonPath("$[0].subtotal", is(3980.0)))
                .andExpect(jsonPath("$[1].idVenta", is(2)));
    }

    @Test
    void testObtenerPorIdExistente() throws Exception {
        Venta v = ventaMock(1L, 10L, 3980.0, 756.2, 3980.0);
        when(ventaService.obtenerVenta(1L)).thenReturn(v);
        when(ventaService.toResponse(v)).thenReturn(ventaResponseMock(1L, 10L, 3980.0, 756.2, 3980.0));

        mockMvc.perform(get("/api/ventas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idVenta", is(1)))
                .andExpect(jsonPath("$.idCliente", is(10)))
                .andExpect(jsonPath("$.metodoPago", is("EFECTIVO")))
                .andExpect(jsonPath("$.subtotal", is(3980.0)))
                .andExpect(jsonPath("$.iva", is(756.2)));
    }

    @Test
    void testObtenerPorIdNoExistente() throws Exception {
        when(ventaService.obtenerVenta(99L))
                .thenThrow(new IllegalArgumentException("Venta no encontrada: 99"));

        mockMvc.perform(get("/api/ventas/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRegistrarVentaPresencial() throws Exception {
        CrearVentaRequest req = ventaRequest(10L);
        Venta v = ventaMock(99L, 10L, 3980.0, 756.2, 3980.0);
        VentaResponse resp = ventaResponseMock(99L, 10L, 3980.0, 756.2, 3980.0);
        when(ventaService.registrarVentaPresencial(any(CrearVentaRequest.class))).thenReturn(v);
        when(ventaService.toResponse(v)).thenReturn(resp);

        mockMvc.perform(post("/api/ventas/presencial")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idVenta", is(99)))
                .andExpect(jsonPath("$.idCliente", is(10)))
                .andExpect(jsonPath("$.subtotal", is(3980.0)));
    }

    @Test
    void testActualizarVenta() throws Exception {
        Long idVenta = 1L;
        Venta v = ventaMock(idVenta, 10L, 1000.0, 190.0, 1000.0);
        VentaResponse resp = ventaResponseMock(idVenta, 10L, 1000.0, 190.0, 1000.0);
        when(ventaService.actualizarVenta(eq(idVenta), any(CrearVentaRequest.class))).thenReturn(v);
        when(ventaService.toResponse(v)).thenReturn(resp);

        CrearVentaRequest req = new CrearVentaRequest();
        req.setIdCliente(10L);
        req.setMetodoPago(com.ecomarket.pedidos.model.MetodoPago.EFECTIVO);
        com.ecomarket.pedidos.dto.ItemVentaRequest it = new com.ecomarket.pedidos.dto.ItemVentaRequest();
        it.setIdProducto(1L);
        it.setCantidad(1);
        it.setNombreProducto("Bolsa"); it.setPrecioUnitario(1000.0);
        req.setItems(List.of(it));

        String url = "/api/ventas/1";
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idVenta", is(1)));
    }

    @Test
    void testEliminarVenta() throws Exception {
        doNothing().when(ventaService).eliminarVenta(1L);

        String url = "/api/ventas/1";
        mockMvc.perform(delete(url))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGenerarFactura() throws Exception {
        Long idVenta = 1L;
        com.ecomarket.pedidos.model.Factura f = new com.ecomarket.pedidos.model.Factura();
        f.setIdFactura(10L);
        f.setFolio(1001);
        FacturaResponse resp = new FacturaResponse();
        resp.setIdFactura(10L);
        resp.setFolio(1001);
        when(ventaService.generarFactura(eq(idVenta), any(com.ecomarket.pedidos.dto.CrearFacturaRequest.class))).thenReturn(f);
        when(ventaService.toResponse(f)).thenReturn(resp);

        com.ecomarket.pedidos.dto.CrearFacturaRequest req = new com.ecomarket.pedidos.dto.CrearFacturaRequest();
        req.setIdCliente(10L);
        req.setRutCliente("12.345.678-9");
        req.setRazonSocial("EcoMarket SpA");

        String url = "/api/ventas/1/factura";
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idFactura", is(10)))
                .andExpect(jsonPath("$.folio", is(1001)));
    }

    @Test
    void testObtenerFactura() throws Exception {
        Long idFactura = 10L;
        com.ecomarket.pedidos.model.Factura f = new com.ecomarket.pedidos.model.Factura();
        f.setIdFactura(idFactura);
        f.setFolio(1001);
        FacturaResponse resp = new FacturaResponse();
        resp.setIdFactura(idFactura);
        resp.setFolio(1001);
        when(ventaService.obtenerFactura(idFactura)).thenReturn(f);
        when(ventaService.toResponse(f)).thenReturn(resp);

        String url = "/api/ventas/facturas/10";
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idFactura", is(10)))
                .andExpect(jsonPath("$.folio", is(1001)));
    }
}
