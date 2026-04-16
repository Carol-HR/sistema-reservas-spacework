package com.spacework.controller;

import com.spacework.model.Evaluacion;
import com.spacework.dao.EvaluacionDAO;
import java.util.List;

/**
 * Controlador para gestión de Evaluaciones
 * Maneja la lógica de negocio relacionada con calificaciones y comentarios
 */
public class EvaluacionController {
    
    private EvaluacionDAO evaluacionDAO;

    public EvaluacionController() {
        this.evaluacionDAO = new EvaluacionDAO();
    }

    /**
     * Registra una nueva evaluación (vía token de email)
     * El cliente es el que evalúa, no un usuario del sistema
     */
    public boolean registrarEvaluacion(int idReserva, int idCliente, int calificacion, String comentario) {
        if (calificacion < 1 || calificacion > 5) {
            System.out.println("Error: Calificación debe estar entre 1 y 5");
            return false;
        }
        
        if (idCliente <= 0) {
            System.out.println("Error: ID de cliente inválido");
            return false;
        }
        
        Evaluacion eval = new Evaluacion();
        eval.setIdReserva(idReserva);
        eval.setIdCliente(idCliente);
        eval.setCalificacion(calificacion);
        eval.setComentario(comentario);
        
        return evaluacionDAO.insertar(eval);
    }

    /**
     * Obtiene todas las evaluaciones
     */
    public List<Evaluacion> obtenerTodas() {
        return evaluacionDAO.listar();
    }

    /**
     * Obtiene evaluaciones de una reserva
     */
    public List<Evaluacion> obtenerPorReserva(int idReserva) {
        return evaluacionDAO.listarPorReserva(idReserva);
    }

    /**
     * Busca una evaluación por ID
     */
    public Evaluacion obtenerPorId(int idEvaluacion) {
        return evaluacionDAO.buscarPorId(idEvaluacion);
    }

    /**
     * Obtiene la calificación promedio de un espacio
     */
    public double obtenerPromedioEspacio(int idEspacio) {
        return evaluacionDAO.promedioCalificacion(idEspacio);
    }

    /**
     * Obtiene el conteo de evaluaciones de un espacio
     */
    public int obtenerConteoEspacio(int idEspacio) {
        return evaluacionDAO.contarEvaluacionesPorEspacio(idEspacio);
    }

    /**
     * Actualiza una evaluación
     */
    public boolean actualizarEvaluacion(int idEvaluacion, int calificacion, String comentario) {
        Evaluacion eval = evaluacionDAO.buscarPorId(idEvaluacion);
        if (eval != null) {
            eval.setCalificacion(calificacion);
            eval.setComentario(comentario);
            return evaluacionDAO.actualizar(eval);
        }
        return false;
    }

    /**
     * Elimina una evaluación
     */
    public boolean eliminarEvaluacion(int idEvaluacion) {
        return evaluacionDAO.eliminar(idEvaluacion);
    }

    /**
     * Obtiene representación en estrellas
     */
    public String obtenerEstrellas(int calificacion) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < calificacion; i++) {
            sb.append("★");
        }
        for (int i = calificacion; i < 5; i++) {
            sb.append("☆");
        }
        return sb.toString();
    }
}
