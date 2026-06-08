package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.dto.CrearFacturaRequest;
import com.ecomarket.pedidos.dto.CrearVentaRequest;
import com.ecomarket.pedidos.model.Factura;
import com.ecomarket.pedidos.model.Venta;
import com.ecomarket.pedidos.repository.FacturaRepository;
import com.ecomarket.pedidos.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final FacturaRepository facturaRepository;
    private static final AtomicInteger folioCounter = new AtomicInteger(1000);

    public VentaService(VentaRepository ventaRepository, FacturaRepository facturaRepository) {
        this.ventaRepository = ventaRepository;
        this.facturaRepository = facturaRepository;
    }

    @Transactional
    public Venta registrarVentaPresencial(CrearVentaRequest request) {
        double subtotal = request.getItems().stream()
                .mapToDouble(i -> i.getCantidad() * i.getPrecioUnitario())
                .sum();
        double descuento = request.getDescuento() != null ? request.getDescuento() : 0.0;
        double total = subtotal - descuento;

        Venta venta = new Venta();
        venta.setIdCliente(request.getIdCliente());
        venta.setIdPedido(request.getIdPedido());
        venta.setMetodoPago(request.getMetodoPago());
        venta.setSubtotal(subtotal);
        venta.setDescuento(descuento);
        venta.setTotal(total);
        venta.setObservaciones(request.getObservaciones());
        return ventaRepository.save(venta);
    }

    public Venta obtenerVenta(Long idVenta) {
        return ventaRepository.findById(idVenta)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada: " + idVenta));
    }

    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    @Transactional
    public Venta actualizarVenta(Long idVenta, CrearVentaRequest request) {
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
        return ventaRepository.save(venta);
    }

    @Transactional
    public void eliminarVenta(Long idVenta) {
        Venta venta = obtenerVenta(idVenta);
        ventaRepository.delete(venta);
    }

    @Transactional
    public Factura generarFactura(Long idVenta, CrearFacturaRequest request) {
        Venta venta = obtenerVenta(idVenta);
        if (facturaRepository.existsByIdVenta(idVenta)) {
            throw new IllegalStateException("La venta ya tiene una factura asociada");
        }
        Factura factura = new Factura();
        factura.setIdVenta(idVenta);
        factura.setIdCliente(venta.getIdCliente());
        factura.setRutCliente(request.getRutCliente());
        factura.setRazonSocial(request.getRazonSocial());
        factura.setFolio(folioCounter.incrementAndGet());
        factura.setSubtotal(venta.getTotal());
        return facturaRepository.save(factura);
    }

    public Factura obtenerFactura(Long idFactura) {
        return facturaRepository.findById(idFactura)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada: " + idFactura));
    }
}
