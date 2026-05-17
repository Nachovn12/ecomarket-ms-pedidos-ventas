package com.ecomarket.pedidos.repository;

import com.ecomarket.pedidos.entity.HistorialPedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistorialPedidoRepository extends JpaRepository<HistorialPedido, Long> {

    List<HistorialPedido> findByIdPedidoOrderByFechaCambioDesc(Long idPedido);
}
