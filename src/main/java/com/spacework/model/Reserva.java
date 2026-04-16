package com.spacework.model;

import java.util.Date;

public class Reserva {

    private int     idReserva;
    private Cliente cliente;
    private Espacio espacio;
    private Date    fechaInicio;
    private Date    fechaFin;
    private double  montoTotal;
    private String  estado;      // PENDIENTE / CONFIRMADA / COMPLETADA / CANCELADA
    private Date    fechaCreacion;

    public Reserva() {}

    public Reserva(int idReserva, Cliente cliente, Espacio espacio,
                   Date fechaInicio, Date fechaFin, double montoTotal, String estado) {
        this.idReserva   = idReserva;
        this.cliente     = cliente;
        this.espacio     = espacio;
        this.fechaInicio = fechaInicio;
        this.fechaFin    = fechaFin;
        this.montoTotal  = montoTotal;
        this.estado      = estado;
    }

    public int     getIdReserva()    { return idReserva; }
    public Cliente getCliente()      { return cliente; }
    public Espacio getEspacio()      { return espacio; }
    public Date    getFechaInicio()  { return fechaInicio; }
    public Date    getFechaFin()     { return fechaFin; }
    public double  getMontoTotal()   { return montoTotal; }
    public String  getEstado()       { return estado; }
    public Date    getFechaCreacion(){ return fechaCreacion; }

    public void setIdReserva(int id)          { this.idReserva = id; }
    public void setCliente(Cliente cliente)    { this.cliente = cliente; }
    public void setEspacio(Espacio espacio)    { this.espacio = espacio; }
    public void setFechaInicio(Date fecha)     { this.fechaInicio = fecha; }
    public void setFechaFin(Date fecha)        { this.fechaFin = fecha; }
    public void setMontoTotal(double monto)    { this.montoTotal = monto; }
    public void setEstado(String estado)       { this.estado = estado; }
    public void setFechaCreacion(Date fecha)   { this.fechaCreacion = fecha; }

    @Override
    public String toString() {
        return "Reserva #" + idReserva + " - " + cliente.getNombreCompleto()
               + " | " + espacio.getNombre() + " | " + estado;
    }
}
