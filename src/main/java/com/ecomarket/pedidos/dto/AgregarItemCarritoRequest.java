package com.ecomarket.pedidos.dto;

import lombok.Data;

@Data
public class AgregarItemCarritoRequest {
    private Long idProducto;
    private String nombreProducto;
    private int cantidad;
    private Double precioUnitario;
    private int stockDisponible;
}