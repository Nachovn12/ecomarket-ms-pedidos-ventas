package com.ecomarket.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Datos de salida de una factura electronica")
public class FacturaResponse {

    @Schema(description = "ID de la factura", example = "1")
    private Long idFactura;

    @Schema(description = "ID de la venta asociada", example = "1")
    private Long idVenta;

    @Schema(description = "ID del cliente", example = "10")
    private Long idCliente;

    @Schema(description = "RUT del cliente", example = "12.345.678-9")
    private String rutCliente;

    @Schema(description = "Razon social", example = "EcoMarket SpA")
    private String razonSocial;

    @Schema(description = "Folio correlativo", example = "1234")
    private Integer folio;

    @Schema(description = "Subtotal antes de impuestos", example = "19900.0")
    private Double subtotal;

    @Schema(description = "IVA (19%)", example = "3781.0")
    private Double iva;

    @Schema(description = "Total con impuestos", example = "23681.0")
    private Double total;

    @Schema(description = "Estado de la factura", example = "EMITIDA",
            allowableValues = {"EMITIDA", "ANULADA"})
    private String estado;

    @Schema(description = "Fecha de emision", example = "2026-06-01T10:30:00")
    private LocalDateTime fechaEmision;
}