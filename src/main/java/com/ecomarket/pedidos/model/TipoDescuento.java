package com.ecomarket.pedidos.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipos de descuento que puede ofrecer un cupon", allowableValues = {"PORCENTAJE", "MONTO_FIJO"})
public enum TipoDescuento {
    PORCENTAJE,
    MONTO_FIJO
}
