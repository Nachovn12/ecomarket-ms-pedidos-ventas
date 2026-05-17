package com.ecomarket.pedidos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrearCarritoRequest {

    @NotNull(message = "El idCliente es obligatorio")
    private Long idCliente;
}