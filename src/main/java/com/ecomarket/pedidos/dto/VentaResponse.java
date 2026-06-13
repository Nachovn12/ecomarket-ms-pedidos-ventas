package com.ecomarket.pedidos.dto;

import com.ecomarket.pedidos.model.MetodoPago;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Datos de salida de una venta")
public class VentaResponse {

    @Schema(description = "ID de la venta", example = "1")
    private Long idVenta;

    @Schema(description = "ID del cliente", example = "10")
    private Long idCliente;

    @Schema(description = "ID del pedido online origen", example = "1")
    private Long idPedido;

    @Schema(description = "Metodo de pago", example = "TARJETA",
            allowableValues = {"TARJETA", "TRANSFERENCIA", "EFECTIVO"})
    private MetodoPago metodoPago;

    @Schema(description = "Subtotal de la venta", example = "19900.0")
    private Double subtotal;

    @Schema(description = "Descuento aplicado", example = "1990.0")
    private Double descuento;

    @Schema(description = "Total de la venta", example = "17910.0")
    private Double total;

    @Schema(description = "IVA (19%)", example = "3402.9")
    private Double iva;

    @Schema(description = "Observaciones", example = "Cliente retira en tienda")
    private String observaciones;

    @Schema(description = "Fecha de la venta", example = "2026-06-01T10:00:00")
    private LocalDateTime fechaVenta;

    @Schema(description = "Items vendidos")
    private List<ItemVentaResponse> items = new ArrayList<>();
}