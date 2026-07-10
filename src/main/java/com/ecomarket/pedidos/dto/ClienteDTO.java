package com.ecomarket.pedidos.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClienteDTO {
    private Long id;
    private String nombre;
    private String correo;
    private String telefono;
    private String direccionEnvio;
    private String medioPago;
    private String rol;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
}
