package com.uniquindio.ecommerce.Domain.repository;

import com.uniquindio.ecommerce.Domain.entity.Comprador;
import com.uniquindio.ecommerce.Domain.valueobject.Email;

import java.util.Optional;

public interface CompradorRepositorio {
    boolean existePorEmail(Email email);
    Optional buscarPorEmail(Email email);
    Comprador guardar(Comprador comprador);
}