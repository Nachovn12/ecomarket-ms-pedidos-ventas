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
 * Cliente REST para comunicacion con MS inventario y abastecimiento.
 * Implementa IE 2.4.1 (Comunicacion entre MS) con manejo de timeout y errores remotos.
 */
@Service
@RequiredArgsConstructor
public class InventarioClientService {

    private static final Logger log = LoggerFactory.getLogger(InventarioClientService.class);

    private final RestTemplate restTemplate;

    @Value("${ms.inventario.url}")
    private String msInventarioUrl;

    /**
     * Consulta el stock disponible de un producto en el MS inventario.
     * GET /api/inventario/productos/{idProducto}
     * @return Map con datos de inventario (stockActual, stockMinimo, etc.) o null si no existe
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> consultarStock(Long idProducto) {
        String url = msInventarioUrl + "/api/inventario/productos/" + idProducto;
        log.info("Consultando stock en MS Inventario. idProducto={}, url={}", idProducto, url);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Rol-Usuario", "SISTEMA");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            Map<String, Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class).getBody();
            log.info("Stock consultado correctamente. idProducto={}", idProducto);
            return response;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Producto no encontrado en MS Inventario. idProducto={}", idProducto);
            return null;
        } catch (ResourceAccessException e) {
            log.error("MS Inventario no disponible. idProducto={}, error={}", idProducto, e.getMessage());
            throw new IllegalStateException("MS Inventario no esta disponible. Intente nuevamente en unos momentos.");
        } catch (Exception e) {
            log.error("Error inesperado al consultar stock. idProducto={}, error={}", idProducto, e.getMessage());
            throw new IllegalStateException("No se pudo comunicar con MS Inventario.");
        }
    }

    /**
     * Descuenta stock de un producto en el MS inventario (al confirmar pedido).
     * POST /api/inventario/ajustes con cantidad negativa.
     * Si el MS no responde o falla, retorna false (la operacion queda como pendiente).
     */
    public boolean descontarStock(Long idProducto, Integer cantidad, String motivo) {
        String url = msInventarioUrl + "/api/inventario/ajustes";
        log.info("Descontando stock en MS Inventario. idProducto={}, cantidad={}", idProducto, cantidad);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Rol-Usuario", "SISTEMA");
            headers.set("Content-Type", "application/json");
            Map<String, Object> body = Map.of(
                    "idProducto", idProducto,
                    "cantidad", -Math.abs(cantidad),
                    "motivo", motivo != null ? motivo : "Venta/pedido confirmado"
            );
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            log.info("Stock descontado correctamente. idProducto={}, cantidad={}", idProducto, cantidad);
            return true;
        } catch (HttpClientErrorException e) {
            log.warn("Error al descontar stock. idProducto={}, status={}, body={}",
                    idProducto, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (ResourceAccessException e) {
            log.error("MS Inventario no disponible al descontar stock. error={}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error inesperado al descontar stock. error={}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica la conectividad con el MS inventario y abastecimiento.
     * GET /api/inventario/productos
     */
    public Map<String, Object> ping() {
        String url = msInventarioUrl + "/api/inventario/productos";
        log.info("Llamando a MS Inventario y Abastecimiento. url={}", url);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Rol-Usuario", "SISTEMA");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class).getBody();
            log.info("MS Inventario y Abastecimiento respondio OK");
            return response;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("MS Inventario y Abastecimiento respondio 404");
            return null;
        } catch (ResourceAccessException e) {
            log.error("MS Inventario y Abastecimiento no disponible. error={}", e.getMessage());
            throw new IllegalStateException("MS Inventario y Abastecimiento no esta disponible. Intente nuevamente en unos momentos.");
        } catch (Exception e) {
            log.error("Error inesperado al llamar a MS Inventario y Abastecimiento. error={}", e.getMessage());
            throw new IllegalStateException("No se pudo comunicar con MS Inventario y Abastecimiento.");
        }
    }
}