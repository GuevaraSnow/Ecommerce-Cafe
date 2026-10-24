package com.uniquindio.ecommerce.Domain.repository;

public interface EncriptadorContrasenaRepositorio {
    String encriptar(String contrasenaPlana);
    boolean coinciden(String contrasenaPlana, String contrasenaEncriptada);
}