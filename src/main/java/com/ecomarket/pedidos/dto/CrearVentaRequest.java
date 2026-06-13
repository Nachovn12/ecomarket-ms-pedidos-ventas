package com.ecomarket.pedidos.dto;

import com.ecomarket.pedidos.model.MetodoPago;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "Datos para registrar una venta presencial")
public class CrearVentaRequest {

    @NotNull(message = "El idCliente es obligatorio")
    @Schema(description = "ID del cliente", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idCliente;

    @Schema(description = "ID del pedido asociado, si la venta se origino desde uno online", example = "1")
    private Long idPedido;

    @NotNull(message = "El metodoPago es obligatorio")
    @Schema(description = "Metodo de pago", example = "EFECTIVO", allowableValues = {"EFECTIVO", "TARJETA_CREDITO", "TARJETA_DEBITO", "TRANSFERENCIA", "WEBPAY"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private MetodoPago metodoPago;

    @NotNull(message = "Los items de venta son obligatorios")
    @NotEmpty(message = "Debe incluir al menos un item")
    @Valid
    @Schema(description = "Listado de items vendidos", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ItemVentaRequest> items;

    @Schema(description = "Descuento global aplicado a la venta (opcional)", example = "500.0")
    private Double descuento;

    @Schema(description = "Observaciones o notas adicionales", example = "Cliente retira en tienda")
    private String observaciones;
}
