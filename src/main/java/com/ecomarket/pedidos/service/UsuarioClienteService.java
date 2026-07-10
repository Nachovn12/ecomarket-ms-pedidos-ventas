package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.dto.ClienteDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Cliente REST para comunicacion con MS Usuarios e Identidad.
 * Permite verificar que un cliente exista y este activo antes de operar sobre el.
 */
@Service
public class UsuarioClienteService {

    private static final Logger log = LoggerFactory.getLogger(UsuarioClienteService.class);

    private final RestTemplate restTemplate;
    private final String usuariosServiceUrl;

    public UsuarioClienteService(RestTemplate restTemplate,
                                 @Value("${ms.usuarios.url:http://localhost:8083}") String usuariosServiceUrl) {
        this.restTemplate = restTemplate;
        this.usuariosServiceUrl = usuariosServiceUrl;
    }

    /**
     * Obtiene el perfil del cliente desde ms-usuarios-identidad.
     * @throws IllegalArgumentException si el cliente no existe o no está activo
     * @throws IllegalStateException si el servicio de usuarios no está disponible
     */
    public ClienteDTO obtenerCliente(Long idCliente) {
        String url = usuariosServiceUrl + "/api/usuarios/clientes/" + idCliente + "/perfil";
        log.info("Consultando cliente en MS Usuarios. idCliente={}, url={}", idCliente, url);
        try {
            ClienteDTO cliente = restTemplate.getForObject(url, ClienteDTO.class);
            log.info("Cliente obtenido correctamente. idCliente={}", idCliente);
            return cliente;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Cliente no encontrado en MS Usuarios. idCliente={}", idCliente);
            throw new IllegalArgumentException("Cliente no encontrado o inactivo con id: " + idCliente);
        } catch (ResourceAccessException e) {
            log.error("MS Usuarios no disponible. idCliente={}, error={}", idCliente, e.getMessage());
            throw new IllegalStateException("MS Usuarios no esta disponible. Intente nuevamente en unos momentos.");
        } catch (Exception e) {
            log.error("Error inesperado al consultar cliente en MS Usuarios. idCliente={}, error={}", idCliente, e.getMessage());
            throw new RuntimeException("Error al comunicar con el servicio de usuarios: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica que el cliente exista y este activo. Lanza excepcion si no es valido.
     */
    public void verificarCliente(Long idCliente) {
        obtenerCliente(idCliente);
    }
}
