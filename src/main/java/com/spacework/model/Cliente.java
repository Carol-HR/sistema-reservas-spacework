package com.spacework.model;

public class Cliente {

    private int    idCliente;
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String telefono;
    private String estado;   // ACTIVO / INACTIVO

    public Cliente() {}

    public Cliente(int idCliente, String nombre, String apellido,
                   String dni, String email, String telefono, String estado) {
        this.idCliente = idCliente;
        this.nombre    = nombre;
        this.apellido  = apellido;
        this.dni       = dni;
        this.email     = email;
        this.telefono  = telefono;
        this.estado    = estado;
    }

    public int    getIdCliente() { return idCliente; }
    public String getNombre()    { return nombre; }
    public String getApellido()  { return apellido; }
    public String getDni()       { return dni; }
    public String getEmail()     { return email; }
    public String getTelefono()  { return telefono; }
    public String getEstado()    { return estado; }

    public void setIdCliente(int id)          { this.idCliente = id; }
    public void setNombre(String nombre)       { this.nombre = nombre; }
    public void setApellido(String apellido)   { this.apellido = apellido; }
    public void setDni(String dni)             { this.dni = dni; }
    public void setEmail(String email)         { this.email = email; }
    public void setTelefono(String telefono)   { this.telefono = telefono; }
    public void setEstado(String estado)       { this.estado = estado; }

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    @Override
    public String toString() {
        return getNombreCompleto() + " - DNI: " + dni;
    }
}
