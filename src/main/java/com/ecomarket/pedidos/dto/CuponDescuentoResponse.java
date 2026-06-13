package com.ecomarket.pedidos.dto;

import com.ecomarket.pedidos.model.TipoDescuento;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;

@Data
@Schema(description = "Datos de salida de un cupon de descuento")
public class CuponDescuentoResponse {

    @Schema(description = "ID del cupon", example = "1")
    private Long idCupon;

    @Schema(description = "Codigo unico del cupon", example = "ECO10")
    private String codigo;

    @Schema(description = "Tipo de descuento", example = "PORCENTAJE",
            allowableValues = {"PORCENTAJE", "MONTO_FIJO"})
    private TipoDescuento tipoDescuento;

    @Schema(description = "Valor del descuento. Si es PORCENTAJE se interpreta como 0-100. Si es MONTO_FIJO, como CLP", example = "10.0")
    private Double valorDescuento;

    @Schema(description = "Monto minimo de compra para que el cupon aplique", example = "5000.0")
    private Double montoMinimo;

    @Schema(description = "Fecha de vencimiento del cupon", example = "2026-12-31")
    private LocalDate fechaVencimiento;

    @Schema(description = "Indica si el cupon esta activo", example = "true")
    private Boolean activo;
}