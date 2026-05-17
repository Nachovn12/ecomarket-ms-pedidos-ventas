package com.ecomarket.pedidos.repository;

import com.ecomarket.pedidos.entity.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    List<Venta> findByIdCliente(Long idCliente);
}