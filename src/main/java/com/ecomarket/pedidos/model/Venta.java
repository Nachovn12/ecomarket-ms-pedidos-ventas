package com.ecomarket.pedidos.model;


import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ventas")
@Getter
@Setter
@NoArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idVenta;

    private Long idCliente;
    private Long idPedido;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    private Double subtotal = 0.0;
    private Double descuento = 0.0;
    private Double total = 0.0;
    private String observaciones;
    private LocalDateTime fechaVenta;

    @PrePersist
    public void prePersist() {
        this.fechaVenta = LocalDateTime.now();
    }
}