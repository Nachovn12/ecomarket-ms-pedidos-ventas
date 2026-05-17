package com.ecomarket.pedidos;

import com.ecomarket.pedidos.dto.*;
import com.ecomarket.pedidos.entity.*;
import com.ecomarket.pedidos.repository.*;
import com.ecomarket.pedidos.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MsPedidosVentasApplicationTests {

    @Autowired private CarritoService carritoService;
    @Autowired private PedidoService pedidoService;
    @Autowired private VentaService ventaService;
    @Autowired private DevolucionService devolucionService;

    private Long idCarritoActivo;

    @BeforeEach
    void setUp() {
        CarritoCompra carrito = carritoService.crearCarrito(1L);

        AgregarItemCarritoRequest itemReq = new AgregarItemCarritoRequest();
        itemReq.setIdProducto(10L);
        itemReq.setNombreProducto("Producto Test");
        itemReq.setCantidad(2);
        itemReq.setPrecioUnitario(1000.0);
        itemReq.setStockDisponible(10);
        carritoService.agregarItem(carrito.getIdCarrito(), itemReq);

        idCarritoActivo = carrito.getIdCarrito();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void crearPedidoDesdeCarrito() {
        CrearPedidoRequest req = new CrearPedidoRequest();
        req.setMetodoPago(MetodoPago.EFECTIVO);
        req.setDireccionEntrega("Calle Test 123");

        Pedido pedido = pedidoService.crearDesdeCarrito(idCarritoActivo, req);

        assertNotNull(pedido.getIdPedido());
        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertEquals(1L, pedido.getIdCliente());
        assertTrue(pedido.getTotal() > 0);
    }

    @Test
    void consultarEstadoPedido() {
        CrearPedidoRequest req = new CrearPedidoRequest();
        req.setMetodoPago(MetodoPago.EFECTIVO);
        Pedido pedido = pedidoService.crearDesdeCarrito(idCarritoActivo, req);

        EstadoPedido estado = pedidoService.consultarEstado(pedido.getIdPedido());

        assertEquals(EstadoPedido.PENDIENTE, estado);
    }

    @Test
    void cancelarPedidoPendiente() {
        CrearPedidoRequest req = new CrearPedidoRequest();
        req.setMetodoPago(MetodoPago.TARJETA);
        Pedido pedido = pedidoService.crearDesdeCarrito(idCarritoActivo, req);

        Pedido cancelado = pedidoService.cancelarPedido(pedido.getIdPedido(), "Test cancelacion");

        assertEquals(EstadoPedido.CANCELADO, cancelado.getEstado());
    }

    @Test
    void registrarVentaPresencial() {
        ItemVentaRequest item = new ItemVentaRequest();
        item.setIdProducto(1L);
        item.setNombreProducto("Producto Venta");
        item.setCantidad(3);
        item.setPrecioUnitario(500.0);

        CrearVentaRequest req = new CrearVentaRequest();
        req.setIdCliente(2L);
        req.setMetodoPago(MetodoPago.EFECTIVO);
        req.setItems(List.of(item));

        Venta venta = ventaService.registrarVentaPresencial(req);

        assertNotNull(venta.getIdVenta());
        assertEquals(1500.0, venta.getTotal());
        assertEquals(1500.0, venta.getSubtotal());
        assertEquals(0.0, venta.getDescuento());
    }

    @Test
    void generarFactura() {
        ItemVentaRequest item = new ItemVentaRequest();
        item.setIdProducto(1L);
        item.setNombreProducto("Producto Factura");
        item.setCantidad(1);
        item.setPrecioUnitario(2000.0);

        CrearVentaRequest ventaReq = new CrearVentaRequest();
        ventaReq.setIdCliente(3L);
        ventaReq.setMetodoPago(MetodoPago.TRANSFERENCIA);
        ventaReq.setItems(List.of(item));
        Venta venta = ventaService.registrarVentaPresencial(ventaReq);

        CrearFacturaRequest facturaReq = new CrearFacturaRequest();
        facturaReq.setRutCliente("12345678-9");
        facturaReq.setRazonSocial("Empresa Test SpA");
        Factura factura = ventaService.generarFactura(venta.getIdVenta(), facturaReq);

        assertNotNull(factura.getIdFactura());
        assertEquals(venta.getIdVenta(), factura.getIdVenta());
    }

    @Test
    void evitarFacturaDuplicada() {
        ItemVentaRequest item = new ItemVentaRequest();
        item.setIdProducto(1L);
        item.setNombreProducto("Producto Duplicado");
        item.setCantidad(1);
        item.setPrecioUnitario(1000.0);

        CrearVentaRequest ventaReq = new CrearVentaRequest();
        ventaReq.setIdCliente(4L);
        ventaReq.setMetodoPago(MetodoPago.EFECTIVO);
        ventaReq.setItems(List.of(item));
        Venta venta = ventaService.registrarVentaPresencial(ventaReq);

        CrearFacturaRequest facturaReq = new CrearFacturaRequest();
        facturaReq.setRutCliente("11111111-1");
        ventaService.generarFactura(venta.getIdVenta(), facturaReq);

        assertThrows(IllegalStateException.class, () ->
            ventaService.generarFactura(venta.getIdVenta(), facturaReq)
        );
    }

    @Test
    void crearDevolucion() {
        ItemVentaRequest item = new ItemVentaRequest();
        item.setIdProducto(1L);
        item.setNombreProducto("Producto Dev");
        item.setCantidad(1);
        item.setPrecioUnitario(800.0);

        CrearVentaRequest ventaReq = new CrearVentaRequest();
        ventaReq.setIdCliente(5L);
        ventaReq.setMetodoPago(MetodoPago.EFECTIVO);
        ventaReq.setItems(List.of(item));
        Venta venta = ventaService.registrarVentaPresencial(ventaReq);

        CrearDevolucionRequest devReq = new CrearDevolucionRequest();
        devReq.setIdCliente(5L);
        devReq.setIdVenta(venta.getIdVenta());
        devReq.setMotivo("Producto defectuoso");
        Devolucion devolucion = devolucionService.crearDevolucion(devReq);

        assertNotNull(devolucion.getIdDevolucion());
        assertEquals(venta.getIdVenta(), devolucion.getIdVenta());
    }

    @Test
    void crearReclamacion() {
        CrearPedidoRequest pedidoReq = new CrearPedidoRequest();
        pedidoReq.setMetodoPago(MetodoPago.EFECTIVO);
        Pedido pedido = pedidoService.crearDesdeCarrito(idCarritoActivo, pedidoReq);

        CrearReclamacionRequest recReq = new CrearReclamacionRequest();
        recReq.setIdCliente(1L);
        recReq.setIdPedido(pedido.getIdPedido());
        recReq.setMotivo("Pedido incompleto");
        recReq.setDescripcion("Faltaron 2 productos");
        Reclamacion reclamacion = pedidoService.crearReclamacionPorPedido(pedido.getIdPedido(), recReq);

        assertNotNull(reclamacion.getIdReclamacion());
        assertEquals(pedido.getIdPedido(), reclamacion.getIdPedido());

        List<Reclamacion> lista = pedidoService.listarReclamacionesPorPedido(pedido.getIdPedido());
        assertEquals(1, lista.size());
    }
}