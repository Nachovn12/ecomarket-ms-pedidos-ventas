package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.entity.Cupon;
import com.ecomarket.pedidos.repository.CuponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
public class CuponService {

    @Autowired
    private CuponRepository repository;

    public Map<String, Object> aplicarCupon(String codigo, Double subtotal) {

        Optional<Cupon> optional = repository.findByCodigo(codigo);

        if (optional.isEmpty()) {
            return Map.of("error", "Cupón inválido: el código no existe");
        }

        Cupon cupon = optional.get();

        if (!cupon.isActivo()) {
            return Map.of("error", "Cupón inválido: el cupón está deshabilitado");
        }

        if (cupon.getFechaVencimiento().isBefore(LocalDate.now())) {
            return Map.of("error", "Cupón vencido: la fecha de expiración fue " + cupon.getFechaVencimiento());
        }

        if (subtotal < cupon.getMontoMinimo()) {
            return Map.of("error", "El carrito no cumple el monto mínimo de $" + cupon.getMontoMinimo() + " para este cupón");
        }

        Double descuento = subtotal * (cupon.getDescuentoPorcentaje() / 100);
        Double total = subtotal - descuento;

        return Map.of(
            "mensaje",    "Cupón aplicado correctamente",
            "codigo",     cupon.getCodigo(),
            "subtotal",   subtotal,
            "descuento",  descuento,
            "totalFinal", total
        );
    }

    public Cupon crearCupon(Cupon cupon) {
        return repository.save(cupon);
    }
}