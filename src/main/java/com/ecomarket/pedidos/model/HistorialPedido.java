package com.ecomarket.pedidos.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_pedidos")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Entidad JPA que registra cambios de estado en un pedido")
public class HistorialPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID del registro de historial", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idHistorial;

    @Schema(description = "ID del pedido afectado", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idPedido;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Estado anterior del pedido", example = "PENDIENTE", allowableValues = {"PENDIENTE", "CONFIRMADO", "EN_PREPARACION", "ENVIADO", "ENTREGADO", "CANCELADO"})
    private EstadoPedido estadoAnterior;

    @Enumerated(EnumType.STRING)
    @Schema(description = "Nuevo estado del pedido", example = "CONFIRMADO", allowableValues = {"PENDIENTE", "CONFIRMADO", "EN_PREPARACION", "ENVIADO", "ENTREGADO", "CANCELADO"})
    private EstadoPedido estadoNuevo;

    @Schema(description = "Descripcion del cambio", example = "Pedido confirmado por el sistema de pagos")
    private String descripcion;

    @Schema(description = "Fecha del cambio de estado", example = "2026-06-02T11:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaCambio;

    @PrePersist
    public void prePersist() {
        this.fechaCambio = LocalDateTime.now();
    }
}
