package com.ecomarket.pedidos.security;

import com.ecomarket.pedidos.exception.AccesoDenegadoException;

public final class IdorValidator {

    private IdorValidator() {}

    public static void verificarAccesoEntidadCliente(Long idClienteEntidad, String rol, String idUsuario) {
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            if (idClienteEntidad == null || idUsuario == null || !idUsuario.equals(idClienteEntidad.toString())) {
                throw new AccesoDenegadoException("Acceso denegado: No tiene permisos para consultar o modificar información de otro cliente.");
            }
        }
    }

    public static void verificarNoEsCliente(String rol) {
        if ("CLIENTE".equalsIgnoreCase(rol)) {
            throw new AccesoDenegadoException("Acceso denegado: Los clientes no tienen permiso para acceder a listados generales u operaciones administrativas.");
        }
    }
}
