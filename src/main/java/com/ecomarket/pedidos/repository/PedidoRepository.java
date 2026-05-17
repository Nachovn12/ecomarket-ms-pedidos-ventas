package com.ecomarket.pedidos.repository;

import com.ecomarket.pedidos.entity.EstadoPedido;
import com.ecomarket.pedidos.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByIdCliente(Long idCliente);
    List<Pedido> findByIdClienteOrderByFechaCreacionDesc(Long idCliente);
    List<Pedido> findByEstado(EstadoPedido estado);
}