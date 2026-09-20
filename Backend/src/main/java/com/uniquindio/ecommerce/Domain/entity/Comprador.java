package main.java.com.uniquindio.ecommerce.Domain.entity;

public class Comprador {
    private String id;
    private String nombre;
    private Email email;
    private Contraseña contraseña;
    private Telefono telefono;
    private FechaDeNacimiento fechaNacimiento;
    private TipoComprador tipo;
    private boolean eliminadoLogicamente;

    public Comprador(String id, String nombre, Email email, Contrasena contrasena,
                     Telefono telefono, FechaDeNacimiento fechaNacimiento, TipoDeComprador tipo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.contraseña = contraseña;
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

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Contraseña getContraseña() {
        return contraseña;
    }

    public void setContraseña(Contraseña contraseña) {
        this.contraseña = contraseña;


    public Telefono getTelefono() {
        return telefono;
    }

    public void setTelefono(Telefono telefono) {
        this.telefono = telefono;
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
