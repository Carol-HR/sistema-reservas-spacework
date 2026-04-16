package com.spacework.controller;

import com.spacework.model.Horario;
import com.spacework.dao.HorarioDAO;
import java.util.List;

/**
 * Controlador para gestión de Horarios
 * Maneja la lógica de negocio relacionada con horarios operacionales
 */
public class HorarioController {
    
    private HorarioDAO horarioDAO;

    public HorarioController() {
        this.horarioDAO = new HorarioDAO();
    }

    /**
     * Registra un nuevo horario
     */
    public boolean registrarHorario(int idEspacio, int diaSemana, String horaApertura, String horaCierre) {
        if (diaSemana < 0 || diaSemana > 6) {
            System.out.println("Error: Día de semana inválido");
            return false;
        }
        
        if (!validarFormato(horaApertura) || !validarFormato(horaCierre)) {
            System.out.println("Error: Formato de hora inválido (HH:MM)");
            return false;
        }
        
        Horario horario = new Horario();
        horario.setIdEspacio(idEspacio);
        horario.setDiaSemana(diaSemana);
        horario.setHoraApertura(horaApertura);
        horario.setHoraCierre(horaCierre);
        horario.setEstado("ACTIVO");
        
        return horarioDAO.insertar(horario);
    }

    /**
     * Obtiene todos los horarios
     */
    public List<Horario> obtenerTodos() {
        return horarioDAO.listar();
    }

    /**
     * Obtiene horarios de un espacio
     */
    public List<Horario> obtenerPorEspacio(int idEspacio) {
        return horarioDAO.listarPorEspacio(idEspacio);
    }

    /**
     * Obtiene el horario para un día específico
     */
    public Horario obtenerPorDia(int idEspacio, int diaSemana) {
        return horarioDAO.obtenerPorDia(idEspacio, diaSemana);
    }

    /**
     * Busca un horario por ID
     */
    public Horario obtenerPorId(int idHorario) {
        return horarioDAO.buscarPorId(idHorario);
    }

    /**
     * Actualiza un horario
     */
    public boolean actualizarHorario(int idHorario, String horaApertura, String horaCierre) {
        Horario horario = horarioDAO.buscarPorId(idHorario);
        if (horario != null) {
            horario.setHoraApertura(horaApertura);
            horario.setHoraCierre(horaCierre);
            return horarioDAO.actualizar(horario);
        }
        return false;
    }

    /**
     * Desactiva un horario
     */
    public boolean desactivarHorario(int idHorario) {
        return horarioDAO.desactivar(idHorario);
    }

    /**
     * Valida formato de hora HH:MM
     */
    private boolean validarFormato(String hora) {
        if (hora == null || hora.length() != 5) return false;
        String[] partes = hora.split(":");
        if (partes.length != 2) return false;
        try {
            int h = Integer.parseInt(partes[0]);
            int m = Integer.parseInt(partes[1]);
            return h >= 0 && h <= 23 && m >= 0 && m <= 59;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
