package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.entity.ItemCarrito;
import com.ecomarket.pedidos.service.CarritoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pedidos/carritos") // Ruta alineada con el API Gateway
public class CarritoController {

    private final CarritoService service;

    public CarritoController(CarritoService service) {
        this.service = service;
    }

    @PostMapping("/agregar")
    public ResponseEntity<ItemCarrito> agregar(@RequestBody ItemCarrito item, @RequestParam int cantidad) {
        return ResponseEntity.ok(service.agregarProducto(item, cantidad));
    }

    @GetMapping
    public ResponseEntity<List<ItemCarrito>> listar() {
        return ResponseEntity.ok(service.listarCarrito());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemCarrito> actualizar(@PathVariable Long id, @RequestParam int cantidad) {
        return ResponseEntity.ok(service.actualizarCantidad(id, cantidad, 0));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}