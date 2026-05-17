package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.dto.CrearDevolucionRequest;
import com.ecomarket.pedidos.dto.CrearReclamacionRequest;
import com.ecomarket.pedidos.entity.Devolucion;
import com.ecomarket.pedidos.entity.Reclamacion;
import com.ecomarket.pedidos.repository.DevolucionRepository;
import com.ecomarket.pedidos.repository.ReclamacionRepository;
import org.springframework.stereotype.Service;

@Service
public class DevolucionService {

    private final DevolucionRepository devolucionRepository;
    private final ReclamacionRepository reclamacionRepository;

    public DevolucionService(DevolucionRepository devolucionRepository,
                             ReclamacionRepository reclamacionRepository) {
        this.devolucionRepository = devolucionRepository;
        this.reclamacionRepository = reclamacionRepository;
    }

    public Devolucion crearDevolucion(CrearDevolucionRequest request) {
        Devolucion devolucion = new Devolucion();
        devolucion.setIdCliente(request.getIdCliente());
        devolucion.setIdPedido(request.getIdPedido());
        devolucion.setIdVenta(request.getIdVenta());
        devolucion.setMotivo(request.getMotivo());
        return devolucionRepository.save(devolucion);
    }

    public Reclamacion crearReclamacion(CrearReclamacionRequest request) {
        Reclamacion reclamacion = new Reclamacion();
        reclamacion.setIdCliente(request.getIdCliente());
        reclamacion.setIdPedido(request.getIdPedido());
        reclamacion.setIdVenta(request.getIdVenta());
        reclamacion.setMotivo(request.getMotivo());
        reclamacion.setDescripcion(request.getDescripcion());
        return reclamacionRepository.save(reclamacion);
    }
}