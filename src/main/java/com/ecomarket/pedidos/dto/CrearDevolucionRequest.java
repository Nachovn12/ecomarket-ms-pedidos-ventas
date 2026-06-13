package com.ecomarket.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Datos para crear una devolucion")
public class CrearDevolucionRequest {

    @NotNull(message = "El idCliente es obligatorio")
    @Schema(description = "ID del cliente que solicita la devolucion", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idCliente;

    @Schema(description = "ID del pedido asociado, si la devolucion proviene de un pedido online", example = "1")
    private Long idPedido;

    @Schema(description = "ID de la venta presencial asociada", example = "5")
    private Long idVenta;

    @NotBlank(message = "El motivo es obligatorio")
    @Schema(description = "Motivo de la devolucion", example = "Producto defectuoso", requiredMode = Schema.RequiredMode.REQUIRED)
    private String motivo;
}
