package com.spacework.model;

import java.util.Date;

public class AuditoriaLog {
    private int idAuditoria;
    private String tabla;
    private int idRegistro;
    private String tipoCambio; // INSERT, UPDATE, DELETE
    private String usuario;
    private Date fechaHora;
    private String cambios;

    public AuditoriaLog(String tabla, int idRegistro, String tipoCambio, String usuario, String cambios) {
        this.tabla = tabla;
        this.idRegistro = idRegistro;
        this.tipoCambio = tipoCambio;
        this.usuario = usuario;
        this.cambios = cambios;
        this.fechaHora = new Date();
    }

    public AuditoriaLog(int idAuditoria, String tabla, int idRegistro, String tipoCambio,
                       String usuario, Date fechaHora, String cambios) {
        this.idAuditoria = idAuditoria;
        this.tabla = tabla;
        this.idRegistro = idRegistro;
        this.tipoCambio = tipoCambio;
        this.usuario = usuario;
        this.fechaHora = fechaHora;
        this.cambios = cambios;
    }

    public int getIdAuditoria() { return idAuditoria; }
    public void setIdAuditoria(int idAuditoria) { this.idAuditoria = idAuditoria; }

    public String getTabla() { return tabla; }
    public void setTabla(String tabla) { this.tabla = tabla; }

    public int getIdRegistro() { return idRegistro; }
    public void setIdRegistro(int idRegistro) { this.idRegistro = idRegistro; }

    public String getTipoCambio() { return tipoCambio; }
    public void setTipoCambio(String tipoCambio) { this.tipoCambio = tipoCambio; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public Date getFechaHora() { return fechaHora; }
    public void setFechaHora(Date fechaHora) { this.fechaHora = fechaHora; }

    public String getCambios() { return cambios; }
    public void setCambios(String cambios) { this.cambios = cambios; }
}
