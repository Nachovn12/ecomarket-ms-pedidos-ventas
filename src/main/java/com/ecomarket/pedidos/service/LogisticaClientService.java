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
 * Cliente REST para comunicacion con MS logistica y envios.
 * Implementa IE 2.4.1 (Comunicacion entre MS) con manejo de timeout y errores remotos.
 * Solicita el despacho del pedido al MS Logistica luego de confirmar el pedido.
 */
@Service
@RequiredArgsConstructor
public class LogisticaClientService {

    private static final Logger log = LoggerFactory.getLogger(LogisticaClientService.class);

    private final RestTemplate restTemplate;

    @Value("")
    private String msLogisticaUrl;

    /**
     * Solicita la creacion de un envio en MS Logistica para un pedido confirmado.
     * POST /api/envios con idPedido, origen, destino y datos basicos.
     * Si el MS no responde o falla, retorna false (la operacion queda como pendiente
     * y se puede reintentar luego; no bloquea la confirmacion del pedido).
     */
    public boolean solicitarDespacho(Long idPedido, String origen, String destino) {
        String url = msLogisticaUrl + "/api/envios";
        log.info("Solicitando despacho en MS Logistica. idPedido={}, url={}", idPedido, url);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Rol-Usuario", "SISTEMA");
            headers.set("Content-Type", "application/json");
            Map<String, Object> body = Map.of(
                    "idPedido", idPedido,
                    "origen", origen != null ? origen : "Centro de distribucion EcoMarket",
                    "destino", destino != null ? destino : "Sin especificar"
            );
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            log.info("Despacho solicitado correctamente. idPedido={}", idPedido);
            return true;
        } catch (HttpClientErrorException e) {
            log.warn("Error al solicitar despacho. idPedido={}, status={}, body={}",
                    idPedido, e.getStatusCode(), e.getResponseBodyAsString());
            return false;
        } catch (ResourceAccessException e) {
            log.error("MS Logistica no disponible al solicitar despacho. idPedido={}, error={}",
                    idPedido, e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Error inesperado al solicitar despacho. idPedido={}, error={}",
                    idPedido, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica la conectividad con el MS logistica y envios.
     * GET /api/envios
     */
    public Map<String, Object> ping() {
        String url = msLogisticaUrl + "/api/envios";
        log.info("Llamando a MS Logistica y Envios. url={}", url);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Rol-Usuario", "SISTEMA");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class).getBody();
            log.info("MS Logistica y Envios respondio OK");
            return response;
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("MS Logistica y Envios respondio 404");
            return null;
        } catch (ResourceAccessException e) {
            log.error("MS Logistica y Envios no disponible. error={}", e.getMessage());
            throw new IllegalStateException("MS Logistica y Envios no esta disponible. Intente nuevamente en unos momentos.");
        } catch (Exception e) {
            log.error("Error inesperado al llamar a MS Logistica y Envios. error={}", e.getMessage());
            throw new IllegalStateException("No se pudo comunicar con MS Logistica y Envios.");
        }
    }
}