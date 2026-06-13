package com.ecomarket.pedidos.model;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reclamaciones")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Entidad JPA que representa una reclamacion de un cliente")
public class Reclamacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID de la reclamacion", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idReclamacion;

    @Schema(description = "ID del cliente", example = "10")
    private Long idCliente;

    @Schema(description = "ID del pedido asociado", example = "1")
    private Long idPedido;

    @Schema(description = "ID de la venta asociada", example = "5")
    private Long idVenta;

    @Schema(description = "Motivo breve de la reclamacion", example = "Demora en la entrega")
    private String motivo;

    @Schema(description = "Descripcion detallada del problema", example = "Han pasado 10 dias y aun no recibo el pedido")
    private String descripcion;

    @Schema(description = "Estado de la reclamacion", example = "ABIERTA", allowableValues = {"ABIERTA", "EN_REVISION", "RESUELTA", "CERRADA"})
    private String estado = "ABIERTA";

    @Schema(description = "Fecha de creacion de la reclamacion", example = "2026-06-05T09:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }
}
