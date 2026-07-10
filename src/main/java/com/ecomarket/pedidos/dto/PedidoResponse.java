package com.ecomarket.pedidos.dto;

import com.ecomarket.pedidos.model.EstadoPedido;
import com.ecomarket.pedidos.model.MetodoPago;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Datos de salida de un pedido")
public class PedidoResponse {

    @Schema(description = "ID del pedido", example = "1")
    private Long idPedido;

    @Schema(description = "ID del cliente", example = "10")
    private Long idCliente;

    @Schema(description = "Nombre completo del cliente", example = "Ignacio Valeria")
    private String nombreCliente;

    @Schema(description = "Correo electronico del cliente", example = "ignacio.valeria@ecomarket.cl")
    private String correoCliente;

    @Schema(description = "Telefono del cliente", example = "+56 9 8877 6655")
    private String telefonoCliente;

    @Schema(description = "Estado actual del pedido", example = "PENDIENTE", allowableValues = {"PENDIENTE", "CONFIRMADO", "EN_PREPARACION", "ENVIADO", "ENTREGADO", "CANCELADO"})
    private EstadoPedido estado;

    @Schema(description = "Metodo de pago del pedido", example = "TARJETA_CREDITO")
    private MetodoPago metodoPago;

    @Schema(description = "Subtotal del pedido", example = "19900.0")
    private Double subtotal;

    @Schema(description = "Monto descontado", example = "1990.0")
    private Double descuento;

    @Schema(description = "Total del pedido", example = "17910.0")
    private Double total;

    @Schema(description = "IVA (19% sobre subtotal menos descuento)", example = "756.2")
    private Double iva;

    @Schema(description = "Direccion de entrega", example = "Av. Siempre Viva 742, Santiago")
    private String direccionEntrega;

    @Schema(description = "Observaciones del pedido", example = "Entregar entre 18:00 y 21:00")
    private String observaciones;

    @Schema(description = "Fecha de creacion del pedido", example = "2026-06-01T10:00:00")
    private LocalDateTime fechaCreacion;
}
