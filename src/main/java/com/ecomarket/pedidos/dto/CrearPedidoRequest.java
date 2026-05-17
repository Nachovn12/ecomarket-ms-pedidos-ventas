package com.ecomarket.pedidos.dto;

import com.ecomarket.pedidos.entity.MetodoPago;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrearPedidoRequest {

    @NotNull(message = "El idCliente es obligatorio")
    private Long idCliente;

    private MetodoPago metodoPago;
    private String direccionEntrega;
    private String observaciones;
}