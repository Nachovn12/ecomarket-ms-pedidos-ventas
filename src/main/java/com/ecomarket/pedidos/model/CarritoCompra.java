package com.ecomarket.pedidos.model;


import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Entidad JPA que representa el carrito de compras de un cliente")
public class CarritoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID del carrito", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idCarrito;

    @Column(nullable = false)
    @Schema(description = "ID del cliente dueno del carrito", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idCliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Estado del carrito", example = "ACTIVO", allowableValues = {"ACTIVO", "CONVERTIDO", "VACIO", "CANCELADO"})
    private EstadoCarrito estado = EstadoCarrito.ACTIVO;

    @Column(nullable = false)
    @Schema(description = "Subtotal del carrito sin descuentos", example = "19900.0")
    private Double subtotal = 0.0;

    @Column(nullable = false)
    @Schema(description = "Monto descontado por cupones", example = "1990.0")
    private Double descuentoAplicado = 0.0;

    @Column(nullable = false)
    @Schema(description = "Total final del carrito", example = "17910.0")
    private Double total = 0.0;

    @Schema(description = "Codigo del cupon actualmente aplicado", example = "ECO10")
    private String codigoCuponAplicado;

    @Schema(description = "Fecha de creacion del carrito", example = "2026-06-01T10:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha de ultima actualizacion", example = "2026-06-01T12:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Schema(description = "Items contenidos en el carrito")
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
