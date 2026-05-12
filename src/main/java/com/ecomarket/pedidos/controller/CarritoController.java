package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.entity.ItemCarrito;
import com.ecomarket.pedidos.service.CarritoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {

    @Autowired
    private CarritoService service;

    // CA1 y CA2: Agregar producto (stock simulado en 10)
    @PostMapping("/agregar")
    public String agregar(@RequestBody ItemCarrito item) {
        return service.agregarProducto(item, 10);
    }

    // Listar carrito completo
    @GetMapping
    public List<ItemCarrito> obtenerCarrito() {
        return service.listarCarrito();
    }

    // CA3: Actualizar cantidad de un producto por ID
    @PutMapping("/actualizar/{id}")
    public String actualizar(@PathVariable Long id, @RequestParam int nuevaCantidad) {
        return service.actualizarCantidad(id, nuevaCantidad, 10);
    }

    // CA4: Eliminar producto del carrito por ID
    @DeleteMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id) {
        service.eliminarProducto(id);
        return "Producto eliminado del carrito";
    }
}