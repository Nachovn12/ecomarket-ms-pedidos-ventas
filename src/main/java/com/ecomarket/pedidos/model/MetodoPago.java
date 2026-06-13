package com.ecomarket.pedidos.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Metodos de pago aceptados", allowableValues = {"TARJETA", "TRANSFERENCIA", "EFECTIVO"})
public enum MetodoPago {
    TARJETA,
    TRANSFERENCIA,
    EFECTIVO
}
