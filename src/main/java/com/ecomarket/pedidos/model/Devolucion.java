package com.ecomarket.pedidos.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "devoluciones")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Entidad JPA que representa una devolucion de productos")
public class Devolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID de la devolucion", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idDevolucion;

    @Schema(description = "ID del cliente que solicita la devolucion", example = "10")
    private Long idCliente;

    @Schema(description = "ID del pedido asociado", example = "1")
    private Long idPedido;

    @Schema(description = "ID de la venta asociada", example = "5")
    private Long idVenta;

    @Schema(description = "Motivo de la devolucion", example = "Producto defectuoso")
    private String motivo;

    @Schema(description = "Estado de la devolucion", example = "PENDIENTE", allowableValues = {"PENDIENTE", "APROBADA", "RECHAZADA", "PROCESADA"})
    private String estado = "PENDIENTE";

    @Schema(description = "Fecha de creacion de la devolucion", example = "2026-06-05T09:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
