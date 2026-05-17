package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.dto.CrearPedidoRequest;
import com.ecomarket.pedidos.dto.CrearReclamacionRequest;
import com.ecomarket.pedidos.dto.PedidoResponse;
import com.ecomarket.pedidos.entity.*;
import com.ecomarket.pedidos.repository.CarritoCompraRepository;
import com.ecomarket.pedidos.repository.HistorialPedidoRepository;
import com.ecomarket.pedidos.repository.PedidoRepository;
import com.ecomarket.pedidos.repository.ReclamacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final CarritoCompraRepository carritoCompraRepository;
    private final HistorialPedidoRepository historialPedidoRepository;
    private final ReclamacionRepository reclamacionRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         CarritoCompraRepository carritoCompraRepository,
                         HistorialPedidoRepository historialPedidoRepository,
                         ReclamacionRepository reclamacionRepository) {
        this.pedidoRepository = pedidoRepository;
        this.carritoCompraRepository = carritoCompraRepository;
        this.historialPedidoRepository = historialPedidoRepository;
        this.reclamacionRepository = reclamacionRepository;
    }

    @Transactional
    public Pedido crearDesdeCarrito(Long idCarrito, CrearPedidoRequest request) {
        CarritoCompra carrito = carritoCompraRepository.findById(idCarrito)
                .orElseThrow(() -> new IllegalArgumentException("Carrito no encontrado: " + idCarrito));

        if (carrito.getEstado() != EstadoCarrito.ACTIVO) {
            throw new IllegalArgumentException("El carrito no esta activo");
        }

        if (carrito.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito esta vacio");
        }

        Pedido pedido = new Pedido();
        pedido.setIdCliente(carrito.getIdCliente());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setMetodoPago(request.getMetodoPago());
        pedido.setDireccionEntrega(request.getDireccionEntrega());
        pedido.setObservaciones(request.getObservaciones());
        pedido.setSubtotal(carrito.getSubtotal());
        pedido.setDescuento(carrito.getDescuentoAplicado());
        pedido.setTotal(carrito.getTotal());

        for (ItemCarrito item : carrito.getItems()) {
            DetallePedido detalle = new DetallePedido();
            detalle.setIdProducto(item.getIdProducto());
            detalle.setNombreProducto(item.getNombreProducto());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getPrecioUnitario());
            detalle.calcularSubtotal();
            detalle.setPedido(pedido);
            pedido.getDetalles().add(detalle);
        }

        carrito.setEstado(EstadoCarrito.CONVERTIDO);
        carritoCompraRepository.save(carrito);

        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        registrarHistorial(pedidoGuardado.getIdPedido(), null, EstadoPedido.PENDIENTE, "Pedido creado desde carrito");
        return pedidoGuardado;
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Pedido obtenerPedido(Long idPedido) {
        return pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + idPedido));
    }

    @Transactional
    public Pedido actualizarPedido(Long idPedido, CrearPedidoRequest request) {
        Pedido pedido = obtenerPedido(idPedido);
        pedido.setMetodoPago(request.getMetodoPago());
        pedido.setDireccionEntrega(request.getDireccionEntrega());
        pedido.setObservaciones(request.getObservaciones());
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public void eliminarPedido(Long idPedido) {
        Pedido pedido = obtenerPedido(idPedido);
        pedidoRepository.delete(pedido);
    }

    @Transactional(readOnly = true)
    public EstadoPedido consultarEstado(Long idPedido) {
        return pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + idPedido))
                .getEstado();
    }

    @Transactional(readOnly = true)
    public List<Pedido> historialCliente(Long idCliente) {
        return pedidoRepository.findByIdClienteOrderByFechaCreacionDesc(idCliente);
    }

    @Transactional
    public Pedido cancelarPedido(Long idPedido, String motivo) {
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + idPedido));

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalArgumentException("Solo se pueden cancelar pedidos en estado PENDIENTE");
        }

        EstadoPedido estadoAnterior = pedido.getEstado();
        pedido.setEstado(EstadoPedido.CANCELADO);
        pedido.setObservaciones("Cancelado: " + (motivo != null && !motivo.isEmpty() ? motivo : "sin motivo"));

        Pedido pedidoGuardado = pedidoRepository.save(pedido);
        registrarHistorial(idPedido, estadoAnterior, EstadoPedido.CANCELADO, "Pedido cancelado");
        return pedidoGuardado;
    }

    @Transactional
    public void registrarHistorial(Long idPedido, EstadoPedido estadoAnterior,
                                   EstadoPedido estadoNuevo, String descripcion) {
        HistorialPedido historial = new HistorialPedido();
        historial.setIdPedido(idPedido);
        historial.setEstadoAnterior(estadoAnterior);
        historial.setEstadoNuevo(estadoNuevo);
        historial.setDescripcion(descripcion);
        historialPedidoRepository.save(historial);
    }

    @Transactional(readOnly = true)
    public List<HistorialPedido> listarHistorialPedido(Long idPedido) {
        return historialPedidoRepository.findByIdPedidoOrderByFechaCambioDesc(idPedido);
    }

    @Transactional
    public Reclamacion crearReclamacionPorPedido(Long idPedido, CrearReclamacionRequest request) {
        obtenerPedido(idPedido);
        Reclamacion reclamacion = new Reclamacion();
        reclamacion.setIdCliente(request.getIdCliente());
        reclamacion.setIdPedido(idPedido);
        reclamacion.setIdVenta(request.getIdVenta());
        reclamacion.setMotivo(request.getMotivo());
        reclamacion.setDescripcion(request.getDescripcion());
        return reclamacionRepository.save(reclamacion);
    }

    @Transactional(readOnly = true)
    public List<Reclamacion> listarReclamacionesPorPedido(Long idPedido) {
        return reclamacionRepository.findByIdPedido(idPedido);
    }

    public PedidoResponse toResponse(Pedido pedido) {
        PedidoResponse r = new PedidoResponse();
        r.setIdPedido(pedido.getIdPedido());
        r.setIdCliente(pedido.getIdCliente());
        r.setEstado(pedido.getEstado());
        r.setMetodoPago(pedido.getMetodoPago());
        r.setSubtotal(pedido.getSubtotal());
        r.setDescuento(pedido.getDescuento());
        r.setTotal(pedido.getTotal());
        r.setDireccionEntrega(pedido.getDireccionEntrega());
        r.setObservaciones(pedido.getObservaciones());
        r.setFechaCreacion(pedido.getFechaCreacion());
        return r;
    }
}
