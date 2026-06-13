package com.ecomarket.pedidos.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "items_carrito")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Entidad JPA que representa un item dentro del carrito de compras")
public class ItemCarrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID del item", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idItem;

    @Column(nullable = false)
    @Schema(description = "ID del producto", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idProducto;

    @Column(nullable = false)
    @Schema(description = "Nombre del producto al momento de agregar al carrito", example = "Bolsa biodegradable mediana", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombreProducto;

    @Column(nullable = false)
    @Schema(description = "Cantidad solicitada", example = "2", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer cantidad;

    @Column(nullable = false)
    @Schema(description = "Precio unitario en CLP", example = "1990.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double precioUnitario;

    @Column(nullable = false)
    @Schema(description = "Subtotal del item (cantidad x precioUnitario)", example = "3980.0")
    private Double subtotal = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_carrito")
    @JsonBackReference
    @Schema(description = "Carrito al que pertenece este item")
    private CarritoCompra carrito;

    @PrePersist
    public void prePersist() {
        recalcularSubtotal();
    }

    @PreUpdate
    public void preUpdate() {
        recalcularSubtotal();
    }

    public void recalcularSubtotal() {
        if (this.cantidad != null && this.precioUnitario != null) {
            this.subtotal = this.cantidad * this.precioUnitario;
        } else {
            this.subtotal = 0.0;
        }
    }
}
