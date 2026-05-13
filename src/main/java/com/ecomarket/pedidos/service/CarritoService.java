package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.entity.ItemCarrito;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class CarritoService {

    // Lista temporal para que el microservicio funcione mientras se integra la BD final
    private List<ItemCarrito> items = new ArrayList<>();

    public ItemCarrito agregarProducto(ItemCarrito item, int cantidad) {
        item.setCantidad(cantidad);
        items.add(item);
        return item;
    }

    public List<ItemCarrito> listarCarrito() {
        return items;
    }

    public ItemCarrito actualizarCantidad(Long id, int nuevaCantidad, int algo) {
        // Lógica simplificada para compilación
        return items.stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .map(i -> {
                    i.setCantidad(nuevaCantidad);
                    return i;
                }).orElse(null);
    }

    public void eliminarProducto(Long id) {
        items.removeIf(i -> i.getId().equals(id));
    }
}