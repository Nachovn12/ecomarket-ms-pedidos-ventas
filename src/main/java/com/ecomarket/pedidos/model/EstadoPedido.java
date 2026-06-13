package com.ecomarket.pedidos.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estados posibles de un pedido en su ciclo de vida", allowableValues = {"PENDIENTE", "CONFIRMADO", "EN_PREPARACION", "ENVIADO", "ENTREGADO", "CANCELADO"})
public enum EstadoPedido {
    PENDIENTE,
    CONFIRMADO,
    EN_PREPARACION,
    ENVIADO,
    ENTREGADO,
    CANCELADO
}
