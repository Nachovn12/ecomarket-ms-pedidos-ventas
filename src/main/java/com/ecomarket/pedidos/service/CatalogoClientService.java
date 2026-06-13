package com.ecomarket.pedidos.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Cliente REST para comunicacion con MS catalogo.
 * Implementa IE 2.4.1 (Comunicacion entre MS) con manejo de timeout y errores remotos.
 */
@Service
@RequiredArgsConstructor
public class CatalogoClientService {

    private static final Logger log = LoggerFactory.getLogger(CatalogoClientService.class);

    private final RestTemplate restTemplate;

    @Value("${ms.catalogo.url}")
    private String msCatalogoUrl;

    /**
     * Obtiene un producto por su ID desde el MS catalogo.
     * @return Map con los datos del producto (incluye idProducto, nombre, precio, estado)
     * @throws IllegalStateException si el MS catalogo no esta disponible
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> obtenerProducto(Long idProducto) {
        String url = msCatalogoUrl + "/api/productos/" + idProducto;
        log.info("Consultando producto en MS Catalogo. idProducto={}, url={}", idProducto, url);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Rol-Usuario", "SISTEMA");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            Map<String, Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class).getBody();
            log.info("Producto obtenido correctamente. idProducto={}", idProducto);
            return response;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Producto no encontrado en MS Catalogo. idProducto={}", idProducto);
            return null;
        } catch (ResourceAccessException e) {
            log.error("MS Catalogo no disponible. idProducto={}, error={}", idProducto, e.getMessage());
            throw new IllegalStateException("MS Catalogo no esta disponible. Intente nuevamente en unos momentos.");
        } catch (Exception e) {
            log.error("Error inesperado al consultar producto en MS Catalogo. idProducto={}, error={}",
                    idProducto, e.getMessage());
            throw new IllegalStateException("No se pudo comunicar con MS Catalogo.");
        }
    }

    /**
     * Verifica la conectividad con el MS catalogo.
     * GET /api/productos
     */
    public Map<String, Object> ping() {
        String url = msCatalogoUrl + "/api/productos";
        log.info("Llamando a MS Catalogo. url={}", url);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Rol-Usuario", "SISTEMA");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class).getBody();
            log.info("MS Catalogo respondio OK");
            return response;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("MS Catalogo respondio 404");
            return null;
        } catch (ResourceAccessException e) {
            log.error("MS Catalogo no disponible. error={}", e.getMessage());
            throw new IllegalStateException("MS Catalogo no esta disponible. Intente nuevamente en unos momentos.");
        } catch (Exception e) {
            log.error("Error inesperado al llamar a MS Catalogo. error={}", e.getMessage());
            throw new IllegalStateException("No se pudo comunicar con MS Catalogo.");
        }
    }
}