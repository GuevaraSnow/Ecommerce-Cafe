package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.AsistenteIA;
import com.uniquindio.ecommerce.Domain.repository.FiltroDePresentaciones;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.valueobject.MensajeDeChat;
import com.uniquindio.ecommerce.Domain.valueobject.Precio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Caso de uso: ConsultarAsistente.
 * (Actor: cualquier visitante) — responde una pregunta sobre el catálogo con ayuda de la IA;
 * no modifica nada.
 */
@Service
@RequiredArgsConstructor
public class ConsultarAsistente {

    private static final int MAX_PRESENTACIONES = 30;
    private static final int MAX_HISTORIAL = 6;

    private final PresentacionRepositorio presentaciones;
    private final AsistenteIA asistente;

    @Transactional(readOnly = true)
    public String ejecutar(String mensaje, List<MensajeDeChat> historial) {
        // El mensaje nuevo siempre es del usuario; solo se crea para validarlo
        new MensajeDeChat("usuario", mensaje);

        List<Presentacion> catalogo = presentaciones
                .buscarDisponibles(new FiltroDePresentaciones(null, null, null, null, null), 0, MAX_PRESENTACIONES)
                .contenido();

        return asistente.responder(instrucciones(catalogo), recortar(historial), mensaje);
    }

    // Deja solo los últimos mensajes de la conversación; un historial nulo cuenta como vacío
    private List<MensajeDeChat> recortar(List<MensajeDeChat> historial) {
        if (historial == null) {
            return List.of();
        }
        return historial.subList(Math.max(0, historial.size() - MAX_HISTORIAL), historial.size());
    }

    private String instrucciones(List<Presentacion> catalogo) {
        String lineas = catalogo.stream()
                .map(this::describir)
                .collect(Collectors.joining("\n"));

        return """
                Eres el asesor de ventas de una tienda en línea de café colombiano de origen.
                Tu única tarea es recomendar y asesorar sobre los productos del catálogo de abajo.
                Reglas:
                - Responde siempre en español, de forma amable y en pocas frases.
                - Recomienda solo productos del catálogo; no inventes productos, precios ni datos.
                - Si la pregunta no tiene que ver con el café o la tienda, di que solo puedes ayudar con el café.
                - Si ningún producto sirve para lo que piden, dilo con honestidad.

                Catálogo:
                """ + lineas;
    }

    private String describir(Presentacion presentacion) {
        Precio precio = presentacion.precioVigente(LocalDate.now());
        return "- %s | %s | %.0f %s | disponible: %.0f %s".formatted(
                presentacion.getTitulo(),
                presentacion.getTipoPresentacion(),
                precio.monto(), precio.moneda(),
                presentacion.getCantidadDisponible().valor(), presentacion.getCantidadDisponible().unidad());
    }
}
