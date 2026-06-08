package com.ecomarket.pedidos.model;


import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "carritos_compra")
@Getter
@Setter
@NoArgsConstructor
public class CarritoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCarrito;

    @Column(nullable = false)
    private Long idCliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCarrito estado = EstadoCarrito.ACTIVO;

    @Column(nullable = false)
    private Double subtotal = 0.0;

    @Column(nullable = false)
    private Double descuentoAplicado = 0.0;

    @Column(nullable = false)
    private Double total = 0.0;

    private String codigoCuponAplicado;

    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ItemCarrito> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        recalcularTotales();
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
        recalcularTotales();
    }

    public void agregarItem(ItemCarrito item) {
        item.setCarrito(this);
        item.recalcularSubtotal();
        this.items.add(item);
        recalcularTotales();
    }

    public void eliminarItem(ItemCarrito item) {
        this.items.remove(item);
        item.setCarrito(null);
        recalcularTotales();
    }

    public void recalcularTotales() {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.subtotal = this.items.stream()
                .mapToDouble(item -> item.getSubtotal() != null ? item.getSubtotal() : 0.0)
                .sum();
        if (this.descuentoAplicado == null) {
            this.descuentoAplicado = 0.0;
        }
        this.total = Math.max(0.0, this.subtotal - this.descuentoAplicado);
    }
}