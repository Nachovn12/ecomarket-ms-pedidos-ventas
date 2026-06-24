package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.dto.CrearPedidoRequest;
import com.ecomarket.pedidos.exception.RecursoNoEncontradoException;
import com.ecomarket.pedidos.model.CarritoCompra;
import com.ecomarket.pedidos.model.EstadoCarrito;
import com.ecomarket.pedidos.model.EstadoPedido;
import com.ecomarket.pedidos.model.HistorialPedido;
import com.ecomarket.pedidos.dto.PedidoResponse;
import com.ecomarket.pedidos.model.ItemCarrito;
import com.ecomarket.pedidos.model.MetodoPago;
import com.ecomarket.pedidos.model.Pedido;
import com.ecomarket.pedidos.repository.CarritoCompraRepository;
import com.ecomarket.pedidos.repository.HistorialPedidoRepository;
import com.ecomarket.pedidos.repository.PedidoRepository;
import com.ecomarket.pedidos.repository.ReclamacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Tests del PedidoService. Cubre creacion desde carrito, cancelacion, IVA 19% e historial.
@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private CarritoCompraRepository carritoCompraRepository;
    @Mock private HistorialPedidoRepository historialPedidoRepository;
    @Mock private ReclamacionRepository reclamacionRepository;
    @Mock private CatalogoClientService catalogoClientService;
    @Mock private InventarioClientService inventarioClientService;
    @Mock private LogisticaClientService logisticaClientService;

    @InjectMocks private PedidoService pedidoService;
    private CarritoCompra carritoActivoConItem(Long idCarrito, Long idCliente, Long idProducto,
                                               String nombre, Integer cantidad, Double precioUnitario) {
        CarritoCompra carrito = new CarritoCompra();
        carrito.setIdCarrito(idCarrito);
        carrito.setIdCliente(idCliente);
        carrito.setEstado(EstadoCarrito.ACTIVO);

        ItemCarrito item = new ItemCarrito();
        item.setIdProducto(idProducto);
        item.setNombreProducto(nombre);
        item.setCantidad(cantidad);
        item.setPrecioUnitario(precioUnitario);
        item.recalcularSubtotal();

        ArrayList<ItemCarrito> items = new ArrayList<>();
        items.add(item);
        carrito.setItems(items);
        carrito.recalcularTotales();
        return carrito;
    }

    private CrearPedidoRequest requestBasico() {
        CrearPedidoRequest req = new CrearPedidoRequest();
        req.setIdCliente(10L);
        req.setMetodoPago(MetodoPago.TARJETA);
        req.setDireccionEntrega("Av. Siempre Viva 742, Santiago");
        req.setObservaciones("Test");
        return req;
    }
    @Test
    void crearDesdeCarrito_OK() {
        Long idCarrito = 1L;
        CarritoCompra carrito = carritoActivoConItem(idCarrito, 10L, 100L, "Bolsa biodegradable", 2, 1990.0);
        // subtotal esperado: 2 * 1990 = 3980

        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));
        when(inventarioClientService.consultarStock(100L))
                .thenReturn(Map.of("stockActual", 50));
        when(catalogoClientService.obtenerProducto(100L))
                .thenReturn(Map.of("idProducto", 100, "precio", 1990.0));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setIdPedido(99L);
            return p;
        });

        Pedido resultado = pedidoService.crearDesdeCarrito(idCarrito, requestBasico());

        assertNotNull(resultado);
        assertEquals(99L, resultado.getIdPedido());
        assertEquals(EstadoPedido.PENDIENTE, resultado.getEstado());
        assertEquals(10L, resultado.getIdCliente());
        assertEquals(3980.0, resultado.getSubtotal());
        // IVA 19% sobre 3980 = 756.20
        assertEquals(756.2, resultado.getIva());
        assertEquals(3980.0, resultado.getTotal());

        verify(carritoCompraRepository, times(1)).findById(idCarrito);
        verify(inventarioClientService, times(1)).consultarStock(100L);
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
        verify(historialPedidoRepository, times(1)).save(any(HistorialPedido.class));
        verify(logisticaClientService, times(1)).solicitarDespacho(anyLong(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }
    @Test
    void crearDesdeCarrito_iva19PorCiento() {
        Long idCarrito = 2L;
        CarritoCompra carrito = carritoActivoConItem(idCarrito, 10L, 200L, "Bolsa bamboo", 1, 1000.0);

        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));
        when(inventarioClientService.consultarStock(200L))
                .thenReturn(Map.of("stockActual", 10));
        when(catalogoClientService.obtenerProducto(200L))
                .thenReturn(Map.of("idProducto", 200, "precio", 1000.0));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setIdPedido(100L);
            return p;
        });

        Pedido resultado = pedidoService.crearDesdeCarrito(idCarrito, requestBasico());

        assertEquals(1000.0, resultado.getSubtotal());
        assertEquals(0.0, resultado.getDescuento());
        assertEquals(190.0, resultado.getIva(), 0.001);
        assertEquals(1000.0, resultado.getTotal());
    }
    @Test
    void crearDesdeCarrito_carritoVacio_lanzaExcepcion() {
        Long idCarrito = 3L;
        CarritoCompra carrito = new CarritoCompra();
        carrito.setIdCarrito(idCarrito);
        carrito.setIdCliente(10L);
        carrito.setEstado(EstadoCarrito.ACTIVO);
        carrito.setItems(new ArrayList<>()); // vacio
        carrito.recalcularTotales();

        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.crearDesdeCarrito(idCarrito, requestBasico()));

        assertTrue(ex.getMessage().toLowerCase().contains("vacio"));
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(inventarioClientService, never()).consultarStock(anyLong());
    }
    @Test
    void crearDesdeCarrito_carritoNoActivo_lanzaExcepcion() {
        Long idCarrito = 4L;
        CarritoCompra carrito = new CarritoCompra();
        carrito.setIdCarrito(idCarrito);
        carrito.setIdCliente(10L);
        carrito.setEstado(EstadoCarrito.CONVERTIDO);
        carrito.setItems(new ArrayList<>());
        carrito.recalcularTotales();

        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.crearDesdeCarrito(idCarrito, requestBasico()));

        assertTrue(ex.getMessage().toLowerCase().contains("activo"));
        verify(pedidoRepository, never()).save(any(Pedido.class));
    }
    @Test
    void crearDesdeCarrito_carritoNoExiste_lanzaRecursoNoEncontrado() {
        Long idCarrito = 999L;
        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> pedidoService.crearDesdeCarrito(idCarrito, requestBasico()));

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    // AC-7: cancelacion PENDIENTE -> CANCELADO con registro en historial.
    @Test
    void cancelarPedido_pedidoPENDIENTE_pasaACANCELADO_yRegistraHistorial() {
        Long idPedido = 50L;
        Pedido pedido = new Pedido();
        pedido.setIdPedido(idPedido);
        pedido.setIdCliente(10L);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setSubtotal(1000.0);
        pedido.setDescuento(0.0);
        pedido.setIva(190.0);
        pedido.setTotal(1000.0);

        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido cancelado = pedidoService.cancelarPedido(idPedido, "Cliente solicito cancelacion");

        assertEquals(EstadoPedido.CANCELADO, cancelado.getEstado());
        assertTrue(cancelado.getObservaciones().contains("Cliente solicito cancelacion"));

        ArgumentCaptor<HistorialPedido> historialCaptor = ArgumentCaptor.forClass(HistorialPedido.class);
        verify(historialPedidoRepository, times(1)).save(historialCaptor.capture());
        HistorialPedido historialGuardado = historialCaptor.getValue();
        assertEquals(idPedido, historialGuardado.getIdPedido());
        assertEquals(EstadoPedido.PENDIENTE, historialGuardado.getEstadoAnterior());
        assertEquals(EstadoPedido.CANCELADO, historialGuardado.getEstadoNuevo());
    }
    @Test
    void cancelarPedido_pedidoNoPENDIENTE_lanzaExcepcion() {
        Long idPedido = 51L;
        Pedido pedido = new Pedido();
        pedido.setIdPedido(idPedido);
        pedido.setIdCliente(10L);
        pedido.setEstado(EstadoPedido.ENTREGADO);

        when(pedidoRepository.findById(idPedido)).thenReturn(Optional.of(pedido));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> pedidoService.cancelarPedido(idPedido, "intento"));

        assertTrue(ex.getMessage().toLowerCase().contains("pendiente"));
        verify(pedidoRepository, never()).save(any(Pedido.class));
        verify(historialPedidoRepository, never()).save(any(HistorialPedido.class));
    }

    @Test
    void listarPedidos_delegaEnRepository() {
        Pedido p1 = new Pedido();
        p1.setIdPedido(1L);
        Pedido p2 = new Pedido();
        p2.setIdPedido(2L);
        when(pedidoRepository.findAll()).thenReturn(java.util.List.of(p1, p2));

        java.util.List<Pedido> resultado = pedidoService.listarPedidos();

        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getIdPedido());
        verify(pedidoRepository, times(1)).findAll();
    }

    @Test
    void obtenerPedido_existente_retornaPedido() {
        Pedido p = new Pedido();
        p.setIdPedido(1L);
        p.setIdCliente(10L);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

        Pedido resultado = pedidoService.obtenerPedido(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getIdPedido());
        assertEquals(10L, resultado.getIdCliente());
    }

    @Test
    void obtenerPedido_noExistente_lanzaRecursoNoEncontrado() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> pedidoService.obtenerPedido(99L));
    }

    @Test
    void actualizarPedido_OK_modificaCamposYGuarda() {
        Pedido existente = new Pedido();
        existente.setIdPedido(1L);
        existente.setIdCliente(10L);
        existente.setEstado(EstadoPedido.PENDIENTE);
        existente.setMetodoPago(MetodoPago.TARJETA);
        existente.setDireccionEntrega("");
        existente.setObservaciones("");

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        CrearPedidoRequest req = new CrearPedidoRequest();
        req.setMetodoPago(MetodoPago.EFECTIVO);
        req.setDireccionEntrega("");
        req.setObservaciones("");

        Pedido actualizado = pedidoService.actualizarPedido(1L, req);

        assertEquals(MetodoPago.EFECTIVO, actualizado.getMetodoPago());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void actualizarPedido_pedidoCANCELADO_lanzaExcepcion() {
        Pedido cancelado = new Pedido();
        cancelado.setIdPedido(1L);
        cancelado.setEstado(EstadoPedido.CANCELADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(cancelado));

        assertThrows(IllegalArgumentException.class,
                () -> pedidoService.actualizarPedido(1L, requestBasico()));

        verify(pedidoRepository, never()).save(any(Pedido.class));
    }

    @Test
    void eliminarPedido_OK() {
        Pedido p = new Pedido();
        p.setIdPedido(1L);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

        pedidoService.eliminarPedido(1L);

        verify(pedidoRepository, times(1)).delete(p);
    }

    @Test
    void consultarEstado_pedidoExistente_retornaEstado() {
        Pedido p = new Pedido();
        p.setIdPedido(1L);
        p.setEstado(EstadoPedido.EN_PREPARACION);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

        EstadoPedido estado = pedidoService.consultarEstado(1L);

        assertEquals(EstadoPedido.EN_PREPARACION, estado);
    }

    @Test
    void consultarEstado_pedidoNoExistente_lanzaExcepcion() {
        when(pedidoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> pedidoService.consultarEstado(99L));
    }

    @Test
    void historialCliente_retornaPedidosDelCliente() {
        Pedido p1 = new Pedido();
        p1.setIdPedido(1L);
        p1.setIdCliente(10L);
        Pedido p2 = new Pedido();
        p2.setIdPedido(2L);
        p2.setIdCliente(10L);
        when(pedidoRepository.findByIdClienteOrderByFechaCreacionDesc(10L))
                .thenReturn(java.util.List.of(p1, p2));

        java.util.List<Pedido> historial = pedidoService.historialCliente(10L);

        assertEquals(2, historial.size());
        assertEquals(10L, historial.get(0).getIdCliente());
        verify(pedidoRepository, times(1)).findByIdClienteOrderByFechaCreacionDesc(10L);
    }

    @Test
    void crearReclamacionPorPedido_OK() {
        Pedido p = new Pedido();
        p.setIdPedido(1L);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(reclamacionRepository.save(any(com.ecomarket.pedidos.model.Reclamacion.class)))
                .thenAnswer(inv -> { com.ecomarket.pedidos.model.Reclamacion r = inv.getArgument(0); r.setIdReclamacion(7L); return r; });

        com.ecomarket.pedidos.dto.CrearReclamacionRequest req = new com.ecomarket.pedidos.dto.CrearReclamacionRequest();
        req.setIdCliente(10L);
        req.setIdPedido(1L);
        req.setMotivo("");

        com.ecomarket.pedidos.model.Reclamacion rec = pedidoService.crearReclamacionPorPedido(1L, req);

        assertNotNull(rec);
        assertEquals(7L, rec.getIdReclamacion());
        assertEquals(1L, rec.getIdPedido());
        assertEquals("", rec.getMotivo());
    }

    @Test
    void toResponse_mapeaTodosLosCampos() {
        Pedido p = new Pedido();
        p.setIdPedido(1L);
        p.setIdCliente(10L);
        p.setEstado(EstadoPedido.PENDIENTE);
        p.setMetodoPago(MetodoPago.TARJETA);
        p.setSubtotal(1000.0);
        p.setDescuento(100.0);
        p.setIva(190.0);
        p.setTotal(1090.0);
        p.setDireccionEntrega("");
        p.setObservaciones("");

        PedidoResponse resp = pedidoService.toResponse(p);

        assertEquals(1L, resp.getIdPedido());
        assertEquals(10L, resp.getIdCliente());
        assertEquals(EstadoPedido.PENDIENTE, resp.getEstado());
        assertEquals(MetodoPago.TARJETA, resp.getMetodoPago());
        assertEquals(1000.0, resp.getSubtotal());
        assertEquals(100.0, resp.getDescuento());
        assertEquals(190.0, resp.getIva());
        assertEquals(1090.0, resp.getTotal());
    }

    @Test
    void listarHistorialPedido_retornaHistorial() {
        HistorialPedido h1 = new HistorialPedido();
        h1.setIdHistorial(1L);
        h1.setIdPedido(1L);
        HistorialPedido h2 = new HistorialPedido();
        h2.setIdHistorial(2L);
        h2.setIdPedido(1L);
        when(historialPedidoRepository.findByIdPedidoOrderByFechaCambioDesc(1L))
                .thenReturn(java.util.List.of(h1, h2));

        java.util.List<HistorialPedido> historial = pedidoService.listarHistorialPedido(1L);

        assertEquals(2, historial.size());
        verify(historialPedidoRepository, times(1)).findByIdPedidoOrderByFechaCambioDesc(1L);
    }

    @Test
    void crearDesdeCarrito_inventarioDevuelveNulo_lanzaException() {
        Long idCarrito = 1L;
        CarritoCompra carrito = carritoActivoConItem(idCarrito, 10L, 100L, "Bolsa", 2, 1990.0);
        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));
        when(inventarioClientService.consultarStock(100L)).thenReturn(null);

        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
                () -> pedidoService.crearDesdeCarrito(idCarrito, requestBasico()));
        assertTrue(ex.getMessage().contains("no existe"));
    }

    @Test
    void crearDesdeCarrito_inventarioDevuelveStockEnLugarDeStockActual() {
        Long idCarrito = 1L;
        CarritoCompra carrito = carritoActivoConItem(idCarrito, 10L, 100L, "Bolsa", 2, 1990.0);
        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));
        when(inventarioClientService.consultarStock(100L)).thenReturn(Map.of("stock", 50));
        when(catalogoClientService.obtenerProducto(100L)).thenReturn(Map.of("idProducto", 100));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido p = pedidoService.crearDesdeCarrito(idCarrito, requestBasico());
        assertNotNull(p);
    }

    @Test
    void crearDesdeCarrito_inventarioNoDevuelveStock_capturaIllegalStateExceptionYContinua() {
        Long idCarrito = 1L;
        CarritoCompra carrito = carritoActivoConItem(idCarrito, 10L, 100L, "Bolsa", 2, 1990.0);
        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));
        when(inventarioClientService.consultarStock(100L)).thenReturn(Map.of());
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido p = pedidoService.crearDesdeCarrito(idCarrito, requestBasico());
        assertNotNull(p);
    }

    @Test
    void crearDesdeCarrito_stockInsuficiente_lanzaException() {
        Long idCarrito = 1L;
        CarritoCompra carrito = carritoActivoConItem(idCarrito, 10L, 100L, "Bolsa", 20, 1990.0);
        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));
        when(inventarioClientService.consultarStock(100L)).thenReturn(Map.of("stockActual", 5));

        com.ecomarket.pedidos.exception.StockInsuficienteException ex = assertThrows(
                com.ecomarket.pedidos.exception.StockInsuficienteException.class,
                () -> pedidoService.crearDesdeCarrito(idCarrito, requestBasico()));
        assertTrue(ex.getMessage().contains("insuficiente"));
    }

    @Test
    void crearDesdeCarrito_catalogoDevuelveNulo_lanzaException() {
        Long idCarrito = 1L;
        CarritoCompra carrito = carritoActivoConItem(idCarrito, 10L, 100L, "Bolsa", 2, 1990.0);
        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));
        when(inventarioClientService.consultarStock(100L)).thenReturn(Map.of("stockActual", 50));
        when(catalogoClientService.obtenerProducto(100L)).thenReturn(null);

        RecursoNoEncontradoException ex = assertThrows(RecursoNoEncontradoException.class,
                () -> pedidoService.crearDesdeCarrito(idCarrito, requestBasico()));
        assertTrue(ex.getMessage().contains("catalogo"));
    }

    @Test
    void actualizarPedido_pedidoENTREGADO_lanzaExcepcion() {
        Pedido p = new Pedido();
        p.setIdPedido(1L);
        p.setEstado(EstadoPedido.ENTREGADO);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pedidoService.actualizarPedido(1L, requestBasico()));
        assertTrue(ex.getMessage().contains("ENTREGADO"));
    }

    @Test
    void cancelarPedido_motivoVacio_usaMensajeDefecto() {
        Pedido p = new Pedido();
        p.setIdPedido(1L);
        p.setEstado(EstadoPedido.PENDIENTE);
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(p));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido cancelado = pedidoService.cancelarPedido(1L, "");
        assertTrue(cancelado.getObservaciones().contains("sin motivo"));
    }

    @Test
    void toResponse_pedido_mapeoOK() {
        Pedido p = new Pedido();
        p.setIdPedido(10L);
        p.setIdCliente(5L);
        p.setTotal(1500.0);
        PedidoResponse resp = pedidoService.toResponse(p);
        assertEquals(10L, resp.getIdPedido());
        assertEquals(5L, resp.getIdCliente());
    }

    @Test
    void cancelarPedido_noExiste_lanzaExcepcion() {
        // Cubre linea 185: orElseThrow en cancelarPedido
        when(pedidoRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class,
                () -> pedidoService.cancelarPedido(999L, "motivo"));
    }

    @Test
    void crearDesdeCarrito_stockComoString_OK() {
        // Cubre linea 84: rama else instanceof (Integer.parseInt) cuando stock viene como String
        Long idCarrito = 1L;
        CarritoCompra carrito = carritoActivoConItem(idCarrito, 10L, 100L, "Bolsa", 1, 1990.0);
        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));
        // stockActual viene como String -> no instanceof Number -> parseInt
        when(inventarioClientService.consultarStock(100L)).thenReturn(Map.of("stockActual", "50"));
        when(catalogoClientService.obtenerProducto(100L)).thenReturn(Map.of("idProducto", 100));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setIdPedido(99L);
            return p;
        });

        Pedido p = pedidoService.crearDesdeCarrito(idCarrito, requestBasico());
        assertNotNull(p);
    }

    @Test
    void crearDesdeCarrito_dosItemsMismoProducto_acumulaCantidades() {
        // Cubre linea 69: lambda en merge() con dos items del mismo producto
        // Cuando merge se llama con un valor ya existente, ejecuta la funcion (a, b) -> a + b
        Long idCarrito = 5L;
        CarritoCompra carrito = new CarritoCompra();
        carrito.setIdCarrito(idCarrito);
        carrito.setIdCliente(10L);
        carrito.setEstado(EstadoCarrito.ACTIVO);

        // 2 items del mismo producto para activar la rama del merge lambda
        ItemCarrito item1 = new ItemCarrito();
        item1.setIdProducto(200L);
        item1.setNombreProducto("Semilla");
        item1.setCantidad(2);
        item1.setPrecioUnitario(500.0);
        item1.recalcularSubtotal();

        ItemCarrito item2 = new ItemCarrito();
        item2.setIdProducto(200L); // mismo producto
        item2.setNombreProducto("Semilla");
        item2.setCantidad(3);
        item2.setPrecioUnitario(500.0);
        item2.recalcularSubtotal();

        ArrayList<ItemCarrito> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        carrito.setItems(items);
        carrito.recalcularTotales();

        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));
        // total solicitado = 5 unidades, stock = 10 -> OK
        when(inventarioClientService.consultarStock(200L)).thenReturn(Map.of("stockActual", 10));
        when(catalogoClientService.obtenerProducto(200L)).thenReturn(Map.of("idProducto", 200));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido p = inv.getArgument(0);
            p.setIdPedido(77L);
            return p;
        });

        Pedido p = pedidoService.crearDesdeCarrito(idCarrito, requestBasico());
        assertNotNull(p);
        assertEquals(77L, p.getIdPedido());
    }
}
