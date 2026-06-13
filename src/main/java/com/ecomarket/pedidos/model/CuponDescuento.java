package com.ecomarket.pedidos.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidad JPA de cupon de descuento.
 * Las validaciones de entrada se gestionan en la capa de service (CuponDescuentoService).
 */
@Entity
@Table(name = "cupones_descuento")
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Entidad JPA que representa un cupon de descuento aplicable a carritos o ventas")
public class CuponDescuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID del cupon", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idCupon;

    @Column(nullable = false, unique = true, length = 50)
    @Schema(description = "Codigo unico del cupon", example = "ECO10", maxLength = 50, requiredMode = Schema.RequiredMode.REQUIRED)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Schema(description = "Tipo de descuento", example = "PORCENTAJE", allowableValues = {"PORCENTAJE", "MONTO_FIJO"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private TipoDescuento tipoDescuento;

    @Column(nullable = false)
    @Schema(description = "Valor del descuento. Si es PORCENTAJE, se interpreta como 0-100. Si es MONTO_FIJO, como CLP", example = "10.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double valorDescuento;

    @Schema(description = "Monto minimo de compra para que el cupon aplique", example = "5000.0")
    private Double montoMinimo;

    @Column(nullable = false)
    @Schema(description = "Fecha de vencimiento del cupon", example = "2026-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate fechaVencimiento;

    @Column(nullable = false)
    @Schema(description = "Indica si el cupon esta activo", example = "true")
    private Boolean activo = true;
}
