package com.ecomarket.pedidos.dto;

import com.ecomarket.pedidos.entity.MetodoPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CrearVentaRequest {

    @NotNull(message = "El idCliente es obligatorio")
    private Long idCliente;

    private Long idPedido;

    @NotNull(message = "El metodoPago es obligatorio")
    private MetodoPago metodoPago;

    @NotNull(message = "Los items de venta son obligatorios")
    @NotEmpty(message = "Debe incluir al menos un item")
    @Valid
    private List<ItemVentaRequest> items;

    private Double descuento;
    private String observaciones;
}
