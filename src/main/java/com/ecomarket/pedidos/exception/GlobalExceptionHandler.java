package com.ecomarket.pedidos.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejador centralizado de excepciones para ms-pedidos-ventas.
 * Retorna respuestas JSON estructuradas con timestamp, status, error, message y path.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("Argumento inválido: {} - path: {}", ex.getMessage(), req.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> manejarConflict(
            IllegalStateException ex, HttpServletRequest req) {
        log.warn("Conflicto de estado: {} - path: {}", ex.getMessage(), req.getRequestURI());
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req.getRequestURI());
    }


    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> manejarNoEncontrado(
            RecursoNoEncontradoException ex, HttpServletRequest req) {
        log.warn("Recurso no encontrado: {} - path: {}", ex.getMessage(), req.getRequestURI());
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(ConflictoNegocioException.class)
    public ResponseEntity<Map<String, Object>> manejarConflictoNegocio(
            ConflictoNegocioException ex, HttpServletRequest req) {
        log.warn("Conflicto de negocio: {} - path: {}", ex.getMessage(), req.getRequestURI());
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<Map<String, Object>> manejarStockInsuficiente(
            StockInsuficienteException ex, HttpServletRequest req) {
        log.warn("Stock insuficiente: {} - path: {}", ex.getMessage(), req.getRequestURI());
        Map<String, Object> body = buildBodyMap(HttpStatus.CONFLICT, "Stock Insuficiente",
                ex.getMessage(), req.getRequestURI());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacion(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errores.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Error de validación en path: {} - campos: {}", req.getRequestURI(), errores);
        Map<String, Object> body = buildBodyMap(HttpStatus.BAD_REQUEST, "Validation Error",
                "Error de validación en los campos enviados", req.getRequestURI());
        body.put("validaciones", errores);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                   HttpServletRequest req) {
        log.warn("Conflicto de integridad de datos - path: {}", req.getRequestURI());
        return buildResponse(HttpStatus.CONFLICT, "Conflict",
                "Violacion de integridad de datos: registro duplicado o referencia invalida", req.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpServletRequest req) {
        log.warn("Cuerpo de la peticion no legible o invalido - path: {}", req.getRequestURI());
        return buildResponse(HttpStatus.BAD_REQUEST, "Bad Request",
                "El cuerpo de la peticion esta vacio, malformado o no es JSON valido", req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarException(
            Exception ex, HttpServletRequest req) {
        log.error("Error interno no controlado - path: {}", req.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Error interno en el servidor", req.getRequestURI());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error,
                                                               String message, String path) {
        return ResponseEntity.status(status).body(buildBodyMap(status, error, message, path));
    }

    private Map<String, Object> buildBodyMap(HttpStatus status, String error,
                                              String message, String path) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);
        return body;
    }
}
