package com.uniquindio.ecommerce.Domain.valueobject;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ContrasenaPlanaTest {

    @Test
    void aceptaContrasenaValida() {
        assertDoesNotThrow(() -> new ContrasenaPlana("Password123!"));
    }

    @Test
    void rechazaContrasenaCortaOMalFormada() {
        assertThrows(ReglaDominioException.class, () -> new ContrasenaPlana("Pass1!")); // Menos de 8
        assertThrows(ReglaDominioException.class, () -> new ContrasenaPlana("password123!")); // Sin mayúscula
        assertThrows(ReglaDominioException.class, () -> new ContrasenaPlana("PASSWORD!")); // Sin número
        assertThrows(ReglaDominioException.class, () -> new ContrasenaPlana(null));
    }
}