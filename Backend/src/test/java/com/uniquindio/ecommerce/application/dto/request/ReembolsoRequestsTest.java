package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/** Comprueba que las anotaciones de SolicitarReembolsoRequest se disparan; las reglas de negocio las valida el dominio. */
class ReembolsoRequestsTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> camposInvalidos(Object request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void solicitarReembolsoCompletoNoTieneViolaciones() {
        assertTrue(camposInvalidos(new SolicitarReembolsoRequest("compra-1", "pres-1", "Llego humedo")).isEmpty());
    }

    @Test
    void solicitarReembolsoSinDatosMarcaTodosLosCampos() {
        assertEquals(Set.of("compraId", "presentacionId", "motivo"),
                camposInvalidos(new SolicitarReembolsoRequest(" ", null, "")));
    }

    @Test
    void elMotivoNoPuedeSuperar500Caracteres() {
        assertEquals(Set.of("motivo"),
                camposInvalidos(new SolicitarReembolsoRequest("compra-1", "pres-1", "a".repeat(501))));
    }
}
