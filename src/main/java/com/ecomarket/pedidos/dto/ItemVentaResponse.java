package com.ecomarket.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Item individual vendido dentro de una venta")
public class ItemVentaResponse {

    @Schema(description = "ID del item", example = "1")
    private Long idItem;

    @Schema(description = "ID del producto", example = "5")
    private Long idProducto;

    @Schema(description = "Nombre del producto", example = "Bolsa biodegradable mediana")
    private String nombreProducto;

    @Schema(description = "Cantidad vendida", example = "2")
    private Integer cantidad;

    @Schema(description = "Precio unitario", example = "1990.0")
    private Double precioUnitario;

    @Schema(description = "Subtotal del item", example = "3980.0")
    private Double subtotal;
}