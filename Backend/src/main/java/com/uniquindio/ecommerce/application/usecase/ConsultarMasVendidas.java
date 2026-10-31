package com.uniquindio.ecommerce.application.usecase;

import com.uniquindio.ecommerce.Domain.repository.PresentacionRepositorio;
import com.uniquindio.ecommerce.Domain.repository.PresentacionVendida;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Caso de uso: ConsultarMasVendidas.
 * (Actor: cualquier visitante) — el ranking de las Presentaciones más vendidas, por número de Compras
 * (y por ingresos si empatan); no modifica nada.
 */
@Service
@RequiredArgsConstructor
public class ConsultarMasVendidas {

    private final PresentacionRepositorio repositorio;

    @Transactional(readOnly = true)
    public List<PresentacionVendida> ejecutar(int limite) {
        return repositorio.masVendidas(limite);
    }
}
