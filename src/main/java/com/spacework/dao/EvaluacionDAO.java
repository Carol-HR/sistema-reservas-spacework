package com.spacework.dao;

import com.spacework.model.Evaluacion;
import com.spacework.util.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla EVALUACIONES
 * Gestiona calificaciones y comentarios de usuarios
 */
public class EvaluacionDAO {

    /**
     * Inserta una nueva evaluación
     */
    public boolean insertar(Evaluacion evaluacion) {
        String sql = "INSERT INTO EVALUACIONES (id_evaluacion, id_reserva, id_cliente, calificacion, comentario, fecha_evaluacion) " +
                     "VALUES (SEQ_EVALUACIONES.NEXTVAL, ?, ?, ?, ?, SYSDATE)";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, evaluacion.getIdReserva());
            pstmt.setInt(2, evaluacion.getIdCliente());
            pstmt.setInt(3, evaluacion.getCalificacion());
            pstmt.setString(4, evaluacion.getComentario());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza una evaluación existente
     */
    public boolean actualizar(Evaluacion evaluacion) {
        String sql = "UPDATE EVALUACIONES SET id_reserva = ?, id_cliente = ?, calificacion = ?, " +
                     "comentario = ? WHERE id_evaluacion = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, evaluacion.getIdReserva());
            pstmt.setInt(2, evaluacion.getIdCliente());
            pstmt.setInt(3, evaluacion.getCalificacion());
            pstmt.setString(4, evaluacion.getComentario());
            pstmt.setInt(5, evaluacion.getIdEvaluacion());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Busca una evaluación por ID
     */
    public Evaluacion buscarPorId(int idEvaluacion) {
        String sql = "SELECT * FROM EVALUACIONES WHERE id_evaluacion = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idEvaluacion);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return construirEvaluacion(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lista todas las evaluaciones
     */
    public List<Evaluacion> listar() {
        List<Evaluacion> evaluaciones = new ArrayList<>();
        String sql = "SELECT * FROM EVALUACIONES ORDER BY fecha_evaluacion DESC";
        
        try (Connection conn = Conexion.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                evaluaciones.add(construirEvaluacion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evaluaciones;
    }

    /**
     * Lista evaluaciones por reserva
     */
    public List<Evaluacion> listarPorReserva(int idReserva) {
        List<Evaluacion> evaluaciones = new ArrayList<>();
        String sql = "SELECT * FROM EVALUACIONES WHERE id_reserva = ? ORDER BY fecha_evaluacion DESC";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idReserva);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                evaluaciones.add(construirEvaluacion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return evaluaciones;
    }

    /**
     * Calcula el promedio de calificación para un espacio
     */
    public double promedioCalificacion(int idEspacio) {
        String sql = "SELECT AVG(e.calificacion) as promedio FROM EVALUACIONES e " +
                     "INNER JOIN RESERVAS r ON e.id_reserva = r.id_reserva " +
                     "WHERE r.id_espacio = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idEspacio);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                double promedio = rs.getDouble("promedio");
                return rs.wasNull() ? 0 : promedio;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Cuenta evaluaciones para un espacio
     */
    public int contarEvaluacionesPorEspacio(int idEspacio) {
        String sql = "SELECT COUNT(*) as total FROM EVALUACIONES e " +
                     "INNER JOIN RESERVAS r ON e.id_reserva = r.id_reserva " +
                     "WHERE r.id_espacio = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idEspacio);
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
     * Elimina una evaluación
     */
    public boolean eliminar(int idEvaluacion) {
        String sql = "DELETE FROM EVALUACIONES WHERE id_evaluacion = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idEvaluacion);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Construye un objeto Evaluacion a partir de un ResultSet
     */
    private Evaluacion construirEvaluacion(ResultSet rs) throws SQLException {
        Evaluacion eval = new Evaluacion();
        eval.setIdEvaluacion(rs.getInt("id_evaluacion"));
        eval.setIdReserva(rs.getInt("id_reserva"));
        eval.setIdCliente(rs.getInt("id_cliente"));
        eval.setCalificacion(rs.getInt("calificacion"));
        eval.setComentario(rs.getString("comentario"));
        
        java.sql.Date fechaEval = rs.getDate("fecha_evaluacion");
        if (fechaEval != null) {
            eval.setFechaEvaluacion(new java.util.Date(fechaEval.getTime()));
        }
        
        return eval;
    }
}
