package com.ecomarket.pedidos.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ventas")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Entidad JPA que representa una venta (presencial u online)")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID de la venta", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idVenta;

    @Schema(description = "ID del cliente", example = "10")
    private Long idCliente;

    @Schema(description = "ID del pedido asociado (si la venta nacio de un pedido online)", example = "1")
    private Long idPedido;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Metodo de pago", example = "EFECTIVO", allowableValues = {"TARJETA", "TRANSFERENCIA", "EFECTIVO"})
    private MetodoPago metodoPago;

    @Schema(description = "Subtotal de la venta", example = "19900.0")
    private Double subtotal = 0.0;

    @Schema(description = "Descuento aplicado", example = "1990.0")
    private Double descuento = 0.0;

    @Schema(description = "Total de la venta", example = "17910.0")
    private Double total = 0.0;

    @Schema(description = "IVA (19% del subtotal menos descuento)", example = "3402.9")
    private Double iva = 0.0;

    @Schema(description = "Observaciones de la venta", example = "Cliente retira en tienda")
    private String observaciones;

    @Schema(description = "Fecha de la venta", example = "2026-06-01T10:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaVenta;

    @PrePersist
    public void prePersist() {
        this.fechaVenta = LocalDateTime.now();
        if (this.subtotal != null && this.descuento != null) {
            double baseIva = Math.max(0.0, this.subtotal - this.descuento);
            this.iva = baseIva * 0.19;
        }
    }
}
