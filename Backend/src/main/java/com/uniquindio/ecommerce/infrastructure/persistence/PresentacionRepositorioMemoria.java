package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.FiltroDePresentaciones;
import com.uniquindio.ecommerce.Domain.repository.Pagina;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionVendida;
import com.uniquindio.ecommerce.Domain.repository.ResenaRepositorio;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Predicate;

/**
 * Versión en memoria del puerto; ya no es un bean: el adaptador activo es PresentacionRepositorioJpa. Se
 * conserva para las pruebas. Para filtrar por calificación mínima necesita el repositorio de Reseñas
 * (en la base de datos el promedio sale de una subconsulta); sin él, ese filtro no se puede aplicar.
 */
public class PresentacionRepositorioMemoria implements PresentacionRepositorio {
    private final Map<String, Presentacion> presentaciones = new HashMap<>();
    private final ResenaRepositorio resenaRepositorio;

    public PresentacionRepositorioMemoria() {
        this(null);
    }

    public PresentacionRepositorioMemoria(ResenaRepositorio resenaRepositorio) {
        this.resenaRepositorio = resenaRepositorio;
    }

    @Override
    public Optional<Presentacion> buscarPorId(String id) {
        return Optional.ofNullable(presentaciones.get(id));
    }

    @Override
    public void guardar(Presentacion presentacion) {
        presentaciones.put(presentacion.getId(), presentacion);
    }

    @Override
    public Pagina<Presentacion> buscarDisponibles(FiltroDePresentaciones filtro, int pagina, int tamano) {
        List<Presentacion> coincidencias = presentaciones.values().stream()
                .filter(Presentacion::estaDisponibleParaVenta)
                .filter(cumple(filtro))
                .sorted(porTitulo())
                .toList();
        List<Presentacion> contenido = coincidencias.stream()
                .skip((long) pagina * tamano)
                .limit(tamano)
                .toList();
        return new Pagina<>(contenido, pagina, tamano, coincidencias.size());
    }

    @Override
    public List<Presentacion> sugerirDisponiblesPorTitulo(String texto, int limite) {
        return presentaciones.values().stream()
                .filter(Presentacion::estaDisponibleParaVenta)
                .filter(contieneTitulo(texto))
                .sorted(porTitulo())
                .limit(limite)
                .toList();
    }

    /** El ranking sale de las Compras, que este repositorio no tiene: se calcula en el adaptador JPA. */
    @Override
    public List<PresentacionVendida> masVendidas(int limite) {
        throw new UnsupportedOperationException("El ranking de ventas necesita las Compras: usa PresentacionRepositorioJpa");
    }

    private Predicate<Presentacion> cumple(FiltroDePresentaciones filtro) {
        return contieneTitulo(filtro.titulo())
                .and(p -> filtro.tipo() == null || p.getTipoPresentacion() == filtro.tipo())
                .and(p -> filtro.vendedorId() == null || p.getVendedorId().equals(filtro.vendedorId()))
                .and(p -> filtro.precioMinimo() == null || p.getPrecio().monto() >= filtro.precioMinimo())
                .and(p -> filtro.precioMaximo() == null || p.getPrecio().monto() <= filtro.precioMaximo())
                .and(p -> filtro.calificacionMinima() == null
                        || promedioDeResenas(p.getId()).orElse(0) >= filtro.calificacionMinima());
    }

    /** El promedio de las Reseñas de la Presentación; vacío si no tiene ninguna. */
    private OptionalDouble promedioDeResenas(String presentacionId) {
        if (resenaRepositorio == null) {
            throw new IllegalStateException("Filtrar por calificación mínima necesita un ResenaRepositorio");
        }
        return resenaRepositorio.buscarPorPresentacion(presentacionId).stream()
                .mapToInt(resena -> resena.getCalificacion().valor())
                .average();
    }

    private Predicate<Presentacion> contieneTitulo(String texto) {
        return p -> texto == null || p.getTitulo().toLowerCase().contains(texto.toLowerCase());
    }

    private Comparator<Presentacion> porTitulo() {
        return Comparator.comparing((Presentacion p) -> p.getTitulo().toLowerCase()).thenComparing(Presentacion::getId);
    }
}
