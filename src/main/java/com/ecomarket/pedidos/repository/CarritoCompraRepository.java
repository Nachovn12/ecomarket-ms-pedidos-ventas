package com.ecomarket.pedidos.repository;

import com.ecomarket.pedidos.model.CarritoCompra;
import com.ecomarket.pedidos.model.EstadoCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarritoCompraRepository extends JpaRepository<CarritoCompra, Long> {
    List<CarritoCompra> findByIdCliente(Long idCliente);
    List<CarritoCompra> findByIdClienteAndEstado(Long idCliente, EstadoCarrito estado);
}