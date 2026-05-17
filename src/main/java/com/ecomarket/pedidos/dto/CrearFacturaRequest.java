package com.ecomarket.pedidos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrearFacturaRequest {

    @NotNull(message = "El idCliente es obligatorio")
    private Long idCliente;

    private String rutCliente;
    private String razonSocial;
}