package com.ecomarket.pedidos.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Entidad JPA que representa un pedido de un cliente")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID del pedido", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idPedido;

    @Column(nullable = false)
    @Schema(description = "ID del cliente", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idCliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Estado del pedido", example = "PENDIENTE", allowableValues = {"PENDIENTE", "CONFIRMADO", "EN_PREPARACION", "ENVIADO", "ENTREGADO", "CANCELADO"})
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Metodo de pago del pedido", example = "TARJETA", allowableValues = {"TARJETA", "TRANSFERENCIA", "EFECTIVO"})
    private MetodoPago metodoPago;

    @Schema(description = "Subtotal del pedido", example = "19900.0")
    private Double subtotal = 0.0;

    @Schema(description = "Descuento aplicado", example = "1990.0")
    private Double descuento = 0.0;

    @Schema(description = "Total del pedido", example = "17910.0")
    private Double total = 0.0;

    @Schema(description = "IVA (19% del subtotal menos descuento)", example = "3402.9")
    private Double iva = 0.0;

    @Schema(description = "Direccion de entrega", example = "Av. Siempre Viva 742, Santiago")
    private String direccionEntrega;

    @Schema(description = "Observaciones del pedido", example = "Entregar entre 18:00 y 21:00")
    private String observaciones;

    @Schema(description = "Fecha de creacion del pedido", example = "2026-06-01T10:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha de ultima actualizacion", example = "2026-06-05T14:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Schema(description = "Lineas de detalle del pedido")
    private List<DetallePedido> detalles = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        if (this.subtotal != null && this.descuento != null) {
            double baseIva = Math.max(0.0, this.subtotal - this.descuento);
            this.iva = baseIva * 0.19;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.fechaActualizacion = LocalDateTime.now();
    }
}
