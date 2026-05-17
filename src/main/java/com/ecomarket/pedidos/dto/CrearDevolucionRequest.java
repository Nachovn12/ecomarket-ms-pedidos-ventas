package com.ecomarket.pedidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrearDevolucionRequest {

    @NotNull(message = "El idCliente es obligatorio")
    private Long idCliente;

    private Long idPedido;
    private Long idVenta;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;
}