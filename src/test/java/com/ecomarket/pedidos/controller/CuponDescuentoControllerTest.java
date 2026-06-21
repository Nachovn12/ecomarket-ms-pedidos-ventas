package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.CuponDescuentoResponse;
import com.ecomarket.pedidos.model.CuponDescuento;
import com.ecomarket.pedidos.model.TipoDescuento;
import com.ecomarket.pedidos.service.CuponDescuentoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Tests HTTP del CuponDescuentoController. La logica esta en CuponDescuentoServiceTest.
@WebMvcTest(CuponDescuentoController.class)
class CuponDescuentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CuponDescuentoService cuponDescuentoService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private CuponDescuento cuponMock(String codigo, TipoDescuento tipo, Double valor) {
        CuponDescuento c = new CuponDescuento();
        c.setCodigo(codigo);
        c.setTipoDescuento(tipo);
        c.setValorDescuento(valor);
        c.setFechaVencimiento(LocalDate.now().plusMonths(3));
        c.setActivo(true);
        return c;
    }

    private CuponDescuentoResponse cuponResponseMock(Long idCupon, String codigo,
                                                      TipoDescuento tipo, Double valor) {
        CuponDescuentoResponse r = new CuponDescuentoResponse();
        r.setIdCupon(idCupon);
        r.setCodigo(codigo);
        r.setTipoDescuento(tipo);
        r.setValorDescuento(valor);
        r.setFechaVencimiento(LocalDate.now().plusMonths(3));
        r.setActivo(true);
        return r;
    }
    @Test
    void testCrearCupon_OK() throws Exception {
        CuponDescuento input = cuponMock("ECO10", TipoDescuento.PORCENTAJE, 10.0);
        CuponDescuento guardado = cuponMock("ECO10", TipoDescuento.PORCENTAJE, 10.0);
        guardado.setIdCupon(1L);
        CuponDescuentoResponse resp = cuponResponseMock(1L, "ECO10", TipoDescuento.PORCENTAJE, 10.0);

        when(cuponDescuentoService.crearCupon(any(CuponDescuento.class))).thenReturn(guardado);
        when(cuponDescuentoService.toResponse(guardado)).thenReturn(resp);

        mockMvc.perform(post("/api/pedidos/cupones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCupon", is(1)))
                .andExpect(jsonPath("$.codigo", is("ECO10")))
                .andExpect(jsonPath("$.tipoDescuento", is("PORCENTAJE")))
                .andExpect(jsonPath("$.valorDescuento", is(10.0)))
                .andExpect(jsonPath("$.activo", is(true)));
    }

    @Test
    void testCrearCupon_codigoDuplicado_lanza409() throws Exception {
        CuponDescuento input = cuponMock("DUPLICADO", TipoDescuento.MONTO_FIJO, 500.0);

        when(cuponDescuentoService.crearCupon(any(CuponDescuento.class)))
                .thenThrow(new DataIntegrityViolationException("Codigo de cupon duplicado"));

        mockMvc.perform(post("/api/pedidos/cupones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isConflict());
    }
}
