package com.ecomarket.pedidos.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "detalles_pedido")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Entidad JPA que representa una linea de detalle de un pedido")
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID del detalle", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idDetalle;

    @Column(nullable = false)
    @Schema(description = "ID del producto", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idProducto;

    @Column(nullable = false)
    @Schema(description = "Nombre del producto al momento de la compra", example = "Bolsa biodegradable mediana", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombreProducto;

    @Column(nullable = false)
    @Schema(description = "Cantidad comprada", example = "2", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer cantidad;

    @Column(nullable = false)
    @Schema(description = "Precio unitario en CLP", example = "1990.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double precioUnitario;

    @Column(nullable = false)
    @Schema(description = "Subtotal (cantidad x precioUnitario)", example = "3980.0")
    private Double subtotal = 0.0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido")
    @Schema(description = "Pedido al que pertenece este detalle")
    private Pedido pedido;

    public void calcularSubtotal() {
        if (this.cantidad != null && this.precioUnitario != null) {
            this.subtotal = this.cantidad * this.precioUnitario;
        }
    }
}
