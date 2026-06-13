package com.ecomarket.pedidos.model;


import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Entidad JPA que representa una factura electronica")
public class Factura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID de la factura", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long idFactura;

    @Schema(description = "ID de la venta asociada", example = "1")
    private Long idVenta;

    @Schema(description = "ID del cliente", example = "10")
    private Long idCliente;

    @Schema(description = "RUT del cliente", example = "12.345.678-9")
    private String rutCliente;

    @Schema(description = "Razon social para facturacion empresarial", example = "EcoMarket SpA")
    private String razonSocial;

    @Schema(description = "Folio correlativo de la factura", example = "1234")
    private Integer folio;

    @Schema(description = "Subtotal antes de impuestos", example = "19900.0")
    private Double subtotal = 0.0;

    @Schema(description = "IVA (19% del subtotal)", example = "3781.0")
    private Double iva = 0.0;

    @Schema(description = "Total de la factura con impuestos", example = "23681.0")
    private Double total = 0.0;

    @Schema(description = "Estado de la factura", example = "EMITIDA", allowableValues = {"EMITIDA", "ANULADA"})
    private String estado = "EMITIDA";

    @Schema(description = "Fecha de emision de la factura", example = "2026-06-01T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
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
