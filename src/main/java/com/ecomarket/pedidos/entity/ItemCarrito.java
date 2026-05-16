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

    private Long idProducto;
    private String nombreProducto;
    private int cantidad;
    private Double precioUnitario;
    private Double subtotal;

    @ManyToOne
    @JoinColumn(name = "carrito_id")
    private CarritoCompra carrito;

    public void calcularSubtotal() {
        this.subtotal = this.precioUnitario * this.cantidad;
    }
}