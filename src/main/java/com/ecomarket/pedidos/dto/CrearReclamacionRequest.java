package com.ecomarket.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Datos para crear una reclamacion")
public class CrearReclamacionRequest {

    @NotNull(message = "El idCliente es obligatorio")
    @Schema(description = "ID del cliente", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idCliente;

    @Schema(description = "ID del pedido asociado", example = "1")
    private Long idPedido;

    @Schema(description = "ID de la venta asociada", example = "5")
    private Long idVenta;

    @NotBlank(message = "El motivo es obligatorio")
    @Schema(description = "Motivo de la reclamacion", example = "Demora en la entrega", requiredMode = Schema.RequiredMode.REQUIRED)
    private String motivo;

    @Schema(description = "Descripcion detallada del problema", example = "Han pasado 10 dias y aun no recibo el pedido")
    private String descripcion;
}
