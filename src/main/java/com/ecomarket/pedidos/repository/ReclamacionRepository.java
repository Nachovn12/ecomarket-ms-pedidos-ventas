package com.ecomarket.pedidos.repository;

import com.ecomarket.pedidos.model.Reclamacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReclamacionRepository extends JpaRepository<Reclamacion, Long> {

    List<Reclamacion> findByIdPedido(Long idPedido);

    List<Reclamacion> findByIdVenta(Long idVenta);
}
