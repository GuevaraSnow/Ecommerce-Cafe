package com.uniquindio.ecommerce.infrastructure.rest.mapper;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** El mapper construye los Value Objects; las reglas las aplican ellos y la excepción llega sin capturarse. */
class UsuarioMapperTest {

    private final UsuarioMapper mapper = new UsuarioMapper();

    @Test
    void unTelefonoAusenteOEnBlancoSignificaSinTelefono() {
        assertNull(mapper.toTelefono(null));
        assertNull(mapper.toTelefono(""));
        assertNull(mapper.toTelefono("   "));
    }

    @Test
    void unTelefonoValidoSeConvierte() {
        assertEquals(new Telefono("3001234567"), mapper.toTelefono("3001234567"));
    }

    @Test
    void unTelefonoInvalidoPropagaLaReglaDeDominio() {
        assertThrows(ReglaDominioException.class, () -> mapper.toTelefono("abc"));
    }

    @Test
    void convierteElCorreoYValidaSuFormato() {
        assertEquals(new Email("joseph@uniquindio.edu.co"), mapper.toEmail("joseph@uniquindio.edu.co"));
        assertThrows(ReglaDominioException.class, () -> mapper.toEmail("no-es-un-correo"));
    }

    @Test
    void laContrasenaPlanaDebeSerSeguraPeroLaDelInicioDeSesionSoloSeCompara() {
        assertEquals(new ContrasenaPlana("ClaveNueva123"), mapper.toContrasenaPlana("ClaveNueva123"));
        assertThrows(ReglaDominioException.class, () -> mapper.toContrasenaPlana("debil"));
        assertEquals(new Contrasena("debil"), mapper.toContrasena("debil"));
    }

    @Test
    void laFechaDeNacimientoSeConvierteYUnaFechaFuturaONulaPropagaLaReglaDeDominio() {
        assertEquals(new FechaDeNacimiento(LocalDate.of(2000, 1, 1)), mapper.toFechaDeNacimiento(LocalDate.of(2000, 1, 1)));
        assertThrows(ReglaDominioException.class, () -> mapper.toFechaDeNacimiento(LocalDate.now().plusDays(1)));
        assertThrows(ReglaDominioException.class, () -> mapper.toFechaDeNacimiento(null));
    }
}
