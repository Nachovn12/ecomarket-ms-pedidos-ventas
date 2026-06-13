package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.dto.ActualizarCantidadRequest;
import com.ecomarket.pedidos.dto.AgregarItemCarritoRequest;
import com.ecomarket.pedidos.dto.AplicarCuponResponse;
import com.ecomarket.pedidos.model.CarritoCompra;
import com.ecomarket.pedidos.model.EstadoCarrito;
import com.ecomarket.pedidos.model.ItemCarrito;
import com.ecomarket.pedidos.repository.CarritoCompraRepository;
import com.ecomarket.pedidos.repository.ItemCarritoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.ecomarket.pedidos.dto.CarritoResponse;
import com.ecomarket.pedidos.dto.ItemCarritoResponse;

@Service
public class CarritoService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CarritoService.class);

    private final CarritoCompraRepository carritoCompraRepository;
    private final ItemCarritoRepository itemCarritoRepository;
    private final CuponDescuentoService cuponDescuentoService;

    public CarritoService(
            CarritoCompraRepository carritoCompraRepository,
            ItemCarritoRepository itemCarritoRepository,
            CuponDescuentoService cuponDescuentoService
    ) {
        this.carritoCompraRepository = carritoCompraRepository;
        this.itemCarritoRepository = itemCarritoRepository;
        this.cuponDescuentoService = cuponDescuentoService;
    }

    public CarritoCompra crearCarrito(Long idCliente) {
        log.info("Creando carrito para cliente. idCliente={}", idCliente);
        CarritoCompra carrito = new CarritoCompra();
        carrito.setIdCliente(idCliente);
        carrito.setEstado(EstadoCarrito.ACTIVO);
        carrito.recalcularTotales();
        CarritoCompra guardado = carritoCompraRepository.save(carrito);
        log.info("Carrito creado correctamente. idCarrito={}, idCliente={}", guardado.getIdCarrito(), idCliente);
        return guardado;
    }

    public List<CarritoCompra> listarCarritos() {
        return carritoCompraRepository.findAll();
    }

    public CarritoCompra obtenerCarrito(Long idCarrito) {
        return carritoCompraRepository.findById(idCarrito)
                .orElseThrow(() -> new IllegalArgumentException("Carrito no encontrado"));
    }

    @Transactional
    public CarritoCompra agregarItem(Long idCarrito, AgregarItemCarritoRequest request) {
        validarStock(request.getCantidad(), request.getStockDisponible());
        CarritoCompra carrito = obtenerCarrito(idCarrito);
        validarCarritoActivo(carrito);

        ItemCarrito item = new ItemCarrito();
        item.setIdProducto(request.getIdProducto());
        item.setNombreProducto(request.getNombreProducto());
        item.setCantidad(request.getCantidad());
        item.setPrecioUnitario(request.getPrecioUnitario());
        item.recalcularSubtotal();

        carrito.agregarItem(item);
        limpiarCuponAplicado(carrito);
        carrito.recalcularTotales();

        return carritoCompraRepository.save(carrito);
    }

    @Transactional
    public CarritoCompra actualizarCantidad(Long idCarrito, Long idItem, ActualizarCantidadRequest request) {
        validarStock(request.getCantidad(), request.getStockDisponible());
        CarritoCompra carrito = obtenerCarrito(idCarrito);
        validarCarritoActivo(carrito);

        ItemCarrito item = carrito.getItems().stream()
                .filter(i -> i.getIdItem().equals(idItem))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado en el carrito"));

        item.setCantidad(request.getCantidad());
        item.recalcularSubtotal();

        limpiarCuponAplicado(carrito);
        carrito.recalcularTotales();

        return carritoCompraRepository.save(carrito);
    }

    @Transactional
    public CarritoCompra eliminarItem(Long idCarrito, Long idItem) {
        CarritoCompra carrito = obtenerCarrito(idCarrito);
        validarCarritoActivo(carrito);

        ItemCarrito item = carrito.getItems().stream()
                .filter(i -> i.getIdItem().equals(idItem))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item no encontrado en el carrito"));

        carrito.eliminarItem(item);
        itemCarritoRepository.delete(item);
        limpiarCuponAplicado(carrito);
        carrito.recalcularTotales();

        return carritoCompraRepository.save(carrito);
    }

    @Transactional
    public AplicarCuponResponse aplicarCupon(Long idCarrito, String codigo) {
        CarritoCompra carrito = obtenerCarrito(idCarrito);
        validarCarritoActivo(carrito);
        carrito.recalcularTotales();

        AplicarCuponResponse response = cuponDescuentoService.aplicarCupon(codigo, carrito.getSubtotal());

        carrito.setDescuentoAplicado(response.getDescuento());
        carrito.setCodigoCuponAplicado(response.getCodigo());
        carrito.recalcularTotales();
        carritoCompraRepository.save(carrito);

        return response;
    }

    private void validarCarritoActivo(CarritoCompra carrito) {
        if (carrito.getEstado() != EstadoCarrito.ACTIVO) {
            throw new IllegalArgumentException("El carrito no esta activo");
        }
    }

    private void validarStock(Integer cantidadSolicitada, Integer stockDisponible) {
        if (stockDisponible != null && cantidadSolicitada > stockDisponible) {
            throw new IllegalArgumentException("La cantidad solicitada supera el stock disponible");
        }
    }

    private void limpiarCuponAplicado(CarritoCompra carrito) {
        carrito.setDescuentoAplicado(0.0);
        carrito.setCodigoCuponAplicado(null);
    }


    public CarritoResponse toResponse(CarritoCompra carrito) {
        if (carrito == null) {
            return null;
        }
        CarritoResponse r = new CarritoResponse();
        r.setIdCarrito(carrito.getIdCarrito());
        r.setIdCliente(carrito.getIdCliente());
        r.setEstado(carrito.getEstado());
        r.setSubtotal(carrito.getSubtotal());
        r.setDescuentoAplicado(carrito.getDescuentoAplicado());
        r.setTotal(carrito.getTotal());
        r.setCodigoCuponAplicado(carrito.getCodigoCuponAplicado());
        r.setFechaCreacion(carrito.getFechaCreacion());
        r.setFechaActualizacion(carrito.getFechaActualizacion());
        if (carrito.getItems() != null) {
            r.setItems(carrito.getItems().stream()
                    .map(this::toItemResponse)
                    .toList());
        }
        return r;
    }

    public ItemCarritoResponse toItemResponse(ItemCarrito item) {
        ItemCarritoResponse r = new ItemCarritoResponse();
        r.setIdItem(item.getIdItem());
        r.setIdProducto(item.getIdProducto());
        r.setNombreProducto(item.getNombreProducto());
        r.setCantidad(item.getCantidad());
        r.setPrecioUnitario(item.getPrecioUnitario());
        r.setSubtotal(item.getSubtotal());
        return r;
    }
}