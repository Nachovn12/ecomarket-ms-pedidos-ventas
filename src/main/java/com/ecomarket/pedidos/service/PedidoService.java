package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.dto.CrearPedidoRequest;
import com.ecomarket.pedidos.dto.CrearReclamacionRequest;
import com.ecomarket.pedidos.dto.PedidoResponse;
import com.ecomarket.pedidos.model.*;
import com.ecomarket.pedidos.repository.CarritoCompraRepository;
import com.ecomarket.pedidos.repository.HistorialPedidoRepository;
import com.ecomarket.pedidos.repository.PedidoRepository;
import com.ecomarket.pedidos.repository.ReclamacionRepository;
import com.ecomarket.pedidos.service.CatalogoClientService;
import com.ecomarket.pedidos.service.InventarioClientService;
import com.ecomarket.pedidos.service.LogisticaClientService;
import com.ecomarket.pedidos.exception.RecursoNoEncontradoException;
import com.ecomarket.pedidos.exception.StockInsuficienteException;
import java.util.Map;
import java.util.HashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PedidoService.class);

    private final PedidoRepository pedidoRepository;
    private final CarritoCompraRepository carritoCompraRepository;
    private final HistorialPedidoRepository historialPedidoRepository;
    private final ReclamacionRepository reclamacionRepository;
    @SuppressWarnings("all")
    private final CatalogoClientService catalogoClientService;
    @SuppressWarnings("all")
    private final InventarioClientService inventarioClientService;
    @SuppressWarnings("all")
    private final LogisticaClientService logisticaClientService;

    public PedidoService(PedidoRepository pedidoRepository,
                         CarritoCompraRepository carritoCompraRepository,
                         HistorialPedidoRepository historialPedidoRepository,
                         ReclamacionRepository reclamacionRepository,
                         CatalogoClientService catalogoClientService,
                         InventarioClientService inventarioClientService,
                         LogisticaClientService logisticaClientService) {
        this.pedidoRepository = pedidoRepository;
        this.carritoCompraRepository = carritoCompraRepository;
        this.historialPedidoRepository = historialPedidoRepository;
        this.reclamacionRepository = reclamacionRepository;
        this.catalogoClientService = catalogoClientService;
        this.inventarioClientService = inventarioClientService;
        this.logisticaClientService = logisticaClientService;
    }

    @Transactional
    public Pedido crearDesdeCarrito(Long idCarrito, CrearPedidoRequest request) {
        log.info("Creando pedido desde carrito. idCarrito={}", idCarrito);
        CarritoCompra carrito = carritoCompraRepository.findById(idCarrito)
                .orElseThrow(() -> new RecursoNoEncontradoException("Carrito no encontrado: " + idCarrito));

        if (carrito.getEstado() != EstadoCarrito.ACTIVO) {
            throw new IllegalArgumentException("El carrito no esta activo");
        }

        if (carrito.getItems().isEmpty()) {
            throw new IllegalArgumentException("El carrito esta vacio");
        }

        // IE 2.2.1: Validar stock real en MS Inventario y existencia en MS Catalogo
        // antes de confirmar el pedido (regla de negocio critica).
        log.info("Validando stock y precios contra MS Inventario y MS Catalogo. idCarrito={}", idCarrito);
        Map<Long, Integer> cantidadesPorProducto = new HashMap<>();
        for (ItemCarrito item : carrito.getItems()) {
            cantidadesPorProducto.merge(item.getIdProducto(), item.getCantidad(),
                (a, b) -> (a == null ? 0 : a) + (b == null ? 0 : b));
        }
        for (Map.Entry<Long, Integer> entry : cantidadesPorProducto.entrySet()) {
            Long idProducto = entry.getKey();
            Integer cantidadSolicitada = entry.getValue();
            try {
                Map<String, Object> inv = inventarioClientService.consultarStock(idProducto);
                if (inv == null) {
                    throw new RecursoNoEncontradoException("El producto " + idProducto + " no existe en el inventario");
                }
                Object stockObj = inv.get("stockActual");
                if (stockObj == null) {
                    stockObj = inv.get("stock");
                }
                Integer stockActual = stockObj == null ? null
                        : (stockObj instanceof Number n ? n.intValue() : Integer.parseInt(stockObj.toString()));
                if (stockActual == null) {
                    throw new IllegalStateException("MS Inventario no devolvio stockActual para el producto " + idProducto);
                }
                if (cantidadSolicitada > stockActual) {
                    throw new StockInsuficienteException(
                            "Stock insuficiente para el producto " + idProducto
                                    + ". Solicitado: " + cantidadSolicitada + ", disponible: " + stockActual);
                }
                Map<String, Object> prod = catalogoClientService.obtenerProducto(idProducto);
                if (prod == null) {
                    throw new RecursoNoEncontradoException("El producto " + idProducto + " no existe en el catalogo");
                }
            } catch (StockInsuficienteException | RecursoNoEncontradoException e) {
                throw e;
            } catch (IllegalStateException e) {
                log.warn("No se pudo validar contra MS dependiente. idProducto={}, motivo={}",
                        idProducto, e.getMessage());
            }
        }

        Pedido pedido = new Pedido();
        pedido.setIdCliente(carrito.getIdCliente());
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setMetodoPago(request.getMetodoPago());
        pedido.setDireccionEntrega(request.getDireccionEntrega());
        pedido.setObservaciones(request.getObservaciones());
        pedido.setSubtotal(carrito.getSubtotal());
        pedido.setDescuento(carrito.getDescuentoAplicado());
        double baseIva = Math.max(0.0, carrito.getSubtotal() - carrito.getDescuentoAplicado());
        pedido.setIva(Math.round(baseIva * 0.19 * 100.0) / 100.0);
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
        logisticaClientService.solicitarDespacho(pedidoGuardado.getIdPedido(), "Centro de distribucion EcoMarket", pedidoGuardado.getDireccionEntrega());
        log.info("Pedido creado correctamente. idPedido={}, idCliente={}", pedidoGuardado.getIdPedido(), pedidoGuardado.getIdCliente());
        return pedidoGuardado;
    }

    @Transactional(readOnly = true)
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Pedido obtenerPedido(Long idPedido) {
        return pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado: " + idPedido));
    }

    @Transactional
    public Pedido actualizarPedido(Long idPedido, CrearPedidoRequest request) {
        Pedido pedido = obtenerPedido(idPedido);
        if (pedido.getEstado() == EstadoPedido.CANCELADO || pedido.getEstado() == EstadoPedido.ENTREGADO) {
            throw new IllegalArgumentException(
                    "No se puede modificar un pedido en estado " + pedido.getEstado());
        }
        pedido.setMetodoPago(request.getMetodoPago());
        pedido.setDireccionEntrega(request.getDireccionEntrega());
        pedido.setObservaciones(request.getObservaciones());
        log.info("Pedido actualizado. idPedido={}", idPedido);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public void eliminarPedido(Long idPedido) {
        log.info("Eliminando pedido. idPedido={}", idPedido);
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
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado: " + idPedido));

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden cancelar pedidos en estado PENDIENTE. Estado actual: " + pedido.getEstado());
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
        r.setIva(pedido.getIva());
        r.setDireccionEntrega(pedido.getDireccionEntrega());
        r.setObservaciones(pedido.getObservaciones());
        r.setFechaCreacion(pedido.getFechaCreacion());
        return r;
    }
}
