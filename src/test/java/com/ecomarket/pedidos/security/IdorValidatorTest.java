package com.ecomarket.pedidos.security;

import com.ecomarket.pedidos.exception.AccesoDenegadoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdorValidatorTest {

    @Test
    void verificarAccesoEntidadCliente_RolNoCliente_AccesoPermitido() {
        assertDoesNotThrow(() -> IdorValidator.verificarAccesoEntidadCliente(2L, "ADMINISTRADOR", "1"));
        assertDoesNotThrow(() -> IdorValidator.verificarAccesoEntidadCliente(null, "GERENTE", "1"));
        assertDoesNotThrow(() -> IdorValidator.verificarAccesoEntidadCliente(2L, "EMPLEADO", null));
        assertDoesNotThrow(() -> IdorValidator.verificarAccesoEntidadCliente(2L, "SISTEMA", "1"));
    }

    @Test
    void verificarAccesoEntidadCliente_RolClienteIdCoincide_AccesoPermitido() {
        assertDoesNotThrow(() -> IdorValidator.verificarAccesoEntidadCliente(1L, "CLIENTE", "1"));
        assertDoesNotThrow(() -> IdorValidator.verificarAccesoEntidadCliente(1L, "cliente", "1"));
    }

    @Test
    void verificarAccesoEntidadCliente_RolClienteIdEntidadNull_LanzaExcepcion() {
        assertThrows(AccesoDenegadoException.class, 
            () -> IdorValidator.verificarAccesoEntidadCliente(null, "CLIENTE", "1"));
    }

    @Test
    void verificarAccesoEntidadCliente_RolClienteIdUsuarioNull_LanzaExcepcion() {
        assertThrows(AccesoDenegadoException.class, 
            () -> IdorValidator.verificarAccesoEntidadCliente(1L, "CLIENTE", null));
    }

    @Test
    void verificarAccesoEntidadCliente_RolClienteIdsDistintos_LanzaExcepcion() {
        assertThrows(AccesoDenegadoException.class, 
            () -> IdorValidator.verificarAccesoEntidadCliente(2L, "CLIENTE", "1"));
    }

    @Test
    void verificarNoEsCliente_RolNoCliente_AccesoPermitido() {
        assertDoesNotThrow(() -> IdorValidator.verificarNoEsCliente("ADMINISTRADOR"));
        assertDoesNotThrow(() -> IdorValidator.verificarNoEsCliente("GERENTE"));
        assertDoesNotThrow(() -> IdorValidator.verificarNoEsCliente(null));
    }

    @Test
    void verificarNoEsCliente_RolCliente_LanzaExcepcion() {
        assertThrows(AccesoDenegadoException.class, 
            () -> IdorValidator.verificarNoEsCliente("CLIENTE"));
    }
}
