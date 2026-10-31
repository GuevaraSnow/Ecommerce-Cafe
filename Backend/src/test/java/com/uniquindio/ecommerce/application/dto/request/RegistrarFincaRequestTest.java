package com.uniquindio.ecommerce.application.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Las anotaciones del Request se disparan, también en la ubicación anidada; las reglas de negocio las valida el dominio. */
class RegistrarFincaRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> camposInvalidos(RegistrarFincaRequest request) {
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void unRequestCompletoNoTieneViolaciones() {
        assertTrue(camposInvalidos(new RegistrarFincaRequest("Villa Laura",
                new UbicacionRequest("Quindío", "Calarcá"), 1650)).isEmpty());
    }

    @Test
    void laValidacionBajaHastaLosCamposDeLaUbicacion() {
        RegistrarFincaRequest ubicacionVacia = new RegistrarFincaRequest("Villa Laura", new UbicacionRequest(" ", ""), 1650);

        assertEquals(Set.of("ubicacion.departamento", "ubicacion.municipio"), camposInvalidos(ubicacionVacia));
    }

    @Test
    void laAltitudDebeSerMayorQueCero() {
        UbicacionRequest ubicacion = new UbicacionRequest("Quindío", "Calarcá");

        assertEquals(Set.of("altitud"), camposInvalidos(new RegistrarFincaRequest("Villa Laura", ubicacion, 0)));
        assertEquals(Set.of("altitud"), camposInvalidos(new RegistrarFincaRequest("Villa Laura", ubicacion, -10)));
    }
}
