package com.ecomarket.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Datos de salida de un item dentro del carrito")
public class ItemCarritoResponse {

    @Schema(description = "ID del item", example = "1")
    private Long idItem;

    @Schema(description = "ID del producto", example = "5")
    private Long idProducto;

    @Schema(description = "Nombre del producto al momento de agregar", example = "Bolsa biodegradable mediana")
    private String nombreProducto;

    @Schema(description = "Cantidad solicitada", example = "2")
    private Integer cantidad;

    @Schema(description = "Precio unitario en CLP", example = "1990.0")
    private Double precioUnitario;

    @Schema(description = "Subtotal del item (precio * cantidad)", example = "3980.0")
    private Double subtotal;
}