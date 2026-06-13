package com.ecomarket.pedidos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Datos del cliente para emitir factura electronica")
public class CrearFacturaRequest {

    @NotNull(message = "El idCliente es obligatorio")
    @Schema(description = "ID del cliente", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long idCliente;

    @Schema(description = "RUT del cliente", example = "12.345.678-9")
    private String rutCliente;

    @Schema(description = "Razon social para factura empresarial", example = "EcoMarket SpA")
    private String razonSocial;
}
