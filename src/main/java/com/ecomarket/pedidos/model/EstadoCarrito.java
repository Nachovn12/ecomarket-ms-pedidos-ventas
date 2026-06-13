package com.ecomarket.pedidos.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estados posibles de un carrito de compras", allowableValues = {"ACTIVO", "CONVERTIDO", "VACIO", "CANCELADO"})
public enum EstadoCarrito {
    ACTIVO,
    CONVERTIDO,
    VACIO,
    CANCELADO
}
