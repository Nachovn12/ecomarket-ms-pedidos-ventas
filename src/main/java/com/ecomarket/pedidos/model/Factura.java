package com.ecomarket.pedidos.model;


import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "facturas")
@Getter
@Setter
@NoArgsConstructor
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFactura;

    private Long idVenta;
    private Long idCliente;
    private String rutCliente;
    private String razonSocial;
    private Integer folio;
    private Double subtotal = 0.0;
    private Double iva = 0.0;
    private Double total = 0.0;
    private String estado = "EMITIDA";
    private LocalDateTime fechaEmision;

    @PrePersist
    public void prePersist() {
        this.fechaEmision = LocalDateTime.now();
        if (this.subtotal != null) {
            this.iva = this.subtotal * 0.19;
            this.total = this.subtotal + this.iva;
        }
    }
}