package com.ecomarket.pedidos.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider();
        ReflectionTestUtils.setField(jwtProvider, "secret", "EcoMarketSpATestSecretKeyHS256ForAutomatedTestsOnly2026x9876543210");
        jwtProvider.init();
    }

    @Test
    void generarTokenServicio_y_validarToken_Exito() {
        String token = jwtProvider.generarTokenServicio("SISTEMA");
        assertNotNull(token);
        assertTrue(jwtProvider.validarToken(token));

        Claims claims = jwtProvider.obtenerClaims(token);
        assertNotNull(claims);
        assertEquals("ms-pedidos-ventas", claims.getSubject());
        assertEquals("SISTEMA", jwtProvider.obtenerRol(token));
        assertEquals(0L, jwtProvider.obtenerIdUsuario(token));
    }

    @Test
    void validarToken_TokenInvalido_RetornaFalse() {
        assertFalse(jwtProvider.validarToken("token.invalido.malformado"));
    }

    @Test
    void init_SecretCorto_LanzaExcepcion() {
        JwtProvider corto = new JwtProvider();
        ReflectionTestUtils.setField(corto, "secret", "short");
        assertThrows(IllegalArgumentException.class, () -> corto.init());
    }
}

