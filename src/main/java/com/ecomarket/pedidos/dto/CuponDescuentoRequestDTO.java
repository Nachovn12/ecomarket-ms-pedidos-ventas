package com.ecomarket.pedidos.dto;

import com.ecomarket.pedidos.model.TipoDescuento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "DTO para crear un nuevo cupón de descuento con validaciones de entrada")
public class CuponDescuentoRequestDTO {

    @NotBlank(message = "El código del cupón es obligatorio")
    @Size(max = 50, message = "El código del cupón no puede exceder los 50 caracteres")
    @Schema(description = "Código único del cupón", example = "ECO10", maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    private String codigo;

    @NotNull(message = "El tipo de descuento es obligatorio")
    @Schema(description = "Tipo de descuento", example = "PORCENTAJE", allowableValues = {"PORCENTAJE", "MONTO_FIJO"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private TipoDescuento tipoDescuento;

    @NotNull(message = "El valor del descuento es obligatorio")
    @Positive(message = "El valor del descuento debe ser positivo")
    @Schema(description = "Valor del descuento", example = "10.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double valorDescuento;

    @PositiveOrZero(message = "El monto mínimo no puede ser negativo")
    @Schema(description = "Monto mínimo de compra para que el cupón aplique", example = "5000.0")
    private Double montoMinimo;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    @FutureOrPresent(message = "La fecha de vencimiento no puede ser en el pasado")
    @Schema(description = "Fecha de vencimiento del cupón", example = "2026-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate fechaVencimiento;

    @Schema(description = "Indica si el cupón está activo", example = "true")
    private Boolean activo = true;
}
