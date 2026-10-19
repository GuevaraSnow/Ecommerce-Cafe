package com.uniquindio.ecommerce.infrastructure.rest.exception;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Traduce las excepciones a respuestas HTTP uniformes para todos los controladores:
 * regla de negocio violada, 400; recurso inexistente, 404; datos mal formados, 400.
 * Los controladores no capturan excepciones: solo las dejan salir hasta aquí.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReglaDominioException.class)
    public ResponseEntity<ErrorResponse> manejarReglaDominio(ReglaDominioException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(NoSuchElementException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, "El recurso solicitado no existe");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return construirRespuesta(HttpStatus.BAD_REQUEST, mensaje);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarCuerpoInvalido(HttpMessageNotReadableException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es válido");
    }

    private ResponseEntity<ErrorResponse> construirRespuesta(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(new ErrorResponse(LocalDateTime.now(), status.value(), mensaje));
    }
}
