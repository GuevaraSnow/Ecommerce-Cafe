package com.uniquindio.ecommerce.Domain.entity;

import com.uniquindio.ecommerce.Domain.valueobject.Email;
import com.uniquindio.ecommerce.Domain.valueobject.Contrasena;
import com.uniquindio.ecommerce.Domain.valueobject.Telefono;
import com.uniquindio.ecommerce.Domain.valueobject.FechaDeNacimiento;
import com.uniquindio.ecommerce.Domain.valueobject.TipoDeComprador;

public class Comprador {
    private String id;
    private String nombre;
    private Email email;
    private Contrasena contrasena;
    private Telefono telefono;
    private FechaDeNacimiento fechaNacimiento;
    private TipoDeComprador tipo;
    private boolean eliminadoLogicamente;

    public Comprador(String id, String nombre, Email email, Contrasena contrasena,
                     Telefono telefono, FechaDeNacimiento fechaNacimiento, TipoDeComprador tipo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.contrasena = contrasena;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.tipo = tipo;
        this.eliminadoLogicamente = false;
    }

    public void eliminar() {
        this.eliminadoLogicamente = true;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Email getEmail() {
        return email;
    }

    public Contrasena getContraseña() {
        return contrasena;
    }

    public Telefono getTelefono() {
        return telefono;
    }

    public FechaDeNacimiento getFechaNacimiento() {
        return fechaNacimiento;
    }

    public TipoDeComprador getTipo() {
        return tipo;
    }

    public boolean isEliminadoLogicamente() {
        return eliminadoLogicamente;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Comprador comprador = (Comprador) o;
        return id != null && id.equals(comprador.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
