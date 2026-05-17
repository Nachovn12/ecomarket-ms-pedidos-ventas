package com.ecomarket.pedidos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemVentaRequest {

    @NotNull(message = "El idProducto es obligatorio")
    private Long idProducto;

    @NotBlank(message = "El nombreProducto es obligatorio")
    private String nombreProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor o igual a 1")
    private Integer cantidad;

    @NotNull(message = "El precioUnitario es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precioUnitario debe ser mayor a 0")
    private Double precioUnitario;
}
