package com.ecomarket.pedidos.repository;

import com.ecomarket.pedidos.entity.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    List<Factura> findByIdVenta(Long idVenta);

    Optional<Factura> findFirstByIdVenta(Long idVenta);

    boolean existsByIdVenta(Long idVenta);
}
