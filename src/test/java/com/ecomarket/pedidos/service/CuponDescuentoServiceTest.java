package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.dto.AplicarCuponResponse;
import com.ecomarket.pedidos.model.CuponDescuento;
import com.ecomarket.pedidos.model.TipoDescuento;
import com.ecomarket.pedidos.repository.CuponDescuentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

// Tests del CuponDescuentoService. Cubre cupones %, monto fijo, vencidos y deshabilitados.
@ExtendWith(MockitoExtension.class)
class CuponDescuentoServiceTest {

    @Mock private CuponDescuentoRepository cuponDescuentoRepository;

    @InjectMocks private CuponDescuentoService cuponDescuentoService;
    private CuponDescuento cuponPorcentaje(String codigo, Double valor, Double montoMinimo, Boolean activo) {
        CuponDescuento cupon = new CuponDescuento();
        cupon.setIdCupon(1L);
        cupon.setCodigo(codigo);
        cupon.setTipoDescuento(TipoDescuento.PORCENTAJE);
        cupon.setValorDescuento(valor);
        cupon.setMontoMinimo(montoMinimo);
        cupon.setFechaVencimiento(LocalDate.now().plusMonths(3));
        cupon.setActivo(activo);
        return cupon;
    }

    private CuponDescuento cuponMontoFijo(String codigo, Double valor, Double montoMinimo, Boolean activo) {
        CuponDescuento cupon = new CuponDescuento();
        cupon.setIdCupon(2L);
        cupon.setCodigo(codigo);
        cupon.setTipoDescuento(TipoDescuento.MONTO_FIJO);
        cupon.setValorDescuento(valor);
        cupon.setMontoMinimo(montoMinimo);
        cupon.setFechaVencimiento(LocalDate.now().plusMonths(3));
        cupon.setActivo(activo);
        return cupon;
    }
    @Test
    void aplicarCupon_porcentaje_OK() {
        String codigo = "ECO10";
        CuponDescuento cupon = cuponPorcentaje(codigo, 10.0, null, true);

        when(cuponDescuentoRepository.findByCodigoIgnoreCase(eq(codigo)))
                .thenReturn(Optional.of(cupon));

        AplicarCuponResponse resultado = cuponDescuentoService.aplicarCupon(codigo, 10000.0);

        assertNotNull(resultado);
        assertEquals(codigo, resultado.getCodigo());
        assertEquals(10000.0, resultado.getSubtotal());
        assertEquals(1000.0, resultado.getDescuento());
        assertEquals(9000.0, resultado.getTotalFinal());
        assertEquals("Cupon aplicado correctamente", resultado.getMensaje());
    }
    @Test
    void aplicarCupon_montoFijo_OK() {
        String codigo = "VERDE500";
        CuponDescuento cupon = cuponMontoFijo(codigo, 500.0, null, true);

        when(cuponDescuentoRepository.findByCodigoIgnoreCase(eq(codigo)))
                .thenReturn(Optional.of(cupon));

        AplicarCuponResponse resultado = cuponDescuentoService.aplicarCupon(codigo, 10000.0);

        assertNotNull(resultado);
        assertEquals(500.0, resultado.getDescuento());
        assertEquals(9500.0, resultado.getTotalFinal());
    }
    @Test
    void aplicarCupon_montoFijo_mayorASubtotal_tomaMinimo() {
        String codigo = "GRANDE";
        CuponDescuento cupon = cuponMontoFijo(codigo, 20000.0, null, true);

        when(cuponDescuentoRepository.findByCodigoIgnoreCase(eq(codigo)))
                .thenReturn(Optional.of(cupon));

        AplicarCuponResponse resultado = cuponDescuentoService.aplicarCupon(codigo, 5000.0);

        assertEquals(5000.0, resultado.getDescuento());
        assertEquals(0.0, resultado.getTotalFinal());
    }
    @Test
    void aplicarCupon_noExiste_lanzaExcepcion() {
        String codigo = "NOEXISTE";
        when(cuponDescuentoRepository.findByCodigoIgnoreCase(eq(codigo)))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cuponDescuentoService.aplicarCupon(codigo, 10000.0));

        assertTrue(ex.getMessage().toLowerCase().contains("cupon")
                || ex.getMessage().toLowerCase().contains("codigo"));
    }
    @Test
    void aplicarCupon_vencido_lanzaExcepcion() {
        String codigo = "EXPIRADO";
        CuponDescuento cupon = cuponPorcentaje(codigo, 15.0, null, true);
        cupon.setFechaVencimiento(LocalDate.now().minusDays(1));

        when(cuponDescuentoRepository.findByCodigoIgnoreCase(eq(codigo)))
                .thenReturn(Optional.of(cupon));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cuponDescuentoService.aplicarCupon(codigo, 10000.0));

        assertTrue(ex.getMessage().toLowerCase().contains("vencid")
                || ex.getMessage().toLowerCase().contains("expir"));
    }
    @Test
    void aplicarCupon_deshabilitado_lanzaExcepcion() {
        String codigo = "DESHABILITADO";
        CuponDescuento cupon = cuponPorcentaje(codigo, 10.0, null, false);

        when(cuponDescuentoRepository.findByCodigoIgnoreCase(eq(codigo)))
                .thenReturn(Optional.of(cupon));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cuponDescuentoService.aplicarCupon(codigo, 10000.0));

        assertTrue(ex.getMessage().toLowerCase().contains("cupon")
                || ex.getMessage().toLowerCase().contains("deshabil"));
    }

    @Test
    void toResponse_mapeaTodosLosCampos() {
        CuponDescuento cupon = new CuponDescuento();
        cupon.setIdCupon(1L);
        cupon.setCodigo("VERDE20");
        cupon.setTipoDescuento(com.ecomarket.pedidos.model.TipoDescuento.PORCENTAJE);
        cupon.setValorDescuento(20.0);
        cupon.setMontoMinimo(5000.0);
        cupon.setFechaVencimiento(java.time.LocalDate.of(2027, 12, 31));
        cupon.setActivo(true);

        com.ecomarket.pedidos.dto.CuponDescuentoResponse resp = cuponDescuentoService.toResponse(cupon);

        assertEquals(1L, resp.getIdCupon());
        assertEquals("VERDE20", resp.getCodigo());
        assertEquals(com.ecomarket.pedidos.model.TipoDescuento.PORCENTAJE, resp.getTipoDescuento());
        assertEquals(20.0, resp.getValorDescuento());
        assertEquals(5000.0, resp.getMontoMinimo());
        assertEquals(java.time.LocalDate.of(2027, 12, 31), resp.getFechaVencimiento());
        assertEquals(true, resp.getActivo());
    }

    @Test
    void toResponse_cuponNull_retornaNull() {
        assertNull(cuponDescuentoService.toResponse(null));
    }
}
