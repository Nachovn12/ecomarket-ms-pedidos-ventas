package com.ecomarket.pedidos.repository;

import com.ecomarket.pedidos.entity.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarritoRepository extends JpaRepository<ItemCarrito, Long> {
}