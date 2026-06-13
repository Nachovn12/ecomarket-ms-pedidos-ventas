package com.ecomarket.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Solicitud para cambiar el estado de una devolucion")
public class ActualizarEstadoDevolucionRequest {

    @NotBlank(message = "El estado es obligatorio")
    @Schema(description = "Nuevo estado de la devolucion", example = "APROBADA", allowableValues = {"PENDIENTE", "APROBADA", "RECHAZADA", "PROCESADA"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String estado;
}
