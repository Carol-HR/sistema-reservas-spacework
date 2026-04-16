package com.spacework.dao;

import com.spacework.model.Horario;
import com.spacework.util.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla HORARIOS
 * Gestiona horarios operacionales de espacios
 */
public class HorarioDAO {

    /**
     * Inserta un nuevo horario
     */
    public boolean insertar(Horario horario) {
        String sql = "INSERT INTO HORARIOS (id_horario, id_espacio, dia_semana, hora_apertura, hora_cierre, estado) " +
                     "VALUES (SEQ_HORARIOS.NEXTVAL, ?, ?, ?, ?, ?)";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, horario.getIdEspacio());
            pstmt.setInt(2, horario.getDiaSemana());
            pstmt.setString(3, horario.getHoraApertura());
            pstmt.setString(4, horario.getHoraCierre());
            pstmt.setString(5, horario.getEstado());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza un horario existente
     */
    public boolean actualizar(Horario horario) {
        String sql = "UPDATE HORARIOS SET id_espacio = ?, dia_semana = ?, hora_apertura = ?, " +
                     "hora_cierre = ?, estado = ? WHERE id_horario = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, horario.getIdEspacio());
            pstmt.setInt(2, horario.getDiaSemana());
            pstmt.setString(3, horario.getHoraApertura());
            pstmt.setString(4, horario.getHoraCierre());
            pstmt.setString(5, horario.getEstado());
            pstmt.setInt(6, horario.getIdHorario());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Busca un horario por ID
     */
    public Horario buscarPorId(int idHorario) {
        String sql = "SELECT * FROM HORARIOS WHERE id_horario = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idHorario);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return construirHorario(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lista todos los horarios
     */
    public List<Horario> listar() {
        List<Horario> horarios = new ArrayList<>();
        String sql = "SELECT * FROM HORARIOS ORDER BY id_espacio, dia_semana";
        
        try (Connection conn = Conexion.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                horarios.add(construirHorario(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return horarios;
    }

    /**
     * Lista horarios por espacio
     */
    public List<Horario> listarPorEspacio(int idEspacio) {
        List<Horario> horarios = new ArrayList<>();
        String sql = "SELECT * FROM HORARIOS WHERE id_espacio = ? ORDER BY dia_semana";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idEspacio);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                horarios.add(construirHorario(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return horarios;
    }

    /**
     * Obtiene el horario para un espacio y día específico
     */
    public Horario obtenerPorDia(int idEspacio, int diaSemana) {
        String sql = "SELECT * FROM HORARIOS WHERE id_espacio = ? AND dia_semana = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idEspacio);
            pstmt.setInt(2, diaSemana);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return construirHorario(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Desactiva un horario
     */
    public boolean desactivar(int idHorario) {
        String sql = "UPDATE HORARIOS SET estado = 'INACTIVO' WHERE id_horario = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idHorario);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Construye un objeto Horario a partir de un ResultSet
     */
    private Horario construirHorario(ResultSet rs) throws SQLException {
        Horario horario = new Horario();
        horario.setIdHorario(rs.getInt("id_horario"));
        horario.setIdEspacio(rs.getInt("id_espacio"));
        horario.setDiaSemana(rs.getInt("dia_semana"));
        horario.setHoraApertura(rs.getString("hora_apertura"));
        horario.setHoraCierre(rs.getString("hora_cierre"));
        horario.setEstado(rs.getString("estado"));
        
        return horario;
    }
}
