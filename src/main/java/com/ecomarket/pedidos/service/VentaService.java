package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.dto.CrearFacturaRequest;
import com.ecomarket.pedidos.dto.VentaResponse;
import com.ecomarket.pedidos.dto.FacturaResponse;
import com.ecomarket.pedidos.dto.CrearVentaRequest;
import com.ecomarket.pedidos.model.Factura;
import com.ecomarket.pedidos.model.Venta;
import com.ecomarket.pedidos.repository.FacturaRepository;
import com.ecomarket.pedidos.repository.VentaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Servicio de ventas y facturas.
 * Contiene la lÃ³gica de negocio para registrar ventas presenciales y generar facturas.
 * El folio de factura se obtiene de la BD para garantizar consecutividad entre reinicios.
 */
@Service
public class VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaService.class);

    private final VentaRepository ventaRepository;
    private final FacturaRepository facturaRepository;
    private final InventarioClientService inventarioClientService;

    public VentaService(VentaRepository ventaRepository, FacturaRepository facturaRepository,
                        InventarioClientService inventarioClientService) {
        this.ventaRepository = ventaRepository;
        this.facturaRepository = facturaRepository;
        this.inventarioClientService = inventarioClientService;
    }

    @Transactional
    public Venta registrarVentaPresencial(CrearVentaRequest request) {
        log.info("Registrando venta presencial. idCliente={}", request.getIdCliente());

        double subtotal = request.getItems().stream()
                .mapToDouble(i -> i.getCantidad() * i.getPrecioUnitario())
                .sum();
        double descuento = request.getDescuento() != null ? request.getDescuento() : 0.0;

        if (descuento < 0) {
            throw new IllegalArgumentException("El descuento no puede ser negativo");
        }
        if (descuento > subtotal) {
            throw new IllegalArgumentException("El descuento no puede superar el subtotal de la venta");
        }

        double total = subtotal - descuento;
        // IE 2.2.1: Calculo de IVA (19%) sobre la base imponible.
        double iva = Math.round(subtotal * 0.19 * 100.0) / 100.0;

        Venta venta = new Venta();
        venta.setIdCliente(request.getIdCliente());
        venta.setIdPedido(request.getIdPedido());
        venta.setMetodoPago(request.getMetodoPago());
        venta.setSubtotal(subtotal);
        venta.setDescuento(descuento);
        venta.setIva(iva);
        venta.setTotal(total);
        venta.setObservaciones(request.getObservaciones());

        Venta guardada = ventaRepository.save(venta);

        // IE 2.2.1: Validar y descontar stock en MS Inventario despues de registrar la venta.
        // Si la validacion falla, se hace rollback por la excepcion.
        for (var item : request.getItems()) {
            Map<String, Object> inv = inventarioClientService.consultarStock(item.getIdProducto());
            if (inv == null) {
                throw new IllegalArgumentException("El producto " + item.getIdProducto() + " no existe en el inventario");
            }
            Object stockObj = inv.get("stockActual");
            if (stockObj == null) stockObj = inv.get("stock");
            Integer stockActual = stockObj == null ? null
                    : (stockObj instanceof Number n ? n.intValue() : Integer.parseInt(stockObj.toString()));
            if (stockActual != null && item.getCantidad() > stockActual) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para el producto " + item.getIdProducto()
                                + ". Solicitado: " + item.getCantidad() + ", disponible: " + stockActual);
            }
        }
        for (var item : request.getItems()) {
            boolean descontado = inventarioClientService.descontarStock(item.getIdProducto(), item.getCantidad(),
                    "Venta presencial id=" + guardada.getIdVenta());
            if (!descontado) {
                log.warn("No se pudo descontar stock para idProducto={}, idVenta={}",
                        item.getIdProducto(), guardada.getIdVenta());
            }
        }

        log.info("Venta presencial registrada correctamente. idVenta={}, total={}", guardada.getIdVenta(), guardada.getTotal());
        return guardada;
    }

    @Transactional(readOnly = true)
    public Venta obtenerVenta(Long idVenta) {
        log.info("Consultando venta. idVenta={}", idVenta);
        return ventaRepository.findById(idVenta)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada: " + idVenta));
    }

    @Transactional(readOnly = true)
    public List<Venta> listarVentas() {
        log.info("Listando todas las ventas");
        return ventaRepository.findAll();
    }

    @Transactional
    public Venta actualizarVenta(Long idVenta, CrearVentaRequest request) {
        log.info("Actualizando venta. idVenta={}", idVenta);
        Venta venta = obtenerVenta(idVenta);

        double subtotal = request.getItems().stream()
                .mapToDouble(i -> i.getCantidad() * i.getPrecioUnitario())
                .sum();
        double descuento = request.getDescuento() != null ? request.getDescuento() : 0.0;
        double total = subtotal - descuento;
        double iva = Math.round(subtotal * 0.19 * 100.0) / 100.0;

        venta.setMetodoPago(request.getMetodoPago());
        venta.setSubtotal(subtotal);
        venta.setDescuento(descuento);
        venta.setIva(iva);
        venta.setTotal(total);
        venta.setObservaciones(request.getObservaciones());

        log.info("Venta actualizada. idVenta={}", idVenta);
        return ventaRepository.save(venta);
    }

    @Transactional
    public void eliminarVenta(Long idVenta) {
        log.info("Eliminando venta. idVenta={}", idVenta);
        Venta venta = obtenerVenta(idVenta);
        ventaRepository.delete(venta);
        log.info("Venta eliminada correctamente. idVenta={}", idVenta);
    }

    @Transactional
    public Factura generarFactura(Long idVenta, CrearFacturaRequest request) {
        log.info("Generando factura para venta. idVenta={}", idVenta);
        Venta venta = obtenerVenta(idVenta);

        if (facturaRepository.existsByIdVenta(idVenta)) {
            log.warn("Intento de generar factura duplicada. idVenta={}", idVenta);
            throw new IllegalStateException("La venta ya tiene una factura asociada");
        }

        // Folio consecutivo obtenido desde BD â€” no se resetea entre reinicios
        int siguienteFolio = facturaRepository.findMaxFolio().orElse(1000) + 1;

        Factura factura = new Factura();
        factura.setIdVenta(idVenta);
        factura.setIdCliente(venta.getIdCliente());
        factura.setRutCliente(request.getRutCliente());
        factura.setRazonSocial(request.getRazonSocial());
        factura.setFolio(siguienteFolio);
        factura.setSubtotal(venta.getTotal());

        Factura guardada = facturaRepository.save(factura);
        log.info("Factura generada correctamente. idFactura={}, folio={}, idVenta={}",
                guardada.getIdFactura(), guardada.getFolio(), idVenta);
        return guardada;
    }

    @Transactional(readOnly = true)
    public Factura obtenerFactura(Long idFactura) {
        log.info("Consultando factura. idFactura={}", idFactura);
        return facturaRepository.findById(idFactura)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada: " + idFactura));
    }


    public VentaResponse toResponse(Venta venta) {
        if (venta == null) {
            return null;
        }
        VentaResponse r = new VentaResponse();
        r.setIdVenta(venta.getIdVenta());
        r.setIdCliente(venta.getIdCliente());
        r.setIdPedido(venta.getIdPedido());
        r.setMetodoPago(venta.getMetodoPago());
        r.setSubtotal(venta.getSubtotal());
        r.setDescuento(venta.getDescuento());
        r.setTotal(venta.getTotal());
        r.setIva(venta.getIva());
        r.setObservaciones(venta.getObservaciones());
        r.setFechaVenta(venta.getFechaVenta());
        return r;
    }

    public FacturaResponse toResponse(Factura factura) {
        if (factura == null) {
            return null;
        }
        FacturaResponse r = new FacturaResponse();
        r.setIdFactura(factura.getIdFactura());
        r.setIdVenta(factura.getIdVenta());
        r.setIdCliente(factura.getIdCliente());
        r.setRutCliente(factura.getRutCliente());
        r.setRazonSocial(factura.getRazonSocial());
        r.setFolio(factura.getFolio());
        r.setSubtotal(factura.getSubtotal());
        r.setIva(factura.getIva());
        r.setTotal(factura.getTotal());
        r.setEstado(factura.getEstado());
        r.setFechaEmision(factura.getFechaEmision());
        return r;
    }
}