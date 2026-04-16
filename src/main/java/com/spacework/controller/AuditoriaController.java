package com.spacework.controller;

import com.spacework.dao.AuditoriaDAO;
import com.spacework.model.AuditoriaLog;

import java.sql.SQLException;
import java.util.Date;

public class AuditoriaController {

    private final AuditoriaDAO auditoriaDAO = new AuditoriaDAO();

    public void registrarCambio(String tabla, int idRegistro, String tipoCambio, String usuario, String detalles) throws SQLException {
        AuditoriaLog log = new AuditoriaLog(tabla, idRegistro, tipoCambio, usuario, detalles);
        auditoriaDAO.registrar(log);
    }

    public void registrarCambioReserva(int idReserva, String usuario, String nuevoEstado, String anterior) throws SQLException {
        String detalles = "Estado: " + anterior + " → " + nuevoEstado;
        registrarCambio("RESERVAS", idReserva, "UPDATE", usuario, detalles);
    }
}
