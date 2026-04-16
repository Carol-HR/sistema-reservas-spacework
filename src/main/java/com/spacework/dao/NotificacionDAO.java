package com.spacework.dao;

import com.spacework.model.Notificacion;
import com.spacework.util.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla NOTIFICACIONES
 * Gestiona notificaciones enviadas a usuarios
 */
public class NotificacionDAO {

    /**
     * Inserta una nueva notificación
     */
    public boolean insertar(Notificacion notificacion) {
        String sql = "INSERT INTO NOTIFICACIONES (id_notificacion, id_usuario, tipo, asunto, mensaje, leida, fecha_creacion) " +
                     "VALUES (SEQ_NOTIFICACIONES.NEXTVAL, ?, ?, ?, ?, ?, SYSDATE)";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, notificacion.getIdUsuario());
            pstmt.setString(2, notificacion.getTipo());
            pstmt.setString(3, notificacion.getAsunto());
            pstmt.setString(4, notificacion.getMensaje());
            pstmt.setInt(5, notificacion.isLeida() ? 1 : 0);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Busca una notificación por ID
     */
    public Notificacion buscarPorId(int idNotificacion) {
        String sql = "SELECT * FROM NOTIFICACIONES WHERE id_notificacion = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idNotificacion);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return construirNotificacion(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lista todas las notificaciones
     */
    public List<Notificacion> listar() {
        List<Notificacion> notificaciones = new ArrayList<>();
        String sql = "SELECT * FROM NOTIFICACIONES ORDER BY fecha_creacion DESC";
        
        try (Connection conn = Conexion.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                notificaciones.add(construirNotificacion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notificaciones;
    }

    /**
     * Lista notificaciones por usuario
     */
    public List<Notificacion> listarPorUsuario(int idUsuario) {
        List<Notificacion> notificaciones = new ArrayList<>();
        String sql = "SELECT * FROM NOTIFICACIONES WHERE id_usuario = ? ORDER BY fecha_creacion DESC";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                notificaciones.add(construirNotificacion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notificaciones;
    }

    /**
     * Lista notificaciones sin leer de un usuario
     */
    public List<Notificacion> listarSinLeer(int idUsuario) {
        List<Notificacion> notificaciones = new ArrayList<>();
        String sql = "SELECT * FROM NOTIFICACIONES WHERE id_usuario = ? AND leida = 0 ORDER BY fecha_creacion DESC";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                notificaciones.add(construirNotificacion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notificaciones;
    }

    /**
     * Marca una notificación como leída
     */
    public boolean marcarComoLeida(int idNotificacion) {
        String sql = "UPDATE NOTIFICACIONES SET leida = 1, fecha_leida = SYSDATE WHERE id_notificacion = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idNotificacion);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Marca todas las notificaciones de un usuario como leídas
     */
    public boolean marcarTodoComoLeido(int idUsuario) {
        String sql = "UPDATE NOTIFICACIONES SET leida = 1, fecha_leida = SYSDATE WHERE id_usuario = ? AND leida = 0";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsuario);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cuenta notificaciones sin leer de un usuario
     */
    public int contarSinLeer(int idUsuario) {
        String sql = "SELECT COUNT(*) as total FROM NOTIFICACIONES WHERE id_usuario = ? AND leida = 0";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Elimina una notificación
     */
    public boolean eliminar(int idNotificacion) {
        String sql = "DELETE FROM NOTIFICACIONES WHERE id_notificacion = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idNotificacion);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina todas las notificaciones de un usuario
     */
    public boolean eliminarTodas(int idUsuario) {
        String sql = "DELETE FROM NOTIFICACIONES WHERE id_usuario = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idUsuario);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Construye un objeto Notificacion a partir de un ResultSet
     */
    private Notificacion construirNotificacion(ResultSet rs) throws SQLException {
        Notificacion notif = new Notificacion();
        notif.setIdNotificacion(rs.getInt("id_notificacion"));
        notif.setIdUsuario(rs.getInt("id_usuario"));
        notif.setTipo(rs.getString("tipo"));
        notif.setAsunto(rs.getString("asunto"));
        notif.setMensaje(rs.getString("mensaje"));
        notif.setLeida(rs.getInt("leida") == 1);
        
        java.sql.Date fechaCreacion = rs.getDate("fecha_creacion");
        if (fechaCreacion != null) {
            notif.setFechaCreacion(new java.util.Date(fechaCreacion.getTime()));
        }
        
        java.sql.Date fechaLeida = rs.getDate("fecha_leida");
        if (fechaLeida != null) {
            notif.setFechaLeida(new java.util.Date(fechaLeida.getTime()));
        }
        
        return notif;
    }
}
