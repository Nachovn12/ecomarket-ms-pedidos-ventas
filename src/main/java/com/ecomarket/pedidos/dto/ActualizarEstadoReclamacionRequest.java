package com.ecomarket.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Solicitud para cambiar el estado de una reclamacion")
public class ActualizarEstadoReclamacionRequest {

    @NotBlank(message = "El estado es obligatorio")
    @Schema(description = "Nuevo estado de la reclamacion", example = "EN_REVISION", allowableValues = {"ABIERTA", "EN_REVISION", "RESUELTA", "CERRADA"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String estado;
}
