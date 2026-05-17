package com.ecomarket.pedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AplicarCuponResponse {

    private String codigo;
    private Double subtotal;
    private Double descuento;
    private Double totalFinal;
    private String mensaje;
}