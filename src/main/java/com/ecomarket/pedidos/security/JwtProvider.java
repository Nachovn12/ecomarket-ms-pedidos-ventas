package com.ecomarket.pedidos.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtProvider.class);

    @Value("${jwt.secret}")
    private String secret;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        if (secret == null || secret.trim().isEmpty() || secret.length() < 32) {
            throw new IllegalArgumentException("jwt.secret no definido o muy corto (<32 chars) en ms-pedidos. El servicio no puede iniciar de forma segura.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generarTokenServicio(String rolServicio) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600_000L); // 1 hora de validez para token de servicio inter-MS

        return Jwts.builder()
                .subject("ms-pedidos-ventas")
                .claim("rol", rolServicio)
                .claim("idUsuario", 0L)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Token JWT inválido en ms-pedidos-ventas: {}", e.getMessage());
            return false;
        }
    }

    public Claims obtenerClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Error al extraer claims de token malformado en ms-pedidos-ventas: {}", e.getMessage());
            return null;
        }
    }

    public String obtenerRol(String token) {
        Claims claims = obtenerClaims(token);
        if (claims == null) return null;
        Object rolObj = claims.get("rol");
        return rolObj != null ? rolObj.toString() : null;
    }

    public Long obtenerIdUsuario(String token) {
        Claims claims = obtenerClaims(token);
        if (claims == null) return null;
        Object idObj = claims.get("idUsuario");
        if (idObj instanceof Number) {
            return ((Number) idObj).longValue();
        } else if (idObj instanceof String) {
            try {
                return Long.parseLong((String) idObj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}

