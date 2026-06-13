package com.ecomarket.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Datos para crear un carrito de compras para un cliente")
public class CrearCarritoRequest {

    @NotNull(message = "El idCliente es obligatorio")
    @Schema(description = "Identificador del cliente", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idCliente;
}
