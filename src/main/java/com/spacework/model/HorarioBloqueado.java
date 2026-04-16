package com.spacework.model;

import java.util.Date;

public class HorarioBloqueado {
    private int idBloqueo;
    private int idEspacio;
    private Date fechaInicio;
    private Date fechaFin;
    private String razon;
    private Date fechaCreacion;
    private String usuarioCreador;

    public HorarioBloqueado(int idEspacio, Date fechaInicio, Date fechaFin, String razon, String usuarioCreador) {
        this.idEspacio = idEspacio;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.razon = razon;
        this.usuarioCreador = usuarioCreador;
    }

    public HorarioBloqueado(int idBloqueo, int idEspacio, Date fechaInicio, Date fechaFin, 
                           String razon, Date fechaCreacion, String usuarioCreador) {
        this.idBloqueo = idBloqueo;
        this.idEspacio = idEspacio;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.razon = razon;
        this.fechaCreacion = fechaCreacion;
        this.usuarioCreador = usuarioCreador;
    }

    public int getIdBloqueo() { return idBloqueo; }
    public void setIdBloqueo(int idBloqueo) { this.idBloqueo = idBloqueo; }

    public int getIdEspacio() { return idEspacio; }
    public void setIdEspacio(int idEspacio) { this.idEspacio = idEspacio; }

    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    public Date getFechaFin() { return fechaFin; }
    public void setFechaFin(Date fechaFin) { this.fechaFin = fechaFin; }

    public String getRazon() { return razon; }
    public void setRazon(String razon) { this.razon = razon; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getUsuarioCreador() { return usuarioCreador; }
    public void setUsuarioCreador(String usuarioCreador) { this.usuarioCreador = usuarioCreador; }
}
