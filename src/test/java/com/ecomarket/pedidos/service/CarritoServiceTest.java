package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.dto.AgregarItemCarritoRequest;
import com.ecomarket.pedidos.dto.AplicarCuponResponse;
import com.ecomarket.pedidos.exception.RecursoNoEncontradoException;
import com.ecomarket.pedidos.dto.CarritoResponse;
import com.ecomarket.pedidos.model.CarritoCompra;
import com.ecomarket.pedidos.model.EstadoCarrito;
import com.ecomarket.pedidos.model.ItemCarrito;
import com.ecomarket.pedidos.repository.CarritoCompraRepository;
import com.ecomarket.pedidos.repository.ItemCarritoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
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

// Tests del CarritoService. Cubre creacion, items, validacion de stock y aplicacion de cupon.
@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock private CarritoCompraRepository carritoCompraRepository;
    @Mock private ItemCarritoRepository itemCarritoRepository;
    @Mock private CuponDescuentoService cuponDescuentoService;

    @InjectMocks private CarritoService carritoService;
    private CarritoCompra carritoActivoVacio(Long idCarrito, Long idCliente) {
        CarritoCompra carrito = new CarritoCompra();
        carrito.setIdCarrito(idCarrito);
        carrito.setIdCliente(idCliente);
        carrito.setEstado(EstadoCarrito.ACTIVO);
        carrito.setItems(new ArrayList<>());
        carrito.recalcularTotales();
        return carrito;
    }

    private AgregarItemCarritoRequest itemRequest(Long idProducto, String nombre,
                                                  Integer cantidad, Double precio, Integer stockDisponible) {
        AgregarItemCarritoRequest req = new AgregarItemCarritoRequest();
        req.setIdProducto(idProducto);
        req.setNombreProducto(nombre);
        req.setCantidad(cantidad);
        req.setPrecioUnitario(precio);
        req.setStockDisponible(stockDisponible);
        return req;
    }
    @Test
    void crearCarrito_OK() {
        Long idCliente = 10L;
        when(carritoCompraRepository.save(any(CarritoCompra.class))).thenAnswer(inv -> {
            CarritoCompra c = inv.getArgument(0);
            c.setIdCarrito(1L);
            return c;
        });

        CarritoCompra carrito = carritoService.crearCarrito(idCliente);

        assertNotNull(carrito);
        assertEquals(1L, carrito.getIdCarrito());
        assertEquals(idCliente, carrito.getIdCliente());
        assertEquals(EstadoCarrito.ACTIVO, carrito.getEstado());
        assertEquals(0.0, carrito.getSubtotal());
        assertEquals(0.0, carrito.getDescuentoAplicado());
        assertEquals(0.0, carrito.getTotal());

        verify(carritoCompraRepository, times(1)).save(any(CarritoCompra.class));
    }
    @Test
    void agregarItem_OK() {
        Long idCarrito = 1L;
        CarritoCompra carrito = carritoActivoVacio(idCarrito, 10L);
        AgregarItemCarritoRequest req = itemRequest(100L, "Bolsa biodegradable", 2, 1990.0, 50);

        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));
        when(carritoCompraRepository.save(any(CarritoCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        CarritoCompra resultado = carritoService.agregarItem(idCarrito, req);

        assertNotNull(resultado);
        assertEquals(1, resultado.getItems().size());
        assertEquals(3980.0, resultado.getSubtotal());
        assertEquals(3980.0, resultado.getTotal());

        verify(carritoCompraRepository, times(1)).findById(idCarrito);
        verify(carritoCompraRepository, times(1)).save(any(CarritoCompra.class));
    }
    @Test
    void agregarItem_carritoNoActivo_lanzaExcepcion() {
        Long idCarrito = 2L;
        CarritoCompra carrito = carritoActivoVacio(idCarrito, 10L);
        carrito.setEstado(EstadoCarrito.CONVERTIDO);

        AgregarItemCarritoRequest req = itemRequest(100L, "Bolsa", 1, 1000.0, 10);

        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carritoService.agregarItem(idCarrito, req));

        assertTrue(ex.getMessage().toLowerCase().contains("activo"));
        verify(carritoCompraRepository, never()).save(any(CarritoCompra.class));
    }
    @Test
    void agregarItem_stockInsuficiente_lanzaExcepcion() {
        // Regla de negocio EcoMarket: no se puede agregar más unidades de las disponibles en stock.
        // Escenario: cliente intenta agregar 10 unidades de jabón artesanal pero solo hay 3 en bodega.
        Long idCarrito = 3L;
        AgregarItemCarritoRequest req = itemRequest(300L, "Jabón artesanal de lavanda orgánica", 10, 3990.0, 3);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carritoService.agregarItem(idCarrito, req));

        assertTrue(ex.getMessage().toLowerCase().contains("stock"));
        verify(carritoCompraRepository, never()).save(any(CarritoCompra.class));
    }
    @Test
    void aplicarCupon_OK_calculaDescuento() {
        Long idCarrito = 4L;
        CarritoCompra carrito = carritoActivoVacio(idCarrito, 10L);
        ItemCarrito item = new ItemCarrito();
        item.setIdProducto(100L);
        item.setNombreProducto("Bolsa biodegradable pack x10");
        item.setCantidad(1);
        item.setPrecioUnitario(10000.0);
        item.recalcularSubtotal();
        carrito.agregarItem(item);

        String codigo = "ECO10";
        AplicarCuponResponse cuponResponse = new AplicarCuponResponse(
                codigo, 10000.0, 1000.0, 9000.0, "Cupon aplicado correctamente"
        );

        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));
        when(cuponDescuentoService.aplicarCupon(codigo, 10000.0)).thenReturn(cuponResponse);
        when(carritoCompraRepository.save(any(CarritoCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        AplicarCuponResponse resultado = carritoService.aplicarCupon(idCarrito, codigo);

        assertNotNull(resultado);
        assertEquals(codigo, resultado.getCodigo());
        assertEquals(1000.0, resultado.getDescuento());

        verify(cuponDescuentoService, times(1)).aplicarCupon(codigo, 10000.0);
        verify(carritoCompraRepository, times(1)).save(any(CarritoCompra.class));
    }
    @Test
    void agregarItem_carritoNoExiste_lanzaRecursoNoEncontrado() {
        // Regla de negocio: el carrito debe existir para poder agregar productos.
        // Escenario: cliente intenta agregar una botella de agua reutilizable a un carrito inexistente.
        Long idCarrito = 999L;
        AgregarItemCarritoRequest req = itemRequest(102L, "Botella de agua reutilizable 750ml", 1, 8990.0, 10);

        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> carritoService.agregarItem(idCarrito, req));

        verify(carritoCompraRepository, never()).save(any(CarritoCompra.class));
    }

    @Test
    void actualizarCantidad_OK() {
        // Regla de negocio (Acciones del Cliente): el cliente puede modificar cantidades en su carrito.
        // Escenario: cliente de tienda Lastarria cambia de 1 a 5 unidades de cepillo de bambú.
        Long idCarrito = 1L;
        Long idItem = 10L;
        CarritoCompra carrito = new CarritoCompra();
        carrito.setIdCarrito(idCarrito);
        carrito.setIdCliente(10L);
        carrito.setEstado(EstadoCarrito.ACTIVO);
        ItemCarrito item = new ItemCarrito();
        item.setIdItem(idItem);
        item.setIdProducto(201L);
        item.setNombreProducto("Cepillo de dientes de bambú biodegradable");
        item.setCantidad(1);
        item.setPrecioUnitario(2990.0);
        item.recalcularSubtotal();
        carrito.setItems(new java.util.ArrayList<>(java.util.List.of(item)));
        carrito.recalcularTotales();

        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));
        when(carritoCompraRepository.save(any(CarritoCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        com.ecomarket.pedidos.dto.ActualizarCantidadRequest req = new com.ecomarket.pedidos.dto.ActualizarCantidadRequest();
        req.setCantidad(5);
        req.setStockDisponible(20);

        carritoService.actualizarCantidad(idCarrito, idItem, req);

        assertEquals(5, item.getCantidad());
        assertEquals(14950.0, item.getSubtotal(), 0.01); // 5 × $2.990
        verify(carritoCompraRepository, times(1)).save(any(CarritoCompra.class));
    }

    @Test
    void eliminarItem_OK() {
        // Regla de negocio (Acciones del Cliente): el cliente puede eliminar productos de su carrito.
        // Escenario: cliente decide quitar una bolsa de tela reutilizable de su carrito antes de finalizar la compra.
        Long idCarrito = 1L;
        Long idItem = 10L;
        CarritoCompra carrito = new CarritoCompra();
        carrito.setIdCarrito(idCarrito);
        carrito.setIdCliente(10L);
        carrito.setEstado(EstadoCarrito.ACTIVO);
        ItemCarrito item = new ItemCarrito();
        item.setIdItem(idItem);
        item.setIdProducto(150L);
        item.setNombreProducto("Bolsa de tela reutilizable estampada");
        item.setCantidad(2);
        item.setPrecioUnitario(4990.0);
        item.recalcularSubtotal();
        carrito.setItems(new java.util.ArrayList<>(java.util.List.of(item)));
        carrito.recalcularTotales();

        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));
        when(carritoCompraRepository.save(any(CarritoCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        carritoService.eliminarItem(idCarrito, idItem);

        verify(itemCarritoRepository, times(1)).delete(item);
        verify(carritoCompraRepository, times(1)).save(any(CarritoCompra.class));
    }

    @Test
    void aplicarCupon_OK() {
        Long idCarrito = 1L;
        CarritoCompra carrito = new CarritoCompra();
        carrito.setIdCarrito(idCarrito);
        carrito.setIdCliente(10L);
        carrito.setEstado(EstadoCarrito.ACTIVO);
        carrito.setItems(new java.util.ArrayList<>());
        carrito.recalcularTotales();

        when(carritoCompraRepository.findById(idCarrito)).thenReturn(Optional.of(carrito));
        when(carritoCompraRepository.save(any(CarritoCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        com.ecomarket.pedidos.dto.AplicarCuponResponse cuponResp = new com.ecomarket.pedidos.dto.AplicarCuponResponse();
        cuponResp.setCodigo("VERDE10");
        cuponResp.setDescuento(100.0);
        cuponResp.setTotalFinal(900.0);
        when(cuponDescuentoService.aplicarCupon("VERDE10", 0.0)).thenReturn(cuponResp);

        com.ecomarket.pedidos.dto.AplicarCuponResponse resultado = carritoService.aplicarCupon(idCarrito, "VERDE10");

        assertEquals("VERDE10", resultado.getCodigo());
        assertEquals(100.0, resultado.getDescuento());
    }

    @Test
    void toResponse_carritoConItems_mapeaTodosLosCampos() {
        // Regla de negocio (Acciones del Cliente): el cliente aplica cupón de descuento ECO10 en su carrito.
        // Escenario: carrito con bolsas biodegradables y cupón ECO10 aplicado correctamente.
        CarritoCompra carrito = new CarritoCompra();
        carrito.setIdCarrito(1L);
        carrito.setIdCliente(10L);
        carrito.setEstado(EstadoCarrito.ACTIVO);
        carrito.setDescuentoAplicado(1990.0);
        carrito.setCodigoCuponAplicado("ECO10");
        ItemCarrito item = new ItemCarrito();
        item.setIdItem(1L);
        item.setIdProducto(100L);
        item.setNombreProducto("Bolsa biodegradable mediana");
        item.setCantidad(2);
        item.setPrecioUnitario(1990.0);
        item.recalcularSubtotal();
        carrito.setItems(new java.util.ArrayList<>(java.util.List.of(item)));
        carrito.recalcularTotales();

        CarritoResponse resp = carritoService.toResponse(carrito);

        assertEquals(1L, resp.getIdCarrito());
        assertEquals(10L, resp.getIdCliente());
        assertEquals(EstadoCarrito.ACTIVO, resp.getEstado());
        assertEquals(1990.0, resp.getDescuentoAplicado());
        assertEquals("ECO10", resp.getCodigoCuponAplicado());
        assertNotNull(resp.getItems());
        assertEquals(1, resp.getItems().size());
        assertEquals(100L, resp.getItems().get(0).getIdProducto());
        assertEquals("Bolsa biodegradable mediana", resp.getItems().get(0).getNombreProducto());
    }

    @Test
    void listarCarritos_retornaLista() {
        // Regla de negocio (Administrador / Empleado): puede listar todos los carritos activos del sistema.
        // Escenario: empleado de ventas consulta los carritos activos de la tienda Lastarria.
        CarritoCompra c = carritoActivoVacio(1L, 10L);
        when(carritoCompraRepository.findAll()).thenReturn(java.util.List.of(c));

        java.util.List<CarritoCompra> resultado = carritoService.listarCarritos();

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).getIdCliente());
        assertEquals(EstadoCarrito.ACTIVO, resultado.get(0).getEstado());
    }

    @Test
    void actualizarCantidad_itemNoEncontrado_lanzaExcepcion() {
        // Regla de negocio: no se puede actualizar un item que no existe en el carrito.
        // Escenario: cliente intenta modificar un item inexistente (borrado previamente por otra sesión).
        CarritoCompra carrito = carritoActivoVacio(1L, 10L);
        when(carritoCompraRepository.findById(1L)).thenReturn(java.util.Optional.of(carrito));

        com.ecomarket.pedidos.dto.ActualizarCantidadRequest req = new com.ecomarket.pedidos.dto.ActualizarCantidadRequest();
        req.setCantidad(3);
        req.setStockDisponible(10);

        assertThrows(IllegalArgumentException.class,
                () -> carritoService.actualizarCantidad(1L, 999L, req));
    }

    @Test
    void eliminarItem_itemNoEncontrado_lanzaExcepcion() {
        // Regla de negocio: no se puede eliminar un item que no existe en el carrito.
        // Escenario: item ya fue eliminado en otra sesión; el sistema protege la integridad del carrito.
        CarritoCompra carrito = carritoActivoVacio(1L, 10L);
        when(carritoCompraRepository.findById(1L)).thenReturn(java.util.Optional.of(carrito));

        assertThrows(IllegalArgumentException.class,
                () -> carritoService.eliminarItem(1L, 999L));
    }

    @Test
    void toResponse_null_retornaNull() {
        // Cubre linea 152: toResponse(null)
        CarritoResponse resp = carritoService.toResponse(null);
        assertTrue(resp == null);
    }
}
