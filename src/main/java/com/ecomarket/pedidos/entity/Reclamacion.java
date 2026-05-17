package com.ecomarket.pedidos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reclamaciones")
@Getter
@Setter
public class Reclamacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idReclamacion;

    private Long idCliente;
    private Long idPedido;
    private Long idVenta;
    private String motivo;
    private String descripcion;
    private String estado = "ABIERTA";
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void prePersist() {
        this.fechaCreacion = LocalDateTime.now();
    }
}