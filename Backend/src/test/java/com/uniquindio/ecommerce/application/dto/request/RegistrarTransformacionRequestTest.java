package com.uniquindio.ecommerce.application.dto.request;

import com.uniquindio.ecommerce.Domain.valueobject.PerfilTueste;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Las anotaciones del Request se disparan, también en la cantidad anidada; el rango de la merma lo valida el dominio. */
class RegistrarTransformacionRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> camposInvalidos(RegistrarTransformacionRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    private RegistrarTransformacionRequest con(CantidadRequest cantidad, Double merma) {
        return new RegistrarTransformacionRequest("LOT-2026-001", cantidad, merma, LocalDate.of(2026, 10, 28),
                PerfilTueste.MEDIO);
    }

    @Test
    void unRequestCompletoNoTieneViolaciones() {
        assertTrue(camposInvalidos(con(new CantidadRequest(50.0, "kg"), 0.18)).isEmpty());
    }

    @Test
    void laValidacionBajaHastaLosCamposDeLaCantidadDeEntrada() {
        assertEquals(Set.of("cantidadEntrada.valor", "cantidadEntrada.unidad"),
                camposInvalidos(con(new CantidadRequest(null, " "), 0.18)));
        assertEquals(Set.of("cantidadEntrada.valor"), camposInvalidos(con(new CantidadRequest(-5.0, "kg"), 0.18)));
    }

    @Test
    void laMermaDebeSerMayorQueCero() {
        CantidadRequest cantidad = new CantidadRequest(50.0, "kg");

        assertEquals(Set.of("merma"), camposInvalidos(con(cantidad, 0.0)));
        assertEquals(Set.of("merma"), camposInvalidos(con(cantidad, -0.1)));
    }
}
