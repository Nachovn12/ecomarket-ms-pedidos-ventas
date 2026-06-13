package com.ecomarket.pedidos.dto;

import com.ecomarket.pedidos.model.EstadoCarrito;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "Datos de salida de un carrito de compras")
public class CarritoResponse {

    @Schema(description = "ID del carrito", example = "1")
    private Long idCarrito;

    @Schema(description = "ID del cliente dueno del carrito", example = "10")
    private Long idCliente;

    @Schema(description = "Estado del carrito", example = "ACTIVO",
            allowableValues = {"ACTIVO", "CONVERTIDO", "VACIO", "CANCELADO"})
    private EstadoCarrito estado;

    @Schema(description = "Subtotal del carrito sin descuentos", example = "19900.0")
    private Double subtotal;

    @Schema(description = "Monto descontado por cupones", example = "1990.0")
    private Double descuentoAplicado;

    @Schema(description = "Total final del carrito", example = "17910.0")
    private Double total;

    @Schema(description = "Codigo del cupon aplicado", example = "ECO10")
    private String codigoCuponAplicado;

    @Schema(description = "Fecha de creacion", example = "2026-06-01T10:00:00")
    private LocalDateTime fechaCreacion;

    @Schema(description = "Fecha de ultima actualizacion", example = "2026-06-01T12:00:00")
    private LocalDateTime fechaActualizacion;

    @Schema(description = "Items contenidos en el carrito")
    private List<ItemCarritoResponse> items = new ArrayList<>();
}