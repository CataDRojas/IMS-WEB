package com.ims_web.inventory.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================
    // AUTH ERRORS
    // =========================

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("errorCode", "ERR_AUTH_INVALID");
        body.put("message", "Credenciales incorrectas");
        body.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("errorCode", "ERR_FORBIDDEN");
        body.put("message", "Acceso denegado");
        body.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    // =========================
    // DATABASE + TRIGGERS + SQL ERRORS
    // =========================

    @ExceptionHandler({DataIntegrityViolationException.class, UncategorizedSQLException.class})
    public ResponseEntity<Map<String, Object>> handleDatabaseException(
            Exception ex,
            HttpServletRequest request
    ) {

        String rawMessage = extractRootMessage(ex);

        String errorCode = "ERR_DATABASE";
        String userMessage = "Database error";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (rawMessage != null && rawMessage.contains("|")) {
            String[] parts = rawMessage.split("\\|", 2);
            errorCode = parts[0];
            userMessage = parts[1];
        }

        else if (rawMessage != null) {

            if (rawMessage.contains("Duplicate entry")) {
                errorCode = "ERR_DUPLICATE";
                userMessage = "Duplicate value violates unique constraint";
            }

            else if (rawMessage.contains("foreign key constraint fails")) {
                errorCode = "ERR_FK_CONSTRAINT";
                userMessage = "Foreign key constraint violation";
            }

            else if (rawMessage.contains("chk_iva")) {
                errorCode = "ERR_CONFIG_IVA_INVALID";
                userMessage = "IVA must be between 0 and 100";
            }

            else if (rawMessage.contains("chk_movimiento_tipo")) {
                errorCode = "ERR_MOVIMIENTO_TIPO_INVALID";
                userMessage = "Invalid movimiento tipo";
            }

            else if (rawMessage.contains("chk_movimiento_estado")) {
                errorCode = "ERR_MOVIMIENTO_ESTADO_INVALID";
                userMessage = "Invalid movimiento estado";
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("errorCode", errorCode);
        body.put("message", userMessage);
        body.put("path", request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }

    // =========================
    // STOCK LOGIC ERRORS (NEW - JAVA LAYER)
    // =========================

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request
    ) {

        String raw = ex.getMessage();

        String errorCode = "ERR_BUSINESS_RULE";
        String userMessage = "Business rule violation";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (raw != null && raw.contains("|")) {
            String[] parts = raw.split("\\|", 2);
            errorCode = parts[0];
            userMessage = parts[1];
        }

        else if (raw != null) {

            // NEW STOCK VALIDATION ERRORS
            if (raw.contains("ERR_STOCK_NEGATIVE")) {
                errorCode = "ERR_STOCK_NEGATIVE";
                userMessage = "Stock would go below zero";
            }
        }

        Map<String, Object> body = new HashMap<>();
        body.put("errorCode", errorCode);
        body.put("message", userMessage);
        body.put("path", request.getRequestURI());

        return ResponseEntity.status(status).body(body);
    }

    // =========================
    // FALLBACK
    // =========================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {

        Map<String, Object> body = new HashMap<>();
        body.put("errorCode", "ERR_INTERNAL");
        body.put("message", "Unexpected error");
        body.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // =========================
    // HELPER
    // =========================

    private String extractRootMessage(Throwable ex) {
        Throwable root = ex;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }

        if (root instanceof SQLException) {
            return ((SQLException) root).getMessage();
        }

        return root.getMessage();
    }
}