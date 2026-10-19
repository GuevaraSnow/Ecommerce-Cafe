package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.service.ServicioDePublicacion;
import com.uniquindio.ecommerce.Domain.valueobject.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Caso de uso: PublicarPresentacion.
 * (Actor: Caficultor, Tostador o Vendedor de Derivados) — publica una
 * Presentación Trazable. No tiene lógica: delega en el Domain Service
 * {@link ServicioDePublicacion}, que valida y descuenta del Origen, y guarda
 * la Presentación resultante.
 */
@Service
@RequiredArgsConstructor
public class PublicarPresentacion {

    private final PresentacionRepositorio presentacionRepositorio;
    private final ServicioDePublicacion servicioDePublicacion;

    @Transactional
    public Presentacion ejecutar(String id, String vendedorId, String loteId, String transformacionId,
                                  String titulo, TipoDePresentacion tipoPresentacion, Precio precio,
                                  Cantidad cantidadDisponible, Galeria galeria, PerfilTueste perfilDeTueste,
                                  RolVendedor rolVendedor, FechaDeTueste fechaTueste) {
        Presentacion presentacion = servicioDePublicacion.publicar(id, vendedorId, loteId, transformacionId,
                titulo, tipoPresentacion, precio, cantidadDisponible, galeria, perfilDeTueste, rolVendedor,
                fechaTueste);
        presentacionRepositorio.guardar(presentacion);
        return presentacion;
    }
}
