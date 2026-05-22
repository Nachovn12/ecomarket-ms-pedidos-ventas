package com.ecomarket.pedidos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActualizarEstadoReclamacionRequest {
    @NotBlank(message = "El estado es obligatorio")
    private String estado;
}