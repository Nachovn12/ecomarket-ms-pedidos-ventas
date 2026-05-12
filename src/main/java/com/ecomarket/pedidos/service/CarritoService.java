package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.entity.ItemCarrito;
import com.ecomarket.pedidos.repository.CarritoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CarritoService {

    @Autowired
    private CarritoRepository repository;

    // CA1 y CA2: Agregar producto con validación de stock
    public String agregarProducto(ItemCarrito item, int stockDisponible) {
        if (item.getCantidad() > stockDisponible) {
            return "Error: No hay suficiente stock para el producto " + item.getNombre();
        }
        repository.save(item);
        return "Producto añadido correctamente";
    }

    // CA3: Listar todos los productos del carrito
    public List<ItemCarrito> listarCarrito() {
        return repository.findAll();
    }

    // CA3: Actualizar cantidad de un producto
    public String actualizarCantidad(Long id, int nuevaCantidad, int stockDisponible) {
        if (nuevaCantidad > stockDisponible) {
            return "Error: Cantidad supera el stock disponible";
        }
        ItemCarrito item = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado en el carrito"));
        item.setCantidad(nuevaCantidad);
        repository.save(item);
        return "Cantidad actualizada. Subtotal: $" + item.getSubtotal();
    }

    // CA4: Eliminar producto del carrito
    public void eliminarProducto(Long id) {
        repository.deleteById(id);
    }
}