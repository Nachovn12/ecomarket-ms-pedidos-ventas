package com.ecomarket.pedidos.dto;

import com.ecomarket.pedidos.entity.MetodoPago;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrearVentaRequest {

    @NotNull(message = "El idCliente es obligatorio")
    private Long idCliente;

    private Long idPedido;

    @NotNull(message = "El metodoPago es obligatorio")
    private MetodoPago metodoPago;

    @NotNull(message = "El total es obligatorio")
    private Double total;

    private Double subtotal;
    private Double descuento;
    private String observaciones;
}