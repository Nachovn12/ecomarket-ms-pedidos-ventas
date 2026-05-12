package com.ecomarket.pedidos.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "items_carrito")
@Data
public class ItemCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productoId;
    private String nombre;
    private int cantidad;
    private Double precioUnitario;

    public Double getSubtotal() {
        return this.precioUnitario * this.cantidad;
    }
}