package com.ecomarket.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Datos para aplicar un cupon al carrito")
public class AplicarCuponRequest {

    @NotBlank(message = "El codigo del cupon es obligatorio")
    @Schema(description = "Codigo unico del cupon", example = "ECO10", requiredMode = Schema.RequiredMode.REQUIRED)
    private String codigo;
}
