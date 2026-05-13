package com.ecomarket.pedidos.dto;

public class AplicarCuponResponse {
    private String codigo;
    private Double subtotal;
    private Double descuento;
    private Double totalFinal;
    private String mensaje;

    public AplicarCuponResponse() {}

    public AplicarCuponResponse(String codigo, Double subtotal, Double descuento, Double totalFinal, String mensaje) {
        this.codigo = codigo;
        this.subtotal = subtotal;
        this.descuento = descuento;
        this.totalFinal = totalFinal;
        this.mensaje = mensaje;
    }

    // Getters
    public String getCodigo() { return codigo; }
    public Double getSubtotal() { return subtotal; }
    public Double getDescuento() { return descuento; }
    public Double getTotalFinal() { return totalFinal; }
    public String getMensaje() { return mensaje; }
}