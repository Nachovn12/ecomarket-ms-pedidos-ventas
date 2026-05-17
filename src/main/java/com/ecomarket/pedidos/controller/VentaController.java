package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.CrearFacturaRequest;
import com.ecomarket.pedidos.dto.CrearVentaRequest;
import com.ecomarket.pedidos.entity.Factura;
import com.ecomarket.pedidos.entity.Venta;
import com.ecomarket.pedidos.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping("/presencial")
    public ResponseEntity<EntityModel<Venta>> registrarVentaPresencial(
            @Valid @RequestBody CrearVentaRequest request) {
        Venta venta = ventaService.registrarVentaPresencial(request);
        EntityModel<Venta> model = EntityModel.of(venta);
        model.add(linkTo(methodOn(VentaController.class)
                .obtenerVenta(venta.getIdVenta())).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @GetMapping("/{idVenta}")
    public ResponseEntity<EntityModel<Venta>> obtenerVenta(@PathVariable Long idVenta) {
        Venta venta = ventaService.obtenerVenta(idVenta);
        EntityModel<Venta> model = EntityModel.of(venta);
        model.add(linkTo(methodOn(VentaController.class)
                .obtenerVenta(idVenta)).withSelfRel());
        return ResponseEntity.ok(model);
    }

    @PostMapping("/{idVenta}/factura")
    public ResponseEntity<EntityModel<Factura>> generarFactura(
            @PathVariable Long idVenta,
            @RequestBody(required = false) CrearFacturaRequest request) {
        if (request == null) request = new CrearFacturaRequest();
        Factura factura = ventaService.generarFactura(idVenta, request);
        EntityModel<Factura> model = EntityModel.of(factura);
        model.add(linkTo(methodOn(VentaController.class)
                .generarFactura(idVenta, request)).withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    @GetMapping("/facturas/{idFactura}")
    public ResponseEntity<EntityModel<Factura>> obtenerFactura(@PathVariable Long idFactura) {
        Factura factura = ventaService.obtenerFactura(idFactura);
        EntityModel<Factura> model = EntityModel.of(factura);
        model.add(linkTo(methodOn(VentaController.class)
                .obtenerFactura(idFactura)).withSelfRel());
        return ResponseEntity.ok(model);
    }
}