package com.spacework.controller;

import com.spacework.dao.ReporteDAO;

import java.sql.SQLException;
import java.util.Map;

public class ReporteController {

    private final ReporteDAO reporteDAO = new ReporteDAO();

    public Map<String, Double> getIngresosMensuales(int anio) throws SQLException {
        return reporteDAO.ingresosMensuales(anio);
    }

    public Map<String, Integer> getReservasPorEstado() throws SQLException {
        return reporteDAO.reservasPorEstado();
    }

    public Map<String, Integer> getOcupacionPorEspacio() throws SQLException {
        return reporteDAO.ocupacionPorEspacio();
    }
}
