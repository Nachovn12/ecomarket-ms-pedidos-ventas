package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.dto.AplicarCuponResponse;
import com.ecomarket.pedidos.entity.CuponDescuento;
import com.ecomarket.pedidos.entity.TipoDescuento;
import com.ecomarket.pedidos.repository.CuponDescuentoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class CuponDescuentoService {

    private final CuponDescuentoRepository cuponDescuentoRepository;

    public CuponDescuentoService(CuponDescuentoRepository cuponDescuentoRepository) {
        this.cuponDescuentoRepository = cuponDescuentoRepository;
    }

    public CuponDescuento crearCupon(CuponDescuento cuponDescuento) {
        return cuponDescuentoRepository.save(cuponDescuento);
    }

    public AplicarCuponResponse aplicarCupon(String codigo, Double subtotal) {
        CuponDescuento cupon = cuponDescuentoRepository.findByCodigoIgnoreCase(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Cupón inválido: el código no existe"));

        validarCupon(cupon, subtotal);

        Double descuento = calcularDescuento(cupon, subtotal);
        Double totalFinal = Math.max(0, subtotal - descuento);

        return new AplicarCuponResponse(
                cupon.getCodigo(),
                subtotal,
                descuento,
                totalFinal,
                "Cupón aplicado correctamente"
        );
    }

    private void validarCupon(CuponDescuento cupon, Double subtotal) {
        if (Boolean.FALSE.equals(cupon.getActivo())) {
            throw new IllegalArgumentException("Cupón inválido: el cupón está deshabilitado");
        }
        if (cupon.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cupón vencido: la fecha de expiración fue " + cupon.getFechaVencimiento());
        }
        if (cupon.getMontoMinimo() != null && subtotal < cupon.getMontoMinimo()) {
            throw new IllegalArgumentException("El carrito no cumple el monto mínimo para este cupón");
        }
    }

    private Double calcularDescuento(CuponDescuento cupon, Double subtotal) {
        if (cupon.getTipoDescuento() == TipoDescuento.PORCENTAJE) {
            return subtotal * (cupon.getValorDescuento() / 100);
        }
        if (cupon.getTipoDescuento() == TipoDescuento.MONTO_FIJO) {
            return cupon.getValorDescuento();
        }
        throw new IllegalArgumentException("Tipo de descuento no soportado");
    }
}