package com.uniquindio.ecommerce.application.dto.request;

import com.uniquindio.ecommerce.Domain.valueobject.Rol;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** La contraseña debe tener al menos 8 caracteres ya en el Request; que sea segura (mayúscula y número) lo valida el dominio. */
class RegistrarUsuarioRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private Set<String> camposInvalidos(String contrasena) {
        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest("Ana Gomez", "ana@correo.com", contrasena,
                "3011112233", LocalDate.of(1998, 3, 14), Rol.COMPRADOR);
        return validator.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    void unaContrasenaDeOchoCaracteresONoTieneViolaciones() {
        assertTrue(camposInvalidos("Cafe2026*").isEmpty());
        assertTrue(camposInvalidos("12345678").isEmpty());
    }

    @Test
    void unaContrasenaDeMenosDeOchoCaracteresSeReporta() {
        assertEquals(Set.of("contrasena"), camposInvalidos("Cafe1*"));
    }

    @Test
    void unaContrasenaVaciaSeReporta() {
        assertEquals(Set.of("contrasena"), camposInvalidos("   "));
    }
}
