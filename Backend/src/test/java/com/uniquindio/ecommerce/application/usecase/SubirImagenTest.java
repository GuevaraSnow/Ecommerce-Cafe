package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.repository.AlmacenDeImagenes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubirImagenTest {

    @Mock
    private AlmacenDeImagenes almacen;

    @InjectMocks
    private SubirImagen subirImagen;

    @Test
    void unaImagenValidaSeGuardaYDevuelveLaUrl() {
        byte[] foto = new byte[1024];
        when(almacen.guardar(foto, "cafe.png", "image/png")).thenReturn("https://img/cafe.png");

        String url = subirImagen.ejecutar(foto, "cafe.png", "image/png");

        assertEquals("https://img/cafe.png", url);
        verify(almacen).guardar(foto, "cafe.png", "image/png");
    }

    @Test
    void unArchivoQueNoEsImagenLanzaReglaDeDominioYNoLlamaAlAlmacen() {
        assertThrows(ReglaDominioException.class,
                () -> subirImagen.ejecutar(new byte[10], "virus.exe", "application/octet-stream"));

        verifyNoInteractions(almacen);
    }

    @Test
    void unaImagenDeMasDeDosMegasLanzaReglaDeDominio() {
        byte[] enorme = new byte[2 * 1024 * 1024 + 1];

        assertThrows(ReglaDominioException.class, () -> subirImagen.ejecutar(enorme, "foto.jpg", "image/jpeg"));

        verifyNoInteractions(almacen);
    }

    @Test
    void unArchivoVacioLanzaReglaDeDominio() {
        assertThrows(ReglaDominioException.class, () -> subirImagen.ejecutar(new byte[0], "foto.jpg", "image/jpeg"));

        verifyNoInteractions(almacen);
    }
}
