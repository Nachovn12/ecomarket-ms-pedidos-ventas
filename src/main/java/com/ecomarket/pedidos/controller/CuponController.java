package com.ecomarket.pedidos.controller;

import com.ecomarket.pedidos.entity.Cupon;
import com.ecomarket.pedidos.service.CuponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/cupones")
public class CuponController {

    @Autowired
    private CuponService service;

    @PostMapping
    public Cupon crearCupon(@RequestBody Cupon cupon) {
        return service.crearCupon(cupon);
    }

    @PostMapping("/aplicar")
    public Map<String, Object> aplicarCupon(
            @RequestParam String codigo,
            @RequestParam Double subtotal) {
        return service.aplicarCupon(codigo, subtotal);
    }
}