package com.ecomarket.pedidos.dto;

import com.ecomarket.pedidos.model.MetodoPago;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Datos para crear un pedido a partir de un carrito")
public class CrearPedidoRequest {

    @NotNull(message = "El idCliente es obligatorio")
    @Schema(description = "ID del cliente", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idCliente;

    @Schema(description = "Metodo de pago del pedido", example = "TARJETA_CREDITO", allowableValues = {"EFECTIVO", "TARJETA_CREDITO", "TARJETA_DEBITO", "TRANSFERENCIA", "WEBPAY"})
    private MetodoPago metodoPago;

    @Schema(description = "Direccion de entrega del pedido", example = "Av. Siempre Viva 742, Santiago")
    private String direccionEntrega;

    @Schema(description = "Observaciones o notas adicionales", example = "Entregar entre 18:00 y 21:00")
    private String observaciones;
}
