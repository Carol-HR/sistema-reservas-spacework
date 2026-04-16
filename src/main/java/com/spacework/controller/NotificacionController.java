package com.spacework.controller;

import com.spacework.model.Notificacion;
import com.spacework.dao.NotificacionDAO;
import java.util.List;

/**
 * Controlador para gestión de Notificaciones
 * Maneja la lógica de negocio relacionada con notificaciones a usuarios
 */
public class NotificacionController {
    
    private NotificacionDAO notificacionDAO;

    public NotificacionController() {
        this.notificacionDAO = new NotificacionDAO();
    }

    /**
     * Registra una nueva notificación
     */
    public boolean registrarNotificacion(int idUsuario, String tipo, String asunto, String mensaje) {
        if (!validarTipo(tipo)) {
            System.out.println("Error: Tipo de notificación inválido");
            return false;
        }
        
        Notificacion notif = new Notificacion();
        notif.setIdUsuario(idUsuario);
        notif.setTipo(tipo);
        notif.setAsunto(asunto);
        notif.setMensaje(mensaje);
        notif.setLeida(false);
        
        return notificacionDAO.insertar(notif);
    }

    /**
     * Obtiene todas las notificaciones
     */
    public List<Notificacion> obtenerTodas() {
        return notificacionDAO.listar();
    }

    /**
     * Obtiene notificaciones de un usuario
     */
    public List<Notificacion> obtenerPorUsuario(int idUsuario) {
        return notificacionDAO.listarPorUsuario(idUsuario);
    }

    /**
     * Obtiene notificaciones sin leer de un usuario
     */
    public List<Notificacion> obtenerSinLeer(int idUsuario) {
        return notificacionDAO.listarSinLeer(idUsuario);
    }

    /**
     * Busca una notificación por ID
     */
    public Notificacion obtenerPorId(int idNotificacion) {
        return notificacionDAO.buscarPorId(idNotificacion);
    }

    /**
     * Marca una notificación como leída
     */
    public boolean marcarComoLeida(int idNotificacion) {
        return notificacionDAO.marcarComoLeida(idNotificacion);
    }

    /**
     * Marca todas las notificaciones de un usuario como leídas
     */
    public boolean marcarTodoComoLeido(int idUsuario) {
        return notificacionDAO.marcarTodoComoLeido(idUsuario);
    }

    /**
     * Obtiene el conteo de notificaciones sin leer
     */
    public int obtenerConteoSinLeer(int idUsuario) {
        return notificacionDAO.contarSinLeer(idUsuario);
    }

    /**
     * Elimina una notificación
     */
    public boolean eliminarNotificacion(int idNotificacion) {
        return notificacionDAO.eliminar(idNotificacion);
    }

    /**
     * Elimina todas las notificaciones de un usuario
     */
    public boolean eliminarTodas(int idUsuario) {
        return notificacionDAO.eliminarTodas(idUsuario);
    }

    /**
     * Envía notificación de nueva reserva
     */
    public boolean enviarNotificacionReserva(int idUsuario, String espacioNombre) {
        return registrarNotificacion(idUsuario, "RESERVA", "Nueva Reserva",
                "Su reserva para " + espacioNombre + " ha sido registrada.");
    }

    /**
     * Envía notificación de pago
     */
    public boolean enviarNotificacionPago(int idUsuario, String estado) {
        return registrarNotificacion(idUsuario, "PAGO", "Estado de Pago",
                "Su pago ha sido " + estado.toLowerCase() + ".");
    }

    /**
     * Envía notificación de promoción
     */
    public boolean enviarNotificacionPromocion(int idUsuario, String codigoPromo, double porcentaje) {
        return registrarNotificacion(idUsuario, "PROMOCION", "¡Nueva Promoción!",
                "Código: " + codigoPromo + " - Descuento: " + porcentaje + "%");
    }

    /**
     * Valida el tipo de notificación
     */
    private boolean validarTipo(String tipo) {
        return tipo.equals("RESERVA") || tipo.equals("PAGO") || tipo.equals("RECORDATORIO") ||
               tipo.equals("PROMOCION") || tipo.equals("SISTEMA");
    }
}
