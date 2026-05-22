package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.dto.CrearFacturaRequest;
import com.ecomarket.pedidos.dto.CrearVentaRequest;
import com.ecomarket.pedidos.entity.Factura;
import com.ecomarket.pedidos.entity.Venta;
import com.ecomarket.pedidos.service.VentaService;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Venta>>> listarVentas() {
        List<EntityModel<Venta>> ventas = ventaService.listarVentas()
                .stream()
                .map(v -> {
                    EntityModel<Venta> model = EntityModel.of(v);
                    model.add(linkTo(methodOn(VentaController.class)
                            .obtenerVenta(v.getIdVenta())).withSelfRel());
                    return model;
                })
                .collect(Collectors.toList());
        CollectionModel<EntityModel<Venta>> collection = CollectionModel.of(ventas);
        collection.add(linkTo(methodOn(VentaController.class).listarVentas()).withSelfRel());
        return ResponseEntity.ok(collection);
    }

    @GetMapping("/{idVenta}")
    public ResponseEntity<EntityModel<Venta>> obtenerVenta(@PathVariable Long idVenta) {
        Venta venta = ventaService.obtenerVenta(idVenta);
        EntityModel<Venta> model = EntityModel.of(venta);
        model.add(linkTo(methodOn(VentaController.class)
                .obtenerVenta(idVenta)).withSelfRel());
        return ResponseEntity.ok(model);
    }

    @PutMapping("/{idVenta}")
    public ResponseEntity<EntityModel<Venta>> actualizarVenta(
            @PathVariable Long idVenta,
            @Valid @RequestBody CrearVentaRequest request) {
        Venta venta = ventaService.actualizarVenta(idVenta, request);
        EntityModel<Venta> model = EntityModel.of(venta);
        model.add(linkTo(methodOn(VentaController.class)
                .obtenerVenta(idVenta)).withSelfRel());
        return ResponseEntity.ok(model);
    }

    @DeleteMapping("/{idVenta}")
    public ResponseEntity<Void> eliminarVenta(@PathVariable Long idVenta) {
        ventaService.eliminarVenta(idVenta);
        return ResponseEntity.noContent().build();
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