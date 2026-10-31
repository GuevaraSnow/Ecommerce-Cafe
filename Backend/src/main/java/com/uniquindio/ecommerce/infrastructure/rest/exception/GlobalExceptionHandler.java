package com.uniquindio.ecommerce.infrastructure.rest.exception;

import com.uniquindio.ecommerce.Domain.exception.CredencialesInvalidasException;
import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Traduce las excepciones a respuestas HTTP uniformes para todos los controladores:
 * regla de negocio violada, 422; credenciales inválidas, 401; sin permiso, 403; recurso inexistente, 404;
 * datos o parámetros mal formados, 400; y cualquier otro error inesperado, 500 con un mensaje genérico
 * (el detalle solo va al registro, nunca al cliente).
 * Los controladores no capturan excepciones: solo las dejan salir hasta aquí.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Los mismos textos los usa la seguridad al responder 401 y 403 desde el filtro. */
    public static final String MENSAJE_AUTENTICACION_REQUERIDA = "Se requiere un token válido para esta operación";
    public static final String MENSAJE_SIN_PERMISO = "No tienes permiso para esta operación";

    private static final Logger registro = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ReglaDominioException.class)
    public ResponseEntity<ErrorResponse> manejarReglaDominio(ReglaDominioException ex) {
        return construirRespuesta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        return construirRespuesta(HttpStatus.UNAUTHORIZED, ex.getMessage());
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

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> manejarValidacionDeParametros(HandlerMethodValidationException ex) {
        String mensaje = ex.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream())
                .map(MessageSourceResolvable::getDefaultMessage)
                .sorted()
                .collect(Collectors.joining("; "));
        return construirRespuesta(HttpStatus.BAD_REQUEST, mensaje);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> manejarParametroConTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, "El parámetro '" + ex.getName() + "' tiene un valor no válido");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> manejarParametroFaltante(MissingServletRequestParameterException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Falta el parámetro '" + ex.getParameterName() + "'");
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> manejarEncabezadoFaltante(MissingRequestHeaderException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, "Falta el encabezado '" + ex.getHeaderName() + "'");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> manejarRutaInexistente(NoResourceFoundException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, "El recurso solicitado no existe");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> manejarMetodoNoPermitido(HttpRequestMethodNotSupportedException ex) {
        return construirRespuesta(HttpStatus.METHOD_NOT_ALLOWED, "El método " + ex.getMethod() + " no está permitido en esta ruta");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> manejarTipoDeContenidoNoSoportado(HttpMediaTypeNotSupportedException ex) {
        return construirRespuesta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "El tipo de contenido no es compatible: se espera application/json");
    }

    /** Un {@code @PreAuthorize} que niega el acceso lanza esta excepción dentro del controlador; sin este manejador la atraparía el de {@code Exception}. */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> manejarAccesoDenegado(AccessDeniedException ex) {
        return construirRespuesta(HttpStatus.FORBIDDEN, MENSAJE_SIN_PERMISO);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> manejarAutenticacionRequerida(AuthenticationException ex) {
        return construirRespuesta(HttpStatus.UNAUTHORIZED, MENSAJE_AUTENTICACION_REQUERIDA);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarErrorInesperado(Exception ex) {
        registro.error("Error inesperado", ex);
        return construirRespuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado. Intenta de nuevo más tarde.");
    }

    private ResponseEntity<ErrorResponse> construirRespuesta(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(new ErrorResponse(LocalDateTime.now(), status.value(), mensaje));
    }
}
