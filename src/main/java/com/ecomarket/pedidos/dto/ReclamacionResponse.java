package com.ecomarket.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Datos de salida de una reclamacion")
public class ReclamacionResponse {

    @Schema(description = "ID de la reclamacion", example = "1")
    private Long idReclamacion;

    @Schema(description = "ID del cliente", example = "10")
    private Long idCliente;

    @Schema(description = "ID del pedido asociado", example = "1")
    private Long idPedido;

    @Schema(description = "ID de la venta asociada", example = "5")
    private Long idVenta;

    @Schema(description = "Motivo de la reclamacion", example = "Demora en la entrega")
    private String motivo;

    @Schema(description = "Descripcion detallada", example = "Llevo 10 dias esperando el producto")
    private String descripcion;

    @Schema(description = "Estado de la reclamacion", example = "ABIERTA",
            allowableValues = {"ABIERTA", "EN_REVISION", "RESUELTA", "CERRADA"})
    private String estado;

    @Schema(description = "Fecha de creacion", example = "2026-06-05T09:00:00")
    private LocalDateTime fechaCreacion;
}