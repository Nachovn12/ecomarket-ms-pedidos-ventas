package com.ecomarket.pedidos.model;


import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_pedidos")
@Getter
@Setter
@NoArgsConstructor
public class HistorialPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idHistorial;

    private Long idPedido;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estadoAnterior;

    @Enumerated(EnumType.STRING)
    private EstadoPedido estadoNuevo;

    private String descripcion;
    private LocalDateTime fechaCambio;

    @PrePersist
    public void prePersist() {
        this.fechaCambio = LocalDateTime.now();
    }
}
