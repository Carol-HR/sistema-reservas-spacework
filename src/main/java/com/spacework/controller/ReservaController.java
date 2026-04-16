package com.spacework.controller;

import com.spacework.dao.ReservaDAO;
import com.spacework.model.Cliente;
import com.spacework.model.Espacio;
import com.spacework.model.Reserva;
import com.spacework.controller.ValidacionController;
import com.spacework.controller.AuditoriaController;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ReservaController {

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final ValidacionController validacionCtrl = new ValidacionController();
    private final AuditoriaController auditoriaCtrl = new AuditoriaController();

    public List<Reserva> listarTodas() throws SQLException {
        return reservaDAO.listarTodas();
    }

    public List<Reserva> listarPorCliente(int idCliente) throws SQLException {
        return reservaDAO.listarPorCliente(idCliente);
    }

    /**
     * Calcula el monto total de la reserva según las horas y el precio del espacio.
     */
    public double calcularMonto(Date fechaInicio, Date fechaFin, double precioPorHora) {
        long diferenciaMs = fechaFin.getTime() - fechaInicio.getTime();
        double horas = diferenciaMs / (double) TimeUnit.HOURS.toMillis(1);
        return Math.round(horas * precioPorHora * 100.0) / 100.0;
    }

    public void crearReserva(Cliente cliente, Espacio espacio,
                              Date fechaInicio, Date fechaFin) throws SQLException {
        if (fechaInicio == null || fechaFin == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias.");
        }
        if (!validacionCtrl.validarFechas(fechaInicio, fechaFin)) {
            throw new IllegalArgumentException("La fecha de fin debe ser posterior a la de inicio.");
        }
        if (!validacionCtrl.validarHorarioNosBloqueados(espacio.getIdEspacio(), fechaInicio, fechaFin)) {
            throw new IllegalArgumentException("El espacio tiene horarios bloqueados en ese rango.");
        }
        if (!validacionCtrl.validarNoSolapamiento(espacio.getIdEspacio(), fechaInicio, fechaFin, -1)) {
            throw new IllegalStateException("El espacio ya está reservado en ese horario. Selecciona otro rango.");
        }
        double monto = calcularMonto(fechaInicio, fechaFin, espacio.getPrecioPorHora());
        Reserva r = new Reserva();
        r.setCliente(cliente);
        r.setEspacio(espacio);
        r.setFechaInicio(fechaInicio);
        r.setFechaFin(fechaFin);
        r.setMontoTotal(monto);
        reservaDAO.insertar(r);
        auditoriaCtrl.registrarCambio("RESERVAS", r.getIdReserva(), "INSERT", "admin", "Nueva reserva creada");
    }

    public void confirmarReserva(int idReserva) throws SQLException {
        reservaDAO.cambiarEstado(idReserva, "CONFIRMADA");
    }

    public void completarReserva(int idReserva) throws SQLException {
        reservaDAO.cambiarEstado(idReserva, "COMPLETADA");
    }

    public void cancelarReserva(int idReserva) throws SQLException {
        reservaDAO.cambiarEstado(idReserva, "CANCELADA");
    }
}
