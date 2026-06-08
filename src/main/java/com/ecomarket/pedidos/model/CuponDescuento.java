package com.ecomarket.pedidos.model;


import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Getter;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Entity
@Table(name = "cupones_descuento")
@Getter
@Setter
@NoArgsConstructor
public class CuponDescuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCupon;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String codigo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDescuento tipoDescuento;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false)
    private Double valorDescuento;

    @DecimalMin("0.0")
    private Double montoMinimo;

    @NotNull
    private LocalDate fechaVencimiento;

    @Column(nullable = false)
    private Boolean activo = true;
}