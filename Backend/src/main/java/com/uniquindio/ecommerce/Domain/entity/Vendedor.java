package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.exception.ReglaDominioException;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.Email;
import com.uniquindio.ecommerce.Domain.valueobject.RolVendedor;
import com.uniquindio.ecommerce.Domain.valueobject.Telefono;

import java.util.Objects;
import java.util.UUID;

public class Vendedor {

    private final String id;
    private final String nombre;
    private final Email email;
    private final Contrasena contrasena;
    private final Telefono telefono;
    private final RolVendedor rol;
    private boolean eliminadoLogicamente;

    private Vendedor(String nombre, Email email, Contrasena contrasena, Telefono telefono, RolVendedor rol) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
        this.telefono = telefono;
        this.rol = rol;
        this.eliminadoLogicamente = false;
    }

    public static Vendedor registrar(String nombre, Email email, Contrasena contrasena, Telefono telefono, RolVendedor rol) {
        if (nombre == null || nombre.isBlank()) {
            throw new ReglaDominioException("El vendedor debe tener un nombre");
        }
        if (email == null) {
            throw new ReglaDominioException("El vendedor debe tener un email");
        }
        if (contrasena == null) {
            throw new ReglaDominioException("El vendedor debe tener una contrasena");
        }
        if (telefono == null) {
            throw new ReglaDominioException("El vendedor debe tener un telefono");
        }
        if (rol == null) {
            throw new ReglaDominioException("El vendedor debe tener un rol");
        }
        return new Vendedor(nombre, email, contrasena, telefono, rol);
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public Email getEmail() { return email; }
    public Contrasena getContrasena() { return contrasena; }
    public Telefono getTelefono() { return telefono; }
    public RolVendedor getRol() { return rol; }
    public boolean isEliminadoLogicamente() { return eliminadoLogicamente; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vendedor)) return false;
        Vendedor vendedor = (Vendedor) o;
        return id.equals(vendedor.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}