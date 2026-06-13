package com.ecomarket.pedidos.service;

import com.ecomarket.pedidos.dto.AplicarCuponResponse;
import com.ecomarket.pedidos.dto.CuponDescuentoResponse;
import com.ecomarket.pedidos.model.CuponDescuento;
import com.ecomarket.pedidos.model.TipoDescuento;
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
                .orElseThrow(() -> new IllegalArgumentException("Cupon invalido: el codigo no existe"));

        validarCupon(cupon, subtotal);

        Double descuento = calcularDescuento(cupon, subtotal);
        Double totalFinal = Math.max(0.0, subtotal - descuento);

        return new AplicarCuponResponse(
                cupon.getCodigo(),
                subtotal,
                descuento,
                totalFinal,
                "Cupon aplicado correctamente"
        );
    }

    private void validarCupon(CuponDescuento cupon, Double subtotal) {
        if (Boolean.FALSE.equals(cupon.getActivo())) {
            throw new IllegalArgumentException("Cupon invalido: el cupon esta deshabilitado");
        }
        if (cupon.getFechaVencimiento() != null && cupon.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cupon vencido: la fecha de expiracion fue " + cupon.getFechaVencimiento());
        }
        if (cupon.getMontoMinimo() != null && subtotal < cupon.getMontoMinimo()) {
            throw new IllegalArgumentException("El carrito no cumple el monto minimo para este cupon");
        }
    }

    private Double calcularDescuento(CuponDescuento cupon, Double subtotal) {
        if (cupon.getTipoDescuento() == TipoDescuento.PORCENTAJE) {
            return subtotal * (cupon.getValorDescuento() / 100);
        }
        if (cupon.getTipoDescuento() == TipoDescuento.MONTO_FIJO) {
            return Math.min(cupon.getValorDescuento(), subtotal);
        }
        throw new IllegalArgumentException("Tipo de descuento no soportado");
    }


    public CuponDescuentoResponse toResponse(CuponDescuento cupon) {
        if (cupon == null) {
            return null;
        }
        CuponDescuentoResponse r = new CuponDescuentoResponse();
        r.setIdCupon(cupon.getIdCupon());
        r.setCodigo(cupon.getCodigo());
        r.setTipoDescuento(cupon.getTipoDescuento());
        r.setValorDescuento(cupon.getValorDescuento());
        r.setMontoMinimo(cupon.getMontoMinimo());
        r.setFechaVencimiento(cupon.getFechaVencimiento());
        r.setActivo(cupon.getActivo());
        return r;
    }
}