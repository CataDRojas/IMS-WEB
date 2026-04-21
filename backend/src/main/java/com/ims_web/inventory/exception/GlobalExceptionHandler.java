package com.ims_web.inventory.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({DataIntegrityViolationException.class, UncategorizedSQLException.class})
    public ResponseEntity<Map<String, Object>> handleDatabaseException(Exception ex, HttpServletRequest request) {

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

                if (rawMessage.contains("MovimientoDetalle")) {
                    errorCode = "ERR_FK_MOVIMIENTO_DETALLE";
                    userMessage = "Invalid Movimiento or Producto reference";
                }
                else if (rawMessage.contains("productos")) {
                    errorCode = "ERR_FK_PRODUCTO";
                    userMessage = "Invalid Categoria or Descuento reference";
                }
                else if (rawMessage.contains("Categoria")) {
                    errorCode = "ERR_FK_CATEGORIA";
                    userMessage = "Invalid Descuento reference";
                }
                else {
                    errorCode = "ERR_FK_GENERIC";
                    userMessage = "Foreign key constraint violation";
                }
            }

            else if (rawMessage.contains("chk_producto_stock")) {
                errorCode = "ERR_PRODUCTO_STOCK_INVALID";
                userMessage = "Producto stock must be >= 0";
            }
            else if (rawMessage.contains("chk_producto_precio")) {
                errorCode = "ERR_PRODUCTO_PRECIO_INVALID";
                userMessage = "Producto price must be >= 0";
            }
            else if (rawMessage.contains("chk_det_cantidad")) {
                errorCode = "ERR_DETALLE_CANTIDAD_INVALID";
                userMessage = "Detalle cantidad cannot be zero";
            }
            else if (rawMessage.contains("chk_det_precio_unit")) {
                errorCode = "ERR_DETALLE_PRECIO_UNIT_INVALID";
                userMessage = "Detalle unit price must be >= 0";
            }
            else if (rawMessage.contains("chk_det_precio_total")) {
                errorCode = "ERR_DETALLE_PRECIO_TOTAL_INVALID";
                userMessage = "Detalle total must be >= 0";
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex, HttpServletRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("errorCode", "ERR_INTERNAL");
        body.put("message", "Unexpected error");
        body.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

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