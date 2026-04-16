package com.spacework.model;

import java.util.Date;

public class Usuario {

    private int    idUsuario;
    private String username;
    private String passwordHash;
    private String nombre;
    private String email;
    private String rol;      // ADMINISTRADOR / OPERADOR
    private String estado;   // ACTIVO / INACTIVO
    private Date   fechaRegistro;

    public Usuario() {}

    public Usuario(int idUsuario, String username, String passwordHash,
                   String nombre, String email, String rol, String estado) {
        this.idUsuario    = idUsuario;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.nombre       = nombre;
        this.email        = email;
        this.rol          = rol;
        this.estado       = estado;
    }

    public int    getIdUsuario()    { return idUsuario; }
    public String getUsername()     { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getNombre()       { return nombre; }
    public String getEmail()        { return email; }
    public String getRol()          { return rol; }
    public String getEstado()       { return estado; }
    public Date   getFechaRegistro(){ return fechaRegistro; }

    public void setIdUsuario(int idUsuario)       { this.idUsuario = idUsuario; }
    public void setUsername(String username)       { this.username = username; }
    public void setPasswordHash(String hash)       { this.passwordHash = hash; }
    public void setNombre(String nombre)           { this.nombre = nombre; }
    public void setEmail(String email)             { this.email = email; }
    public void setRol(String rol)                 { this.rol = rol; }
    public void setEstado(String estado)           { this.estado = estado; }
    public void setFechaRegistro(Date fecha)       { this.fechaRegistro = fecha; }

    @Override
    public String toString() {
        return nombre + " (" + username + ") - " + rol;
    }
}
