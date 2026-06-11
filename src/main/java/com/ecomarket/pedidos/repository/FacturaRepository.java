package com.ecomarket.pedidos.repository;

import com.ecomarket.pedidos.model.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacturaRepository extends JpaRepository<Factura, Long> {

    List<Factura> findByIdVenta(Long idVenta);

    Optional<Factura> findFirstByIdVenta(Long idVenta);

    boolean existsByIdVenta(Long idVenta);

    /** Obtiene el folio máximo registrado para generar el siguiente consecutivo. */
    @org.springframework.data.jpa.repository.Query("SELECT MAX(f.folio) FROM Factura f")
    java.util.Optional<Integer> findMaxFolio();
}
