package com.spacework.controller;

import com.spacework.dao.ReservaDAO;
import com.spacework.dao.HorarioBloqueadoDAO;
import com.spacework.model.Reserva;
import com.spacework.model.HorarioBloqueado;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class ValidacionController {

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final HorarioBloqueadoDAO bloqueadoDAO = new HorarioBloqueadoDAO();

    /**
     * Valida que no haya solapamiento de reservas en el mismo espacio
     */
    public boolean validarNoSolapamiento(int idEspacio, Date fechaInicio, Date fechaFin, int idReservaActual) throws SQLException {
        List<Reserva> reservas = reservaDAO.listarPorEspacio(idEspacio);
        for (Reserva r : reservas) {
            // Saltar la reserva actual (si es actualización)
            if (r.getIdReserva() == idReservaActual) continue;
            
            // Saltar reservas canceladas
            if ("CANCELADA".equals(r.getEstado())) continue;
            
            // Verificar solapamiento: si inicio < fin_otra AND fin > inicio_otra
            if (fechaInicio.before(r.getFechaFin()) && fechaFin.after(r.getFechaInicio())) {
                return false; // SOLAPAMIENTO ENCONTRADO
            }
        }
        return true; // SIN SOLAPAMIENTO
    }

    /**
     * Valida que la fecha de fin sea posterior a la de inicio
     */
    public boolean validarFechas(Date fechaInicio, Date fechaFin) {
        return fechaInicio != null && fechaFin != null && fechaFin.after(fechaInicio);
    }

    /**
     * Valida que no sea en el pasado
     */
    public boolean validarNoEnPasado(Date fecha) {
        Date ahora = new Date();
        return fecha != null && fecha.after(ahora);
    }

    /**
     * Verifica si el espacio tiene horarios bloqueados
     */
    public boolean validarHorarioNosBloqueados(int idEspacio, Date fechaInicio, Date fechaFin) throws SQLException {
        List<HorarioBloqueado> bloqueos = bloqueadoDAO.listarPorEspacio(idEspacio);
        for (HorarioBloqueado b : bloqueos) {
            if (fechaInicio.before(b.getFechaFin()) && fechaFin.after(b.getFechaInicio())) {
                return false; // Horario bloqueado
            }
        }
        return true;
    }
}
