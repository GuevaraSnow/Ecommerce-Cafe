package com.uniquindio.ecommerce.infrastructure.persistence;

import com.uniquindio.ecommerce.Domain.entity.Presentacion;
import com.uniquindio.ecommerce.Domain.repository.FiltroDePresentaciones;
import com.uniquindio.ecommerce.Domain.repository.Pagina;
import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

@Repository
public class PresentacionRepositorioMemoria implements PresentacionRepositorio {
    private final Map<String, Presentacion> presentaciones = new HashMap<>();

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

    private Predicate<Presentacion> cumple(FiltroDePresentaciones filtro) {
        return contieneTitulo(filtro.titulo())
                .and(p -> filtro.tipo() == null || p.getTipoPresentacion() == filtro.tipo())
                .and(p -> filtro.vendedorId() == null || p.getVendedorId().equals(filtro.vendedorId()))
                .and(p -> filtro.precioMinimo() == null || p.getPrecio().monto() >= filtro.precioMinimo())
                .and(p -> filtro.precioMaximo() == null || p.getPrecio().monto() <= filtro.precioMaximo());
    }

    private Predicate<Presentacion> contieneTitulo(String texto) {
        return p -> texto == null || p.getTitulo().toLowerCase().contains(texto.toLowerCase());
    }

    private Comparator<Presentacion> porTitulo() {
        return Comparator.comparing((Presentacion p) -> p.getTitulo().toLowerCase()).thenComparing(Presentacion::getId);
    }
}
