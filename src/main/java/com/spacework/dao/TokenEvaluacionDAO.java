package com.spacework.dao;

import com.spacework.model.TokenEvaluacion;
import com.spacework.util.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestión de Tokens de Evaluación
 * Tokens temporales para permitir evaluación por email sin autenticación
 */
public class TokenEvaluacionDAO {

    /**
     * Inserta un nuevo token de evaluación
     */
    public boolean crearToken(TokenEvaluacion token) {
        String sql = "INSERT INTO TOKENS_EVALUACION (id_token, id_pago, token, email_cliente, " +
                "fecha_creacion, fecha_expiracion, utilizado) VALUES " +
                "(SEQ_TOKENS_EVALUACION.NEXTVAL, ?, ?, ?, SYSDATE, ?, 0)";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, token.getIdPago());
            pstmt.setString(2, token.getToken());
            pstmt.setString(3, token.getEmailCliente());
            pstmt.setDate(4, new java.sql.Date(token.getFechaExpiracion().getTime()));
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Busca un token por su valor
     */
    public TokenEvaluacion buscarPorToken(String token) {
        String sql = "SELECT id_token, id_pago, token, email_cliente, fecha_creacion, " +
                "fecha_expiracion, utilizado FROM TOKENS_EVALUACION WHERE token = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, token);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return construirTokenEvaluacion(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Busca un token por ID
     */
    public TokenEvaluacion buscarPorId(int idToken) {
        String sql = "SELECT id_token, id_pago, token, email_cliente, fecha_creacion, " +
                "fecha_expiracion, utilizado FROM TOKENS_EVALUACION WHERE id_token = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idToken);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return construirTokenEvaluacion(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Verifica si un token es válido (no expirado y no utilizado)
     */
    public boolean tokenValido(String token) {
        TokenEvaluacion t = buscarPorToken(token);
        
        if (t == null) return false;
        
        // Verificar que no esté utilizado
        if (t.getUtilizado() == 1) return false;
        
        // Verificar que no esté expirado
        java.util.Date ahora = new java.util.Date();
        if (ahora.after(t.getFechaExpiracion())) return false;
        
        return true;
    }

    /**
     * Verifica si un token está expirado
     */
    public boolean tokenExpirado(String token) {
        TokenEvaluacion t = buscarPorToken(token);
        
        if (t == null) return true;
        
        java.util.Date ahora = new java.util.Date();
        return ahora.after(t.getFechaExpiracion());
    }

    /**
     * Marca un token como utilizado
     */
    public boolean marcarUtilizado(int idToken) {
        String sql = "UPDATE TOKENS_EVALUACION SET utilizado = 1 WHERE id_token = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idToken);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene todos los tokens por pago
     */
    public List<TokenEvaluacion> obtenerPorPago(int idPago) {
        String sql = "SELECT id_token, id_pago, token, email_cliente, fecha_creacion, " +
                "fecha_expiracion, utilizado FROM TOKENS_EVALUACION WHERE id_pago = ? " +
                "ORDER BY fecha_creacion DESC";
        
        List<TokenEvaluacion> tokens = new ArrayList<>();
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idPago);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                tokens.add(construirTokenEvaluacion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return tokens;
    }

    /**
     * Obtiene tokens pendientes (no utilizados)
     */
    public List<TokenEvaluacion> obtenerPendientes() {
        String sql = "SELECT id_token, id_pago, token, email_cliente, fecha_creacion, " +
                "fecha_expiracion, utilizado FROM TOKENS_EVALUACION WHERE utilizado = 0 " +
                "AND fecha_expiracion > SYSDATE ORDER BY fecha_creacion DESC";
        
        List<TokenEvaluacion> tokens = new ArrayList<>();
        
        try (Connection conn = Conexion.getConexion();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                tokens.add(construirTokenEvaluacion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return tokens;
    }

    /**
     * Elimina tokens expirados
     */
    public boolean limpiarExpirados() {
        String sql = "DELETE FROM TOKENS_EVALUACION WHERE fecha_expiracion < SYSDATE";
        
        try (Connection conn = Conexion.getConexion();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Helper: construye objeto TokenEvaluacion desde ResultSet
     */
    private TokenEvaluacion construirTokenEvaluacion(ResultSet rs) throws SQLException {
        TokenEvaluacion token = new TokenEvaluacion();
        token.setIdToken(rs.getInt("id_token"));
        token.setIdPago(rs.getInt("id_pago"));
        token.setToken(rs.getString("token"));
        token.setEmailCliente(rs.getString("email_cliente"));
        token.setFechaCreacion(rs.getDate("fecha_creacion"));
        token.setFechaExpiracion(rs.getDate("fecha_expiracion"));
        token.setUtilizado(rs.getInt("utilizado"));
        return token;
    }
}
