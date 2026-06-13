package com.ecomarket.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resultado de aplicar un cupon al carrito")
public class AplicarCuponResponse {

    @Schema(description = "Codigo del cupon aplicado", example = "ECO10")
    private String codigo;

    @Schema(description = "Subtotal del carrito antes del descuento", example = "19900.0")
    private Double subtotal;

    @Schema(description = "Monto descontado", example = "1990.0")
    private Double descuento;

    @Schema(description = "Total final a pagar", example = "17910.0")
    private Double totalFinal;

    @Schema(description = "Mensaje informativo", example = "Cupon aplicado correctamente")
    private String mensaje;
}
