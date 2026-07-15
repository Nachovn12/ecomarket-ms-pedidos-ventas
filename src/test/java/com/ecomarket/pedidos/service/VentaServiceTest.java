package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.dto.CrearFacturaRequest;
import com.ecomarket.pedidos.dto.CrearVentaRequest;
import com.ecomarket.pedidos.dto.ItemVentaRequest;
import com.ecomarket.pedidos.exception.RecursoNoEncontradoException;
import com.ecomarket.pedidos.model.Factura;
import com.ecomarket.pedidos.model.MetodoPago;
import com.ecomarket.pedidos.model.Venta;
import com.ecomarket.pedidos.repository.FacturaRepository;
import com.ecomarket.pedidos.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Tests del VentaService. Cubre venta presencial, IVA 19% y facturacion con folio consecutivo.
@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private FacturaRepository facturaRepository;
    @Mock private InventarioClientService inventarioClientService;

    @InjectMocks private VentaService ventaService;
    private ItemVentaRequest item(Long idProducto, String nombre, Integer cantidad, Double precio) {
        ItemVentaRequest item = new ItemVentaRequest();
        item.setIdProducto(idProducto);
        item.setNombreProducto(nombre);
        item.setCantidad(cantidad);
        item.setPrecioUnitario(precio);
        return item;
    }

    private CrearVentaRequest ventaRequest(Long idCliente, List<ItemVentaRequest> items) {
        CrearVentaRequest req = new CrearVentaRequest();
        req.setIdCliente(idCliente);
        req.setMetodoPago(MetodoPago.EFECTIVO);
        req.setItems(items);
        return req;
    }
    @Test
    void registrarVentaPresencial_OK() {
        ItemVentaRequest it = item(100L, "Bolsa biodegradable", 2, 1990.0);
        CrearVentaRequest req = ventaRequest(10L, List.of(it));

        when(inventarioClientService.consultarStock(100L))
                .thenReturn(Map.of("stockActual", 50));
        when(inventarioClientService.descontarStock(anyLong(), anyInt(), anyString()))
                .thenReturn(true);
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setIdVenta(500L);
            return v;
        });

        Venta resultado = ventaService.registrarVentaPresencial(req);

        assertNotNull(resultado);
        assertEquals(500L, resultado.getIdVenta());
        assertEquals(3980.0, resultado.getSubtotal());
        assertEquals(0.0, resultado.getDescuento());
        assertEquals(3980.0, resultado.getTotal());
        assertEquals(756.2, resultado.getIva());

        verify(ventaRepository, times(1)).save(any(Venta.class));
        verify(inventarioClientService, times(1)).descontarStock(100L, 2,
                "Venta presencial id=500");
    }
    @Test
    void registrarVentaPresencial_iva19PorCiento() {
        ItemVentaRequest it = item(200L, "Bolsas bamboo pack", 1, 1000.0);
        CrearVentaRequest req = ventaRequest(11L, List.of(it));

        when(inventarioClientService.consultarStock(200L))
                .thenReturn(Map.of("stockActual", 100));
        when(inventarioClientService.descontarStock(anyLong(), anyInt(), anyString()))
                .thenReturn(true);
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setIdVenta(501L);
            return v;
        });

        Venta resultado = ventaService.registrarVentaPresencial(req);

        assertEquals(1000.0, resultado.getSubtotal());
        assertEquals(0.0, resultado.getDescuento());
        assertEquals(190.0, resultado.getIva(), 0.001);
        assertEquals(1000.0, resultado.getTotal());
    }
    @Test
    void registrarVentaPresencial_multiplesItems_calculaSubtotal() {
        ItemVentaRequest it1 = item(1L, "Bolsa pequena", 2, 1500.0);
        ItemVentaRequest it2 = item(2L, "Bolsa grande", 3, 2000.0);
        CrearVentaRequest req = ventaRequest(12L, List.of(it1, it2));

        when(inventarioClientService.consultarStock(1L))
                .thenReturn(Map.of("stockActual", 50));
        when(inventarioClientService.consultarStock(2L))
                .thenReturn(Map.of("stockActual", 50));
        when(inventarioClientService.descontarStock(anyLong(), anyInt(), anyString()))
                .thenReturn(true);
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setIdVenta(502L);
            return v;
        });

        Venta resultado = ventaService.registrarVentaPresencial(req);

        assertEquals(9000.0, resultado.getSubtotal());
        assertEquals(1710.0, resultado.getIva(), 0.001);
        assertEquals(9000.0, resultado.getTotal());
    }
    @Test
    void registrarVentaPresencial_stockInsuficiente_lanzaExcepcion() {
        ItemVentaRequest it = item(300L, "Producto limitado", 10, 500.0);
        CrearVentaRequest req = ventaRequest(13L, List.of(it));

        when(inventarioClientService.consultarStock(300L))
                .thenReturn(Map.of("stockActual", 3));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ventaService.registrarVentaPresencial(req));

        assertTrue(ex.getMessage().toLowerCase().contains("stock"));
        verify(inventarioClientService, never()).descontarStock(anyLong(), anyInt(), anyString());
    }
    @Test
    void generarFactura_OK_folioConsecutivo() {
        Long idVenta = 500L;
        Venta venta = new Venta();
        venta.setIdVenta(idVenta);
        venta.setIdCliente(10L);
        venta.setSubtotal(1000.0);
        venta.setDescuento(0.0);
        venta.setIva(190.0);
        venta.setTotal(1000.0);

        CrearFacturaRequest req = new CrearFacturaRequest();
        req.setIdCliente(10L);
        req.setRutCliente("12.345.678-9");
        req.setRazonSocial("EcoMarket SpA");

        when(ventaRepository.findById(idVenta)).thenReturn(Optional.of(venta));
        when(facturaRepository.existsByIdVenta(idVenta)).thenReturn(false);
        when(facturaRepository.findMaxFolio()).thenReturn(Optional.of(1000));
        when(facturaRepository.save(any(Factura.class))).thenAnswer(inv -> {
            Factura f = inv.getArgument(0);
            f.setIdFactura(1L);
            return f;
        });

        Factura factura = ventaService.generarFactura(idVenta, req);

        assertNotNull(factura);
        assertEquals(1001, factura.getFolio());
        assertEquals(idVenta, factura.getIdVenta());
        assertEquals("12.345.678-9", factura.getRutCliente());
        assertEquals("EcoMarket SpA", factura.getRazonSocial());
        assertEquals(1000.0, factura.getSubtotal());

        ArgumentCaptor<Factura> captor = ArgumentCaptor.forClass(Factura.class);
        verify(facturaRepository, times(1)).save(captor.capture());
        assertEquals(1001, captor.getValue().getFolio());
    }
    @Test
    void generarFactura_ventaYaFacturada_lanzaExcepcion() {
        Long idVenta = 500L;
        Venta venta = new Venta();
        venta.setIdVenta(idVenta);
        venta.setIdCliente(10L);
        venta.setSubtotal(1000.0);
        venta.setTotal(1000.0);

        CrearFacturaRequest req = new CrearFacturaRequest();
        req.setIdCliente(10L);
        req.setRutCliente("12.345.678-9");
        req.setRazonSocial("EcoMarket SpA");

        when(ventaRepository.findById(idVenta)).thenReturn(Optional.of(venta));
        when(facturaRepository.existsByIdVenta(idVenta)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> ventaService.generarFactura(idVenta, req));

        assertTrue(ex.getMessage().toLowerCase().contains("factura"));
        verify(facturaRepository, never()).save(any(Factura.class));
    }

    @Test
    void obtenerVenta_existente_retornaVenta() {
        Venta v = new Venta();
        v.setIdVenta(1L);
        v.setIdCliente(10L);
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(v));

        Venta resultado = ventaService.obtenerVenta(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdVenta());
        assertEquals(10L, resultado.getIdCliente());
    }

    @Test
    void obtenerVenta_noExistente_lanzaExcepcion() {
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> ventaService.obtenerVenta(99L));
    }

    @Test
    void listarVentas_delegaEnRepository() {
        Venta v1 = new Venta();
        v1.setIdVenta(1L);
        Venta v2 = new Venta();
        v2.setIdVenta(2L);
        when(ventaRepository.findAll()).thenReturn(List.of(v1, v2));

        List<Venta> resultado = ventaService.listarVentas();

        assertEquals(2, resultado.size());
        verify(ventaRepository, times(1)).findAll();
    }

    @Test
    void actualizarVenta_OK() {
        Venta existente = new Venta();
        existente.setIdVenta(1L);
        existente.setIdCliente(10L);
        existente.setSubtotal(1000.0);
        existente.setDescuento(0.0);
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemVentaRequest it = item(1L, "", 2, 500.0);
        CrearVentaRequest req = ventaRequest(10L, List.of(it));

        Venta resultado = ventaService.actualizarVenta(1L, req);

        assertEquals(1000.0, resultado.getSubtotal());
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    @Test
    void eliminarVenta_OK() {
        Venta v = new Venta();
        v.setIdVenta(1L);
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(v));

        ventaService.eliminarVenta(1L);

        verify(ventaRepository, times(1)).delete(v);
    }

    @Test
    void obtenerFactura_existente() {
        Factura f = new Factura();
        f.setIdFactura(1L);
        f.setFolio(1001);
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(f));

        Factura resultado = ventaService.obtenerFactura(1L);

        assertNotNull(resultado);
        assertEquals(1001, resultado.getFolio());
    }

    @Test
    void obtenerFactura_noExistente_lanzaExcepcion() {
        when(facturaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> ventaService.obtenerFactura(99L));
    }

    @Test
    void registrarVentaPresencial_descuentoNegativo_lanzaExcepcion() {
        // Regla de negocio: el descuento no puede ser un valor negativo.
        // Escenario: empleado de ventas intenta ingresar descuento de -$500 en bolsas de papel craft.
        ItemVentaRequest it = item(100L, "Bolsa de papel kraft reciclado", 2, 1990.0);
        CrearVentaRequest req = ventaRequest(10L, List.of(it));
        req.setDescuento(-500.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> ventaService.registrarVentaPresencial(req));
        assertTrue(ex.getMessage().contains("negativo"));
    }

    @Test
    void registrarVentaPresencial_descuentoMayorAlSubtotal_lanzaExcepcion() {
        // Regla de negocio: el descuento no puede ser mayor al subtotal de la venta.
        // Escenario: empleado intenta aplicar descuento de $10.000 a una venta de $3.980 (2 bolsas).
        ItemVentaRequest it = item(100L, "Bolsa de papel kraft reciclado", 2, 1990.0);
        CrearVentaRequest req = ventaRequest(10L, List.of(it));
        req.setDescuento(10000.0);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> ventaService.registrarVentaPresencial(req));
        assertTrue(ex.getMessage().contains("superar"));
    }

    @Test
    void registrarVentaPresencial_inventarioNulo_lanzaExcepcion() {
        // Regla de negocio: no se puede procesar una venta si el producto no existe en inventario.
        // Escenario: empleado intenta vender un producto cuyo stock no está registrado en MS Inventario.
        ItemVentaRequest it = item(100L, "Bolsa biodegradable mediana", 2, 1990.0);
        CrearVentaRequest req = ventaRequest(10L, List.of(it));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));
        when(inventarioClientService.consultarStock(100L)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, 
            () -> ventaService.registrarVentaPresencial(req));
        assertTrue(ex.getMessage().contains("no existe"));
    }

    @Test
    void registrarVentaPresencial_stockObjectEnLugarDeStockActual_OK() {
        // Regla de negocio: el sistema tolera diferentes formatos de respuesta del MS Inventario.
        // Escenario: MS Inventario retorna el campo como 'stock' en lugar de 'stockActual'.
        ItemVentaRequest it = item(100L, "Bolsa biodegradable mediana", 2, 1990.0);
        CrearVentaRequest req = ventaRequest(10L, List.of(it));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setIdVenta(55L);
            return v;
        });
        when(inventarioClientService.consultarStock(100L)).thenReturn(Map.of("stock", 50));
        when(inventarioClientService.descontarStock(anyLong(), anyInt(), anyString())).thenReturn(true);

        Venta resultado = ventaService.registrarVentaPresencial(req);
        assertEquals(3980.0, resultado.getSubtotal()); // 2 × $1.990 = $3.980
    }

    @Test
    void registrarVentaPresencial_descontarStockFalla_soloLogueaAdvertencia() {
        // Regla de negocio: si el descuento de stock falla, la venta igual se registra (no bloquea la operación).
        // Escenario: la venta se registra exitosamente aunque el MS Inventario no pudo descontar el stock.
        ItemVentaRequest it = item(100L, "Bolsa biodegradable mediana", 2, 1990.0);
        CrearVentaRequest req = ventaRequest(10L, List.of(it));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setIdVenta(55L);
            return v;
        });
        when(inventarioClientService.consultarStock(100L)).thenReturn(Map.of("stockActual", 50));
        when(inventarioClientService.descontarStock(anyLong(), anyInt(), anyString())).thenReturn(false);

        Venta resultado = ventaService.registrarVentaPresencial(req);
        assertEquals(3980.0, resultado.getSubtotal()); // 2 × $1.990 = $3.980
        verify(inventarioClientService, times(1)).descontarStock(anyLong(), anyInt(), anyString());
    }

    @Test
    void actualizarVenta_descuentoNulo_asumeCero() {
        // Regla de negocio: si no se especifica descuento en la actualización, se asume $0.
        // Escenario: empleado actualiza una venta de cepillos de bambú sin cambiar el descuento.
        Venta existente = new Venta();
        existente.setIdVenta(1L);
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemVentaRequest it = item(201L, "Cepillo de dientes de bambú biodegradable", 2, 2990.0);
        CrearVentaRequest req = ventaRequest(10L, List.of(it));
        req.setDescuento(null);

        Venta resultado = ventaService.actualizarVenta(1L, req);
        assertEquals(5980.0, resultado.getSubtotal());
        assertEquals(0.0, resultado.getDescuento());
    }

    @Test
    void toResponse_ventaNull_retornaNull() {
        com.ecomarket.pedidos.dto.VentaResponse resp = ventaService.toResponse((Venta) null);
        assertTrue(resp == null);
    }

    @Test
    void toResponse_venta_mapeoCompleto() {
        Venta v = new Venta();
        v.setIdVenta(10L);
        v.setIdCliente(5L);
        v.setTotal(1500.0);
        com.ecomarket.pedidos.dto.VentaResponse resp = ventaService.toResponse(v);
        assertEquals(10L, resp.getIdVenta());
        assertEquals(5L, resp.getIdCliente());
        assertEquals(1500.0, resp.getTotal());
    }

    @Test
    void toResponse_facturaNull_retornaNull() {
        com.ecomarket.pedidos.dto.FacturaResponse resp = ventaService.toResponse((Factura) null);
        assertTrue(resp == null);
    }

    @Test
    void toResponse_factura_mapeoCompleto() {
        Factura f = new Factura();
        f.setIdFactura(20L);
        f.setRutCliente("11-1");
        com.ecomarket.pedidos.dto.FacturaResponse resp = ventaService.toResponse(f);
        assertEquals(20L, resp.getIdFactura());
        assertEquals("11-1", resp.getRutCliente());
    }

    @Test
    void registrarVentaPresencial_stockComoString_OK() {
        // Regla de negocio: el sistema tolera que MS Inventario retorne stockActual como String.
        // Escenario: el inventario retorna {"stockActual": "50"} (String) en lugar de Integer.
        ItemVentaRequest it = item(100L, "Bolsa biodegradable mediana", 2, 1990.0);
        CrearVentaRequest req = ventaRequest(10L, List.of(it));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setIdVenta(77L);
            return v;
        });
        // stockActual viene como String "50" (no instanceof Number)
        when(inventarioClientService.consultarStock(100L)).thenReturn(Map.of("stockActual", "50"));
        when(inventarioClientService.descontarStock(anyLong(), anyInt(), anyString())).thenReturn(true);

        Venta resultado = ventaService.registrarVentaPresencial(req);
        assertEquals(3980.0, resultado.getSubtotal()); // 2 × $1.990 = $3.980
    }

    @Test
    void registrarVentaPresencial_stockObjetoNulo_stockActualNull_continua() {
        // Regla de negocio: si el mapa de inventario no contiene la clave de stock, la venta continúa sin validar stock.
        // Escenario: MS Inventario retorna mapa vacío; el sistema no bloquea la operación de venta.
        ItemVentaRequest it = item(100L, "Bolsa biodegradable mediana", 1, 1990.0);
        CrearVentaRequest req = ventaRequest(10L, List.of(it));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            v.setIdVenta(88L);
            return v;
        });
        // mapa sin ninguna clave -> stockObj queda null -> stockActual = null -> no supera stockActual
        when(inventarioClientService.consultarStock(100L)).thenReturn(Map.of());
        when(inventarioClientService.descontarStock(anyLong(), anyInt(), anyString())).thenReturn(true);

        Venta resultado = ventaService.registrarVentaPresencial(req);
        assertEquals(1990.0, resultado.getSubtotal()); // 1 × $1.990 = $1.990
    }

    @Test
    void actualizarVenta_conDescuento_calculaCorrectamente() {
        // Regla de negocio (Empleado): puede aplicar descuento al actualizar una venta existente.
        // Escenario: empleado aplica descuento de $2.000 a una venta de 2 bolsas biodegradables ($3.980 subtotal).
        Venta existente = new Venta();
        existente.setIdVenta(1L);
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> inv.getArgument(0));

        ItemVentaRequest it = item(100L, "Bolsa biodegradable mediana", 2, 1990.0);
        CrearVentaRequest req = ventaRequest(10L, List.of(it));
        req.setDescuento(2000.0); // descuento aprobado por gerente de tienda

        Venta resultado = ventaService.actualizarVenta(1L, req);
        assertEquals(3980.0, resultado.getSubtotal());
        assertEquals(2000.0, resultado.getDescuento());
        assertEquals(1980.0, resultado.getTotal());
    }
}

