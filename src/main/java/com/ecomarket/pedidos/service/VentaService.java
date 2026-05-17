package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.dto.CrearFacturaRequest;
import com.ecomarket.pedidos.dto.CrearVentaRequest;
import com.ecomarket.pedidos.entity.Factura;
import com.ecomarket.pedidos.entity.Venta;
import com.ecomarket.pedidos.repository.FacturaRepository;
import com.ecomarket.pedidos.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        Venta venta = new Venta();
        venta.setIdCliente(request.getIdCliente());
        venta.setIdPedido(request.getIdPedido());
        venta.setMetodoPago(request.getMetodoPago());
        venta.setSubtotal(request.getSubtotal() != null ? request.getSubtotal() : request.getTotal());
        venta.setDescuento(request.getDescuento() != null ? request.getDescuento() : 0.0);
        venta.setTotal(request.getTotal());
        venta.setObservaciones(request.getObservaciones());
        return ventaRepository.save(venta);
    }

    public Venta obtenerVenta(Long idVenta) {
        return ventaRepository.findById(idVenta)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada: " + idVenta));
    }

    @Transactional
    public Factura generarFactura(Long idVenta, CrearFacturaRequest request) {
        Venta venta = obtenerVenta(idVenta);

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