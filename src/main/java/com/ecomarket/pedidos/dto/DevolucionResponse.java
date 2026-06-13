package com.ecomarket.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Datos de salida de una devolucion")
public class DevolucionResponse {

    @Schema(description = "ID de la devolucion", example = "1")
    private Long idDevolucion;

    @Schema(description = "ID del cliente", example = "10")
    private Long idCliente;

    @Schema(description = "ID del pedido asociado", example = "1")
    private Long idPedido;

    @Schema(description = "ID de la venta asociada", example = "5")
    private Long idVenta;

    @Schema(description = "Motivo de la devolucion", example = "Producto defectuoso")
    private String motivo;

    @Schema(description = "Estado de la devolucion", example = "PENDIENTE",
            allowableValues = {"PENDIENTE", "APROBADA", "RECHAZADA", "PROCESADA"})
    private String estado;

    @Schema(description = "Fecha de creacion de la solicitud", example = "2026-06-05T09:00:00")
    private LocalDateTime fechaCreacion;
}