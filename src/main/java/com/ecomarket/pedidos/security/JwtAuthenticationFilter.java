package com.ecomarket.pedidos.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final Pattern IDOR_CLIENTE_HISTORIAL_PATTERN = Pattern.compile("^/api/pedidos/clientes/(\\d+)(/.*)?$");

    private final JwtProvider jwtProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Rutas de documentación y Swagger pasan libremente
        if (path.contains("/api-docs") || path.contains("/swagger-ui") || path.contains("/doc/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Requiere token JWT o cabecera X-Token-Usuario
        String token = extraerToken(request);

        if (token == null) {
            log.warn("Petición sin token JWT a endpoint protegido en ms-pedidos-ventas: {} {}", method, path);
            responderError(response, HttpStatus.UNAUTHORIZED, "Debe autenticarse enviando un token JWT en el header Authorization (Bearer) o X-Token-Usuario");
            return;
        }

        if (!jwtProvider.validarToken(token)) {
            log.warn("Token JWT inválido o expirado en ms-pedidos-ventas: {} {}", method, path);
            responderError(response, HttpStatus.UNAUTHORIZED, "Token JWT inválido o expirado");
            return;
        }

        String rol = jwtProvider.obtenerRol(token);
        Long idUsuario = jwtProvider.obtenerIdUsuario(token);

        // 1. Protección IDOR en el historial por URL: /api/pedidos/clientes/{idCliente}/historial
        Matcher matcherIdor = IDOR_CLIENTE_HISTORIAL_PATTERN.matcher(path);
        if (matcherIdor.matches()) {
            Long idClienteEnUrl = Long.parseLong(matcherIdor.group(1));
            if ("CLIENTE".equalsIgnoreCase(rol)) {
                if (idUsuario == null || !idUsuario.equals(idClienteEnUrl)) {
                    log.warn("IDOR detectado en ms-pedidos-ventas: Usuario CLIENTE {} intentó acceder a historial del cliente {}", idUsuario, idClienteEnUrl);
                    responderError(response, HttpStatus.FORBIDDEN, "Acceso denegado. No tiene permisos para consultar datos de otro cliente.");
                    return;
                }
            }
        }

        // 2. Control de mutaciones administrativas (cambios de estado, cupones)
        if (path.contains("/estado") || (path.startsWith("/api/pedidos/cupones") && !"GET".equalsIgnoreCase(method))) {
            if (!("EMPLEADO".equalsIgnoreCase(rol) || "GERENTE".equalsIgnoreCase(rol) ||
                    "ADMINISTRADOR".equalsIgnoreCase(rol) || "SISTEMA".equalsIgnoreCase(rol))) {
                log.warn("Intento de operación administrativa sin permisos en ms-pedidos-ventas. Rol={}, Path={}", rol, path);
                responderError(response, HttpStatus.FORBIDDEN, "No tiene permisos para realizar operaciones administrativas sobre pedidos, devoluciones o cupones.");
                return;
            }
        }

        HttpServletRequest wrappedRequest = envolverRequestConHeaders(request, rol, idUsuario != null ? idUsuario.toString() : null);
        filterChain.doFilter(wrappedRequest, response);
    }

    private String extraerToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        String xToken = request.getHeader("X-Token-Usuario");
        if (xToken != null && !xToken.trim().isEmpty()) {
            if (xToken.startsWith("Bearer ")) {
                return xToken.substring(7);
            }
            return xToken.trim();
        }
        return null;
    }

    private HttpServletRequest envolverRequestConHeaders(HttpServletRequest request, String rol, String idUsuario) {
        return new HttpServletRequestWrapper(request) {
            private final Map<String, String> customHeaders = new HashMap<>();
            {
                if (rol != null) customHeaders.put("X-Rol-Usuario", rol);
                if (idUsuario != null) customHeaders.put("X-Id-Usuario", idUsuario);
            }

            @Override
            public String getHeader(String name) {
                if (customHeaders.containsKey(name)) {
                    return customHeaders.get(name);
                }
                return super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                if (customHeaders.containsKey(name)) {
                    return Collections.enumeration(Collections.singletonList(customHeaders.get(name)));
                }
                return super.getHeaders(name);
            }

            @Override
            public Enumeration<String> getHeaderNames() {
                Set<String> names = new HashSet<>();
                Enumeration<String> superNames = super.getHeaderNames();
                while (superNames.hasMoreElements()) {
                    names.add(superNames.nextElement());
                }
                names.addAll(customHeaders.keySet());
                return Collections.enumeration(names);
            }
        };
    }

    private void responderError(HttpServletResponse response, HttpStatus status, String mensaje) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("timestamp", java.time.LocalDateTime.now().toString());
        errorBody.put("status", status.value());
        errorBody.put("error", status.getReasonPhrase());
        errorBody.put("mensaje", mensaje);

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}
