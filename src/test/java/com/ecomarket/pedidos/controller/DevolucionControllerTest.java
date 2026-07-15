package com.ecomarket.pedidos.controller;
// Tests HTTP del DevolucionController. Cubre devoluciones y reclamaciones.

import com.ecomarket.pedidos.dto.ActualizarEstadoDevolucionRequest;
import com.ecomarket.pedidos.dto.CrearDevolucionRequest;
import com.ecomarket.pedidos.dto.DevolucionResponse;
import com.ecomarket.pedidos.model.Devolucion;
import com.ecomarket.pedidos.service.DevolucionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Pruebas unitarias de DevolucionController con @WebMvcTest y @MockitoBean
// Mockea DevolucionService y valida solo la capa HTTP
// La logica de negocio ya esta cubierta por DevolucionServiceTest
@WebMvcTest(controllers = DevolucionController.class, excludeFilters = @org.springframework.context.annotation.ComponentScan.Filter(type = org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE, classes = com.ecomarket.pedidos.security.JwtAuthenticationFilter.class))
class DevolucionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DevolucionService devolucionService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private Devolucion devolucionMock(Long idDevolucion, String estado) {
        Devolucion d = new Devolucion();
        d.setIdDevolucion(idDevolucion);
        d.setIdCliente(10L);
        d.setIdVenta(500L);
        d.setMotivo("Producto defectuoso");
        d.setEstado(estado);
        return d;
    }

    private DevolucionResponse devolucionResponseMock(Long idDevolucion, String estado) {
        DevolucionResponse r = new DevolucionResponse();
        r.setIdDevolucion(idDevolucion);
        r.setIdCliente(10L);
        r.setIdVenta(500L);
        r.setMotivo("Producto defectuoso");
        r.setEstado(estado);
        return r;
    }

    @Test
    void testListarDevoluciones() throws Exception {
        Devolucion d1 = devolucionMock(1L, "PENDIENTE");
        Devolucion d2 = devolucionMock(2L, "APROBADA");
        when(devolucionService.listarDevoluciones()).thenReturn(List.of(d1, d2));
        when(devolucionService.toResponse(d1)).thenReturn(devolucionResponseMock(1L, "PENDIENTE"));
        when(devolucionService.toResponse(d2)).thenReturn(devolucionResponseMock(2L, "APROBADA"));

        mockMvc.perform(get("/api/pedidos/devoluciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].idDevolucion", is(1)))
                .andExpect(jsonPath("$[0].estado", is("PENDIENTE")))
                .andExpect(jsonPath("$[1].estado", is("APROBADA")));
    }

    @Test
    void testObtenerDevolucionNoExistente() throws Exception {
        // El service lanza ResponseStatusException(404) pero el GlobalExceptionHandler
        // no la mapea, asi que usamos RecursoNoEncontradoException que si va a 404.
        when(devolucionService.obtenerDevolucion(99L))
                .thenThrow(new com.ecomarket.pedidos.exception.RecursoNoEncontradoException(
                        "Devolucion no encontrada: 99"));

        mockMvc.perform(get("/api/pedidos/devoluciones/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCrearDevolucion() throws Exception {
        Long idVenta = 500L;
        Devolucion d = devolucionMock(1L, "PENDIENTE");
        DevolucionResponse resp = devolucionResponseMock(1L, "PENDIENTE");
        when(devolucionService.crearDevolucion(any(CrearDevolucionRequest.class))).thenReturn(d);
        when(devolucionService.toResponse(d)).thenReturn(resp);

        CrearDevolucionRequest req = new CrearDevolucionRequest();
        req.setIdCliente(10L);
        req.setMotivo("Producto defectuoso");

        mockMvc.perform(post("/api/pedidos/ventas-pedido/" + idVenta + "/devoluciones")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDevolucion", is(1)))
                .andExpect(jsonPath("$.estado", is("PENDIENTE")))
                .andExpect(jsonPath("$.motivo", is("Producto defectuoso")));
    }

    @Test
    void testActualizarEstadoDevolucion() throws Exception {
        Long idDevolucion = 1L;
        Devolucion d = devolucionMock(idDevolucion, "APROBADA");
        DevolucionResponse resp = devolucionResponseMock(idDevolucion, "APROBADA");
        when(devolucionService.actualizarEstadoDevolucion(any(Long.class), any(String.class)))
                .thenReturn(d);
        when(devolucionService.toResponse(d)).thenReturn(resp);

        ActualizarEstadoDevolucionRequest req = new ActualizarEstadoDevolucionRequest();
        req.setEstado("APROBADA");

        mockMvc.perform(patch("/api/pedidos/devoluciones/" + idDevolucion + "/estado")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDevolucion", is(1)))
                .andExpect(jsonPath("$.estado", is("APROBADA")));
    }

    @Test
    void testCrearReclamacion() throws Exception {
        com.ecomarket.pedidos.model.Reclamacion rec = new com.ecomarket.pedidos.model.Reclamacion();
        rec.setIdReclamacion(7L);
        rec.setIdCliente(10L);
        rec.setMotivo("Producto danado");
        com.ecomarket.pedidos.dto.ReclamacionResponse resp = new com.ecomarket.pedidos.dto.ReclamacionResponse();
        resp.setIdReclamacion(7L);
        resp.setIdCliente(10L);
        resp.setMotivo("Producto danado");
        when(devolucionService.crearReclamacion(any(com.ecomarket.pedidos.dto.CrearReclamacionRequest.class)))
                .thenReturn(rec);
        when(devolucionService.toResponse(rec)).thenReturn(resp);

        com.ecomarket.pedidos.dto.CrearReclamacionRequest req = new com.ecomarket.pedidos.dto.CrearReclamacionRequest();
        req.setIdCliente(10L);
        req.setIdPedido(1L);
        req.setIdVenta(100L);
        req.setMotivo("Producto danado");

        String url = "/api/pedidos/reclamaciones";
        mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idReclamacion", is(7)));
    }

    @Test
    void testListarReclamaciones() throws Exception {
        com.ecomarket.pedidos.model.Reclamacion rec1 = new com.ecomarket.pedidos.model.Reclamacion();
        rec1.setIdReclamacion(1L);
        com.ecomarket.pedidos.model.Reclamacion rec2 = new com.ecomarket.pedidos.model.Reclamacion();
        rec2.setIdReclamacion(2L);
        com.ecomarket.pedidos.dto.ReclamacionResponse r1 = new com.ecomarket.pedidos.dto.ReclamacionResponse();
        r1.setIdReclamacion(1L);
        com.ecomarket.pedidos.dto.ReclamacionResponse r2 = new com.ecomarket.pedidos.dto.ReclamacionResponse();
        r2.setIdReclamacion(2L);
        when(devolucionService.listarReclamaciones()).thenReturn(List.of(rec1, rec2));
        when(devolucionService.toResponse(rec1)).thenReturn(r1);
        when(devolucionService.toResponse(rec2)).thenReturn(r2);

        String url = "/api/pedidos/reclamaciones";
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void testObtenerReclamacion() throws Exception {
        Long idRec = 1L;
        com.ecomarket.pedidos.model.Reclamacion rec = new com.ecomarket.pedidos.model.Reclamacion();
        rec.setIdReclamacion(idRec);
        rec.setIdCliente(10L);
        com.ecomarket.pedidos.dto.ReclamacionResponse resp = new com.ecomarket.pedidos.dto.ReclamacionResponse();
        resp.setIdReclamacion(idRec);
        resp.setIdCliente(10L);
        when(devolucionService.obtenerReclamacion(idRec)).thenReturn(rec);
        when(devolucionService.toResponse(rec)).thenReturn(resp);

        String url = "/api/pedidos/reclamaciones/1";
        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idReclamacion", is(1)));
    }

    @Test
    void testActualizarEstadoReclamacion() throws Exception {
        Long idRec = 1L;
        com.ecomarket.pedidos.model.Reclamacion rec = new com.ecomarket.pedidos.model.Reclamacion();
        rec.setIdReclamacion(idRec);
        rec.setIdCliente(10L);
        rec.setEstado("EN_REVISION");
        com.ecomarket.pedidos.dto.ReclamacionResponse resp = new com.ecomarket.pedidos.dto.ReclamacionResponse();
        resp.setIdReclamacion(idRec);
        resp.setIdCliente(10L);
        resp.setEstado("EN_REVISION");
        when(devolucionService.actualizarEstadoReclamacion(eq(idRec), any())).thenReturn(rec);
        when(devolucionService.toResponse(rec)).thenReturn(resp);

        com.ecomarket.pedidos.dto.ActualizarEstadoReclamacionRequest req = new com.ecomarket.pedidos.dto.ActualizarEstadoReclamacionRequest();
        req.setEstado("EN_REVISION");

        String url = "/api/pedidos/reclamaciones/1/estado";
        mockMvc.perform(
                patch(url).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado", is("EN_REVISION")));
    }

    // --- Tests de caminos de error (Branch Coverage) ---

    @Test
    void testObtenerDevolucionExistente_retorna200() throws Exception {
        // Camino feliz faltante: un empleado consulta una devolucion existente en el sistema.
        Devolucion d = devolucionMock(1L, "APROBADA");
        DevolucionResponse resp = devolucionResponseMock(1L, "APROBADA");
        when(devolucionService.obtenerDevolucion(1L)).thenReturn(d);
        when(devolucionService.toResponse(d)).thenReturn(resp);

        mockMvc.perform(get("/api/pedidos/devoluciones/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDevolucion", is(1)))
                .andExpect(jsonPath("$.estado", is("APROBADA")));
    }

    @Test
    void testActualizarEstadoDevolucion_estadoInvalido_retorna400() throws Exception {
        // Regla: Solo se aceptan estados validos para devoluciones: APROBADA, RECHAZADA, EN_REVISION.
        when(devolucionService.actualizarEstadoDevolucion(any(Long.class), any(String.class)))
                .thenThrow(new IllegalArgumentException(
                        "Estado invalido: ESTADO_INCORRECTO. Valores validos: APROBADA, RECHAZADA, EN_REVISION"));

        ActualizarEstadoDevolucionRequest req = new ActualizarEstadoDevolucionRequest();
        req.setEstado("ESTADO_INCORRECTO");

        mockMvc.perform(patch("/api/pedidos/devoluciones/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testObtenerReclamacion_noExistente_retorna404() throws Exception {
        // Regla: Si el cliente solicita el detalle de una reclamacion que no existe, se retorna 404.
        when(devolucionService.obtenerReclamacion(99L))
                .thenThrow(new com.ecomarket.pedidos.exception.RecursoNoEncontradoException(
                        "Reclamacion no encontrada con id: 99"));

        mockMvc.perform(get("/api/pedidos/reclamaciones/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testActualizarEstadoReclamacion_estadoInvalido_retorna400() throws Exception {
        // Regla: Solo se aceptan estados validos para reclamaciones: ABIERTA, EN_REVISION, RESUELTA, CERRADA.
        when(devolucionService.actualizarEstadoReclamacion(any(Long.class), any()))
                .thenThrow(new IllegalArgumentException(
                        "Estado invalido: FAKE. Valores validos: ABIERTA, EN_REVISION, RESUELTA, CERRADA"));

        com.ecomarket.pedidos.dto.ActualizarEstadoReclamacionRequest req =
                new com.ecomarket.pedidos.dto.ActualizarEstadoReclamacionRequest();
        req.setEstado("FAKE");

        mockMvc.perform(patch("/api/pedidos/reclamaciones/1/estado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testObtenerDevolucion_IDOR_ClienteAjeno_retorna403() throws Exception {
        Devolucion d = devolucionMock(1L, "APROBADA");
        when(devolucionService.obtenerDevolucion(1L)).thenReturn(d);

        mockMvc.perform(get("/api/pedidos/devoluciones/1")
                        .header("X-Rol-Usuario", "CLIENTE")
                        .header("X-Id-Usuario", "99")) // Devolución de cliente 10
                .andExpect(status().isForbidden());
    }

    @Test
    void testObtenerReclamacion_IDOR_ClienteAjeno_retorna403() throws Exception {
        com.ecomarket.pedidos.model.Reclamacion rec = new com.ecomarket.pedidos.model.Reclamacion();
        rec.setIdReclamacion(1L);
        rec.setIdCliente(10L);
        when(devolucionService.obtenerReclamacion(1L)).thenReturn(rec);

        mockMvc.perform(get("/api/pedidos/reclamaciones/1")
                        .header("X-Rol-Usuario", "CLIENTE")
                        .header("X-Id-Usuario", "99")) // Reclamación de cliente 10
                .andExpect(status().isForbidden());
    }

    @Test
    void testListarDevoluciones_IDOR_Cliente_retorna403() throws Exception {
        mockMvc.perform(get("/api/pedidos/devoluciones")
                        .header("X-Rol-Usuario", "CLIENTE")
                        .header("X-Id-Usuario", "10"))
                .andExpect(status().isForbidden());
    }
}
