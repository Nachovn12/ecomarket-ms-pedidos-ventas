package com.ecomarket.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Datos opcionales para cancelar un pedido")
public class CancelarPedidoRequest {

    @Schema(description = "Motivo de la cancelacion", example = "Cambio de opinion del cliente")
    private String motivo;
}
