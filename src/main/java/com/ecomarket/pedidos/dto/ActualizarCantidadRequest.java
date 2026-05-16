package com.ecomarket.pedidos.dto;

import lombok.Data;

@Data
public class ActualizarCantidadRequest {
    private int cantidad;
    private int stockDisponible;
}