package com.ecomarket.pedidos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AplicarCuponRequest {

    @NotBlank(message = "El codigo del cupon es obligatorio")
    private String codigo;
}