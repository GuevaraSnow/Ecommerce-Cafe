package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Comprueba que las anotaciones de los Requests de Reseña se disparan; las reglas de negocio las valida el dominio. */
class ResenaRequestsTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> camposInvalidos(Object request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void dejarResenaCompletaNoTieneViolaciones() {
        assertTrue(camposInvalidos(new DejarResenaRequest("pres-1", 5, "Notas a panela")).isEmpty());
    }

    @Test
    void dejarResenaSinDatosMarcaTodosLosCampos() {
        assertEquals(Set.of("presentacionId", "calificacion", "texto"),
                camposInvalidos(new DejarResenaRequest("", null, " ")));
    }

    @Test
    void laCalificacionVaDe1A5() {
        assertEquals(Set.of("calificacion"), camposInvalidos(new DejarResenaRequest("pres-1", 0, "Regular")));
        assertEquals(Set.of("calificacion"), camposInvalidos(new DejarResenaRequest("pres-1", 6, "Excelente")));
        assertTrue(camposInvalidos(new DejarResenaRequest("pres-1", 1, "Malo")).isEmpty());
    }

    @Test
    void elComentarioNoPuedeSuperar500Caracteres() {
        assertEquals(Set.of("texto"), camposInvalidos(new DejarResenaRequest("pres-1", 4, "a".repeat(501))));
    }

    @Test
    void responderResenaExigeTextoDeMaximo500Caracteres() {
        assertTrue(camposInvalidos(new ResponderResenaRequest("Gracias por tu compra")).isEmpty());
        assertEquals(Set.of("texto"), camposInvalidos(new ResponderResenaRequest(" ")));
        assertEquals(Set.of("texto"), camposInvalidos(new ResponderResenaRequest("a".repeat(501))));
    }
}
