package com.ecomarket.pedidos.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void doFilterInternal_TokenAusenteEnOperacion_retorna401() throws ServletException, IOException {
        request.setRequestURI("/api/pedidos/carritos/1");
        request.setMethod("GET");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_TokenInvalidoEnOperacion_retorna401() throws ServletException, IOException {
        request.setRequestURI("/api/pedidos/carritos/1");
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer token.invalido");

        when(jwtProvider.validarToken("token.invalido")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_TokenExpiradoEnOperacion_retorna401() throws ServletException, IOException {
        request.setRequestURI("/api/pedidos/carritos/1");
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer token.expirado");

        when(jwtProvider.validarToken("token.expirado")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_TokenValidoRolInsuficienteEnAdmin_retorna403() throws ServletException, IOException {
        request.setRequestURI("/api/pedidos/1/estado");
        request.setMethod("PATCH");
        request.addHeader("Authorization", "Bearer token.cliente");

        when(jwtProvider.validarToken("token.cliente")).thenReturn(true);
        when(jwtProvider.obtenerRol("token.cliente")).thenReturn("CLIENTE");
        when(jwtProvider.obtenerIdUsuario("token.cliente")).thenReturn(1L);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_IDORClienteEnHistorial_ClienteAccedeAOtroCliente_retorna403() throws ServletException, IOException {
        request.setRequestURI("/api/pedidos/clientes/10/historial");
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer token.cliente");

        when(jwtProvider.validarToken("token.cliente")).thenReturn(true);
        when(jwtProvider.obtenerRol("token.cliente")).thenReturn("CLIENTE");
        when(jwtProvider.obtenerIdUsuario("token.cliente")).thenReturn(1L); // ID en token=1, en URL=10 -> IDOR!

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_IDORClienteEnHistorial_ClienteAccedeASuPropioHistorial_Exito() throws ServletException, IOException {
        request.setRequestURI("/api/pedidos/clientes/10/historial");
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer token.cliente");

        when(jwtProvider.validarToken("token.cliente")).thenReturn(true);
        when(jwtProvider.obtenerRol("token.cliente")).thenReturn("CLIENTE");
        when(jwtProvider.obtenerIdUsuario("token.cliente")).thenReturn(10L); // ID coincide

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        ArgumentCaptor<jakarta.servlet.http.HttpServletRequest> captor = ArgumentCaptor.forClass(jakarta.servlet.http.HttpServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), eq(response));
        assertEquals("CLIENTE", captor.getValue().getHeader("X-Rol-Usuario"));
        assertEquals("10", captor.getValue().getHeader("X-Id-Usuario"));
    }

    @Test
    void doFilterInternal_TokenValidoRolPermitidoEnAdmin_Exito() throws ServletException, IOException {
        request.setRequestURI("/api/pedidos/1/estado");
        request.setMethod("PATCH");
        request.addHeader("Authorization", "Bearer token.gerente");

        when(jwtProvider.validarToken("token.gerente")).thenReturn(true);
        when(jwtProvider.obtenerRol("token.gerente")).thenReturn("GERENTE");
        when(jwtProvider.obtenerIdUsuario("token.gerente")).thenReturn(3L);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        ArgumentCaptor<jakarta.servlet.http.HttpServletRequest> captor = ArgumentCaptor.forClass(jakarta.servlet.http.HttpServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), eq(response));
        assertEquals("GERENTE", captor.getValue().getHeader("X-Rol-Usuario"));
    }

    @Test
    void doFilterInternal_SwaggerAndApiDocs_pasaSinToken() throws ServletException, IOException {
        String[] rutas = {"/v3/api-docs", "/swagger-ui/index.html", "/doc/swagger-ui/config"};
        for (String ruta : rutas) {
            MockHttpServletRequest req = new MockHttpServletRequest("GET", ruta);
            MockHttpServletResponse res = new MockHttpServletResponse();
            jwtAuthenticationFilter.doFilterInternal(req, res, filterChain);
            assertEquals(HttpStatus.OK.value(), res.getStatus());
        }
        verify(filterChain, times(3)).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_HistorialCliente_RolNoCliente_pasaSinValidarIDOR() throws ServletException, IOException {
        request.setRequestURI("/api/pedidos/clientes/10/historial");
        request.setMethod("GET");
        request.addHeader("Authorization", "Bearer token.gerente");

        when(jwtProvider.validarToken("token.gerente")).thenReturn(true);
        when(jwtProvider.obtenerRol("token.gerente")).thenReturn("GERENTE");
        when(jwtProvider.obtenerIdUsuario("token.gerente")).thenReturn(3L);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpStatus.OK.value(), response.getStatus());
        verify(filterChain).doFilter(any(), eq(response));
    }

    @Test
    void doFilterInternal_CuponesNoGetSinPermisos_retorna403() throws ServletException, IOException {
        request.setRequestURI("/api/pedidos/cupones");
        request.setMethod("POST");
        request.addHeader("Authorization", "Bearer token.cliente");

        when(jwtProvider.validarToken("token.cliente")).thenReturn(true);
        when(jwtProvider.obtenerRol("token.cliente")).thenReturn("CLIENTE");
        when(jwtProvider.obtenerIdUsuario("token.cliente")).thenReturn(1L);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_CuponesYEstadoConRolesPermitidos_Exito() throws ServletException, IOException {
        String[] roles = {"EMPLEADO", "ADMINISTRADOR", "SISTEMA"};
        for (String rol : roles) {
            MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/pedidos/cupones");
            MockHttpServletResponse res = new MockHttpServletResponse();
            String token = "token." + rol.toLowerCase();
            req.addHeader("Authorization", "Bearer " + token);

            when(jwtProvider.validarToken(token)).thenReturn(true);
            when(jwtProvider.obtenerRol(token)).thenReturn(rol);
            when(jwtProvider.obtenerIdUsuario(token)).thenReturn(null);

            jwtAuthenticationFilter.doFilterInternal(req, res, filterChain);
            assertEquals(HttpStatus.OK.value(), res.getStatus());
        }
        verify(filterChain, times(roles.length)).doFilter(any(), any());
    }

    @Test
    void doFilterInternal_VariacionesTokenYWrapperTest() throws ServletException, IOException {
        request.setRequestURI("/api/pedidos/carritos/1");
        request.setMethod("GET");
        request.addHeader("X-Token-Usuario", "Bearer token.xtoken");

        when(jwtProvider.validarToken("token.xtoken")).thenReturn(true);
        when(jwtProvider.obtenerRol("token.xtoken")).thenReturn("ADMINISTRADOR");
        when(jwtProvider.obtenerIdUsuario("token.xtoken")).thenReturn(5L);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        ArgumentCaptor<jakarta.servlet.http.HttpServletRequest> captor = ArgumentCaptor.forClass(jakarta.servlet.http.HttpServletRequest.class);
        verify(filterChain).doFilter(captor.capture(), eq(response));
        jakarta.servlet.http.HttpServletRequest wrapped = captor.getValue();
        assertNotNull(wrapped.getHeader("X-Rol-Usuario"));
        assertNotNull(wrapped.getHeaders("X-Rol-Usuario"));
        assertNotNull(wrapped.getHeaderNames());

        MockHttpServletRequest reqBasic = new MockHttpServletRequest("GET", "/api/pedidos/carritos/1");
        reqBasic.addHeader("Authorization", "Basic 12345");
        reqBasic.addHeader("X-Token-Usuario", "token.plano");
        MockHttpServletResponse resBasic = new MockHttpServletResponse();
        when(jwtProvider.validarToken("token.plano")).thenReturn(true);
        when(jwtProvider.obtenerRol("token.plano")).thenReturn("SISTEMA");
        jwtAuthenticationFilter.doFilterInternal(reqBasic, resBasic, filterChain);
    }
}
