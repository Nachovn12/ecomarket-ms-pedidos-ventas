package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.dto.CrearDevolucionRequest;
import com.ecomarket.pedidos.dto.CrearReclamacionRequest;
import com.ecomarket.pedidos.entity.Devolucion;
import com.ecomarket.pedidos.entity.Reclamacion;
import com.ecomarket.pedidos.repository.DevolucionRepository;
import com.ecomarket.pedidos.repository.ReclamacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DevolucionService {

    private static final Logger log = LoggerFactory.getLogger(DevolucionService.class);

    private final DevolucionRepository devolucionRepository;
    private final ReclamacionRepository reclamacionRepository;

    public DevolucionService(DevolucionRepository devolucionRepository,
                             ReclamacionRepository reclamacionRepository) {
        this.devolucionRepository = devolucionRepository;
        this.reclamacionRepository = reclamacionRepository;
    }

    public Devolucion crearDevolucion(CrearDevolucionRequest request) {
        log.info("Registrando devolución para venta {}", request.getIdVenta());
        Devolucion devolucion = new Devolucion();
        devolucion.setIdCliente(request.getIdCliente());
        devolucion.setIdPedido(request.getIdPedido());
        devolucion.setIdVenta(request.getIdVenta());
        devolucion.setMotivo(request.getMotivo());
        return devolucionRepository.save(devolucion);
    }

    public List<Devolucion> listarDevoluciones() {
        log.info("Listando todas las devoluciones");
        return devolucionRepository.findAll();
    }

    public Devolucion obtenerDevolucion(Long id) {
        log.info("Buscando devolución con id {}", id);
        return devolucionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Devolución no encontrada: " + id));
    }

    public Devolucion actualizarEstadoDevolucion(Long id, String estado) {
        log.info("Actualizando estado de devolución {} a {}", id, estado);
        Devolucion devolucion = obtenerDevolucion(id);
        String estadoNormalizado = normalizarEstadoDevolucion(estado);
        devolucion.setEstado(estadoNormalizado);
        return devolucionRepository.save(devolucion);
    }

    public Reclamacion crearReclamacion(CrearReclamacionRequest request) {
        log.info("Registrando reclamación para cliente {}", request.getIdCliente());
        Reclamacion reclamacion = new Reclamacion();
        reclamacion.setIdCliente(request.getIdCliente());
        reclamacion.setIdPedido(request.getIdPedido());
        reclamacion.setIdVenta(request.getIdVenta());
        reclamacion.setMotivo(request.getMotivo());
        reclamacion.setDescripcion(request.getDescripcion());
        return reclamacionRepository.save(reclamacion);
    }

    public List<Reclamacion> listarReclamaciones() {
        log.info("Listando todas las reclamaciones");
        return reclamacionRepository.findAll();
    }

    public Reclamacion obtenerReclamacion(Long id) {
        log.info("Buscando reclamación con id {}", id);
        return reclamacionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reclamación no encontrada: " + id));
    }

    public Reclamacion actualizarEstadoReclamacion(Long id, String estado) {
        log.info("Actualizando estado de reclamación {} a {}", id, estado);
        Reclamacion reclamacion = obtenerReclamacion(id);
        String estadoNormalizado = normalizarEstadoReclamacion(estado);
        reclamacion.setEstado(estadoNormalizado);
        return reclamacionRepository.save(reclamacion);
    }

    private String normalizarEstadoDevolucion(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El estado de devolución es obligatorio");
        }
        String estadoNormalizado = estado.trim().toUpperCase();
        List<String> permitidos = List.of("SOLICITADA", "APROBADA", "RECHAZADA", "FINALIZADA");
        if (!permitidos.contains(estadoNormalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Estado de devolución inválido");
        }
        return estadoNormalizado;
    }

    private String normalizarEstadoReclamacion(String estado) {
        if (estado == null || estado.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El estado de reclamación es obligatorio");
        }
        String estadoNormalizado = estado.trim().toUpperCase();
        List<String> permitidos = List.of("ABIERTA", "EN_REVISION", "RESUELTA", "CERRADA");
        if (!permitidos.contains(estadoNormalizado)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Estado de reclamación inválido");
        }
        return estadoNormalizado;
    }
}