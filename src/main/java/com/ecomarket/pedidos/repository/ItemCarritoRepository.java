package com.ecomarket.pedidos.repository;

import com.ecomarket.pedidos.entity.ItemCarrito;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemCarritoRepository extends JpaRepository<ItemCarrito, Long> {
}