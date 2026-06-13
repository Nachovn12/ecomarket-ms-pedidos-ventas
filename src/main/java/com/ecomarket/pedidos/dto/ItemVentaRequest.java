package com.ecomarket.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Item individual dentro de una venta")
public class ItemVentaRequest {

    @NotNull(message = "El idProducto es obligatorio")
    @Schema(description = "ID del producto", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idProducto;

    @NotBlank(message = "El nombreProducto es obligatorio")
    @Schema(description = "Nombre del producto", example = "Bolsa biodegradable mediana", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombreProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor o igual a 1")
    @Schema(description = "Cantidad vendida", example = "2", minimum = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer cantidad;

    @NotNull(message = "El precioUnitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precioUnitario debe ser mayor a 0")
    @Schema(description = "Precio unitario del producto en CLP", example = "1990.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double precioUnitario;
}
