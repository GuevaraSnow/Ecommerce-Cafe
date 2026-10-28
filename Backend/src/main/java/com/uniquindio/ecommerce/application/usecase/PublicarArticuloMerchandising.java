package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.ArticuloDeMerchandising;
import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: PublicarArticuloMerchandising.
 * (Actor: Vendedor de Derivados) — publica un Artículo de Merchandising no
 * alimenticio, sin Origen ni Ficha de Origen (regla 7-B).
 */
@Service
@RequiredArgsConstructor
public class PublicarArticuloMerchandising {

    private final PresentacionRepositorio repositorio;

    @Transactional
    public ArticuloDeMerchandising ejecutar(String id, String vendedorId, String titulo, Precio precio,
                                             Cantidad cantidadDisponible, Galeria galeria, String material,
                                             String descripcion, Rol rolVendedor) {
        Presentacion presentacion = Presentacion.publicar(id, vendedorId, null, null, titulo,
                TipoDePresentacion.MERCHANDISING, precio, cantidadDisponible, galeria, null, rolVendedor, null,
                material, descripcion);
        repositorio.guardar(presentacion);
        return (ArticuloDeMerchandising) presentacion;
    }
}
