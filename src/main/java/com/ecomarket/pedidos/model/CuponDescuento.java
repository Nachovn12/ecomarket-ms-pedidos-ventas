package com.ecomarket.pedidos.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * Entidad JPA de Cupón de Descuento.
 * Las validaciones de entrada se gestionan en la capa de Service (CuponDescuentoService).
 */
@Entity
@Table(name = "cupones_descuento")
@Getter
@Setter
@NoArgsConstructor
public class CuponDescuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCupon;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDescuento tipoDescuento;

    @Column(nullable = false)
    private Double valorDescuento;

    private Double montoMinimo;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @Column(nullable = false)
    private Boolean activo = true;
}
