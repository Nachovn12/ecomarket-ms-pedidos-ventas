package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.dto.CrearDevolucionRequest;
import com.ecomarket.pedidos.dto.CrearReclamacionRequest;
import com.ecomarket.pedidos.model.Devolucion;
import com.ecomarket.pedidos.model.Reclamacion;
import com.ecomarket.pedidos.repository.DevolucionRepository;
import com.ecomarket.pedidos.repository.ReclamacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Tests del DevolucionService. Cubre devoluciones, reclamaciones y cambios de estado.
@ExtendWith(MockitoExtension.class)
class DevolucionServiceTest {

    @Mock private DevolucionRepository devolucionRepository;
    @Mock private ReclamacionRepository reclamacionRepository;

    @InjectMocks private DevolucionService devolucionService;
    private Devolucion devolucion(Long idDevolucion, String estado) {
        Devolucion dev = new Devolucion();
        dev.setIdDevolucion(idDevolucion);
        dev.setIdCliente(10L);
        dev.setIdVenta(500L);
        dev.setMotivo("Producto defectuoso");
        dev.setEstado(estado);
        return dev;
    }

    private CrearDevolucionRequest requestDevolucion() {
        CrearDevolucionRequest req = new CrearDevolucionRequest();
        req.setIdCliente(10L);
        req.setIdVenta(500L);
        req.setMotivo("Producto defectuoso");
        return req;
    }
    @Test
    void crearDevolucion_OK() {
        CrearDevolucionRequest req = requestDevolucion();
        when(devolucionRepository.save(any(Devolucion.class))).thenAnswer(inv -> {
            Devolucion d = inv.getArgument(0);
            d.setIdDevolucion(1L);
            return d;
        });

        Devolucion resultado = devolucionService.crearDevolucion(req);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdDevolucion());
        assertEquals(10L, resultado.getIdCliente());
        assertEquals(500L, resultado.getIdVenta());
        assertEquals("Producto defectuoso", resultado.getMotivo());
        assertEquals("PENDIENTE", resultado.getEstado());

        verify(devolucionRepository, times(1)).save(any(Devolucion.class));
    }
    @Test
    void obtenerDevolucion_noExiste_lanza404() {
        Long idDevolucion = 999L;
        when(devolucionRepository.findById(idDevolucion)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> devolucionService.obtenerDevolucion(idDevolucion));

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }
    @Test
    void actualizarEstadoDevolucion_estadoValido_OK() {
        Long idDevolucion = 1L;
        Devolucion dev = devolucion(idDevolucion, "PENDIENTE");
        when(devolucionRepository.findById(idDevolucion)).thenReturn(Optional.of(dev));
        when(devolucionRepository.save(any(Devolucion.class))).thenAnswer(inv -> inv.getArgument(0));

        Devolucion resultado = devolucionService.actualizarEstadoDevolucion(idDevolucion, " aprobada ");

        assertEquals("APROBADA", resultado.getEstado());
        verify(devolucionRepository, times(1)).save(any(Devolucion.class));
    }
    @Test
    void actualizarEstadoDevolucion_estadoInvalido_distinto_lanza400() {
        Long idDevolucion = 1L;
        Devolucion dev = devolucion(idDevolucion, "PENDIENTE");
        when(devolucionRepository.findById(idDevolucion)).thenReturn(Optional.of(dev));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> devolucionService.actualizarEstadoDevolucion(idDevolucion, "INVALIDO"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(devolucionRepository, never()).save(any(Devolucion.class));
    }
    @Test
    void actualizarEstadoDevolucion_nulo_lanza400() {
        Long idDevolucion = 1L;
        Devolucion dev = devolucion(idDevolucion, "PENDIENTE");
        when(devolucionRepository.findById(idDevolucion)).thenReturn(Optional.of(dev));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> devolucionService.actualizarEstadoDevolucion(idDevolucion, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        assertTrue(ex.getReason() != null && ex.getReason().toLowerCase().contains("obligatorio"));
        verify(devolucionRepository, never()).save(any(Devolucion.class));
    }

    @Test
    void crearReclamacion_OK() {
        CrearReclamacionRequest req = new CrearReclamacionRequest();
        req.setIdCliente(10L);
        req.setIdPedido(1L);
        req.setIdVenta(100L);
        req.setMotivo("Producto danado");
        req.setDescripcion("Bolsa rota en la entrega");
        when(reclamacionRepository.save(any(Reclamacion.class))).thenAnswer(inv -> { Reclamacion r = inv.getArgument(0); r.setIdReclamacion(5L); return r; });

        Reclamacion rec = devolucionService.crearReclamacion(req);

        assertNotNull(rec);
        assertEquals(5L, rec.getIdReclamacion());
        assertEquals(10L, rec.getIdCliente());
        assertEquals(1L, rec.getIdPedido());
        assertEquals("Producto danado", rec.getMotivo());
        assertEquals("Bolsa rota en la entrega", rec.getDescripcion());
        verify(reclamacionRepository, times(1)).save(any(Reclamacion.class));
    }

    @Test
    void actualizarEstadoReclamacion_OK_normalizaYGuarda() {
        Long idRec = 1L;
        Reclamacion rec = new Reclamacion();
        rec.setIdReclamacion(idRec);
        rec.setIdCliente(10L);
        rec.setEstado("ABIERTA");
        when(reclamacionRepository.findById(idRec)).thenReturn(Optional.of(rec));
        when(reclamacionRepository.save(any(Reclamacion.class))).thenAnswer(inv -> inv.getArgument(0));

        Reclamacion actualizado = devolucionService.actualizarEstadoReclamacion(idRec, "  resuelta  ");

        assertEquals("RESUELTA", actualizado.getEstado());
        verify(reclamacionRepository, times(1)).save(any(Reclamacion.class));
    }

    @Test
    void actualizarEstadoReclamacion_estadoInvalido_lanza400() {
        Long idRec = 1L;
        Reclamacion rec = new Reclamacion();
        rec.setIdReclamacion(idRec);
        rec.setIdCliente(10L);
        when(reclamacionRepository.findById(idRec)).thenReturn(Optional.of(rec));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> devolucionService.actualizarEstadoReclamacion(idRec, "ESTADO_FAKE"));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verify(reclamacionRepository, never()).save(any(Reclamacion.class));
    }

    @Test
    void toResponse_Devolucion_mapeaTodosLosCampos() {
        Devolucion dev = new Devolucion();
        dev.setIdDevolucion(1L);
        dev.setIdCliente(10L);
        dev.setIdPedido(2L);
        dev.setIdVenta(100L);
        dev.setMotivo("Defecto de fabrica");
        dev.setEstado("APROBADA");

        com.ecomarket.pedidos.dto.DevolucionResponse resp = devolucionService.toResponse(dev);

        assertEquals(1L, resp.getIdDevolucion());
        assertEquals(10L, resp.getIdCliente());
        assertEquals(2L, resp.getIdPedido());
        assertEquals(100L, resp.getIdVenta());
        assertEquals("Defecto de fabrica", resp.getMotivo());
        assertEquals("APROBADA", resp.getEstado());
    }

    @Test
    void toResponse_Reclamacion_mapeaTodosLosCampos() {
        Reclamacion rec = new Reclamacion();
        rec.setIdReclamacion(1L);
        rec.setIdCliente(10L);
        rec.setIdPedido(2L);
        rec.setIdVenta(100L);
        rec.setMotivo("Atraso en entrega");
        rec.setDescripcion("Llego 5 dias tarde");
        rec.setEstado("EN_REVISION");

        com.ecomarket.pedidos.dto.ReclamacionResponse resp = devolucionService.toResponse(rec);

        assertEquals(1L, resp.getIdReclamacion());
        assertEquals(10L, resp.getIdCliente());
        assertEquals(2L, resp.getIdPedido());
        assertEquals(100L, resp.getIdVenta());
        assertEquals("Atraso en entrega", resp.getMotivo());
        assertEquals("Llego 5 dias tarde", resp.getDescripcion());
        assertEquals("EN_REVISION", resp.getEstado());
    }
}
