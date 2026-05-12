package com.ecomarket.pedidos.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "cupones")
@Data
public class Cupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codigo;
    private Double descuentoPorcentaje;
    private Double montoMinimo;
    private LocalDate fechaVencimiento;
    private boolean activo;
}