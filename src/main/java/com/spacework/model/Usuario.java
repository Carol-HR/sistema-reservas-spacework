package com.spacework.model;

import java.util.Date;

public class Usuario {

    private int    idUsuario;
    private String username;
    private String passwordHash;
    private String nombre;
    private String email;
    private int    idRol;      // ID_ROL en BD (NUMBER)
    private String estado;     // ACTIVO / INACTIVO
    private Date   fechaCreacion;
    private Date   fechaActualizacion;
    private String salt;

    public Usuario() {}

    public Usuario(int idUsuario, String username, String passwordHash,
                   String nombre, String email, int idRol, String estado) {
        this.idUsuario    = idUsuario;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.nombre       = nombre;
        this.email        = email;
        this.idRol        = idRol;
        this.estado       = estado;
    }

    public int    getIdUsuario()    { return idUsuario; }
    public String getUsername()     { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getNombre()       { return nombre; }
    public String getEmail()        { return email; }
    public int    getIdRol()        { return idRol; }
    public String getEstado()       { return estado; }
    public Date   getFechaCreacion()    { return fechaCreacion; }
    public Date   getFechaActualizacion() { return fechaActualizacion; }
    public String getSalt()         { return salt; }

    // Método de compatibilidad para obtener nombre del rol basado en ID
    public String getRol() {
        if (idRol == 1) return "ADMINISTRADOR";
        if (idRol == 2) return "OPERADOR";
        return "CLIENTE";
    }

    public void setIdUsuario(int idUsuario)       { this.idUsuario = idUsuario; }
    public void setUsername(String username)       { this.username = username; }
    public void setPasswordHash(String hash)       { this.passwordHash = hash; }
    public void setNombre(String nombre)           { this.nombre = nombre; }
    public void setEmail(String email)             { this.email = email; }
    public void setIdRol(int idRol)                { this.idRol = idRol; }
    public void setEstado(String estado)           { this.estado = estado; }
    public void setFechaCreacion(Date fecha)       { this.fechaCreacion = fecha; }
    public void setFechaActualizacion(Date fecha)  { this.fechaActualizacion = fecha; }
    public void setSalt(String salt)               { this.salt = salt; }

    @Override
    public String toString() {
        return nombre + " (" + username + ") - Rol ID: " + idRol;
    }
}
