package com.ProyectoAula.GymAssist.mobile;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/** Para la API móvil todos los errores salen como {"error": "mensaje"}. */
@RestControllerAdvice(basePackages = "com.ProyectoAula.GymAssist.mobile")
public class MobileErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(MobileErrorHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> status(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("error", String.valueOf(e.getReason())));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> runtime(RuntimeException e) {
        log.warn("API móvil: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage() == null ? "Error inesperado" : e.getMessage()));
    }
}
