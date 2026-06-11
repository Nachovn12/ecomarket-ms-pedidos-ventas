package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.dto.CrearFacturaRequest;
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

/**
 * Servicio de Ventas y Facturas.
 * Contiene la lógica de negocio para registrar ventas presenciales y generar facturas.
 * El folio de factura se obtiene de la BD para garantizar consecutividad entre reinicios.
 */
@Service
public class VentaService {

    private static final Logger log = LoggerFactory.getLogger(VentaService.class);

    private final VentaRepository ventaRepository;
    private final FacturaRepository facturaRepository;

    public VentaService(VentaRepository ventaRepository, FacturaRepository facturaRepository) {
        this.ventaRepository = ventaRepository;
        this.facturaRepository = facturaRepository;
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

        Venta venta = new Venta();
        venta.setIdCliente(request.getIdCliente());
        venta.setIdPedido(request.getIdPedido());
        venta.setMetodoPago(request.getMetodoPago());
        venta.setSubtotal(subtotal);
        venta.setDescuento(descuento);
        venta.setTotal(total);
        venta.setObservaciones(request.getObservaciones());

        Venta guardada = ventaRepository.save(venta);
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

        venta.setMetodoPago(request.getMetodoPago());
        venta.setSubtotal(subtotal);
        venta.setDescuento(descuento);
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

        // Folio consecutivo obtenido desde BD — no se resetea entre reinicios
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
}
