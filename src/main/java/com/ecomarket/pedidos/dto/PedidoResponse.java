package com.ecomarket.pedidos.dto;

import com.ecomarket.pedidos.entity.EstadoPedido;
import com.ecomarket.pedidos.entity.MetodoPago;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PedidoResponse {

    private Long idPedido;
    private Long idCliente;
    private EstadoPedido estado;
    private MetodoPago metodoPago;
    private Double subtotal;
    private Double descuento;
    private Double total;
    private String direccionEntrega;
    private String observaciones;
    private LocalDateTime fechaCreacion;
}