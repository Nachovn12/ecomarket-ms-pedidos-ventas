package com.ecomarket.pedidos.dto;

import jakarta.validation.constraints.NotBlank;

public class AplicarCuponRequest {

    @NotBlank
    private String codigo;

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}