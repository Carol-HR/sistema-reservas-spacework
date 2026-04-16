package com.spacework.dao;

import com.spacework.model.Descuento;
import com.spacework.util.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla DESCUENTOS
 * Gestiona códigos promocionales y descuentos
 */
public class DescuentoDAO {

    /**
     * Inserta un nuevo descuento
     */
    public boolean insertar(Descuento descuento) {
        String sql = "INSERT INTO DESCUENTOS (id_descuento, codigo, descripcion, porcentaje, monto_minimo, " +
                     "fecha_inicio, fecha_fin, usos_maximos, usos_actuales, estado) " +
                     "VALUES (SEQ_DESCUENTOS.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, descuento.getCodigo());
            pstmt.setString(2, descuento.getDescripcion());
            pstmt.setDouble(3, descuento.getPorcentaje());
            pstmt.setDouble(4, descuento.getMontoMinimo());
            pstmt.setDate(5, new java.sql.Date(descuento.getFechaInicio().getTime()));
            pstmt.setDate(6, new java.sql.Date(descuento.getFechaFin().getTime()));
            pstmt.setInt(7, descuento.getUsosMaximos());
            pstmt.setInt(8, descuento.getUsosActuales());
            pstmt.setString(9, descuento.getEstado());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza un descuento existente
     */
    public boolean actualizar(Descuento descuento) {
        String sql = "UPDATE DESCUENTOS SET codigo = ?, descripcion = ?, porcentaje = ?, " +
                     "monto_minimo = ?, fecha_inicio = ?, fecha_fin = ?, usos_maximos = ?, " +
                     "usos_actuales = ?, estado = ? WHERE id_descuento = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, descuento.getCodigo());
            pstmt.setString(2, descuento.getDescripcion());
            pstmt.setDouble(3, descuento.getPorcentaje());
            pstmt.setDouble(4, descuento.getMontoMinimo());
            pstmt.setDate(5, new java.sql.Date(descuento.getFechaInicio().getTime()));
            pstmt.setDate(6, new java.sql.Date(descuento.getFechaFin().getTime()));
            pstmt.setInt(7, descuento.getUsosMaximos());
            pstmt.setInt(8, descuento.getUsosActuales());
            pstmt.setString(9, descuento.getEstado());
            pstmt.setInt(10, descuento.getIdDescuento());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Busca un descuento por ID
     */
    public Descuento buscarPorId(int idDescuento) {
        String sql = "SELECT * FROM DESCUENTOS WHERE id_descuento = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idDescuento);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return construirDescuento(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Busca un descuento por código
     */
    public Descuento buscarPorCodigo(String codigo) {
        String sql = "SELECT * FROM DESCUENTOS WHERE codigo = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, codigo);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return construirDescuento(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lista todos los descuentos
     */
    public List<Descuento> listar() {
        List<Descuento> descuentos = new ArrayList<>();
        String sql = "SELECT * FROM DESCUENTOS ORDER BY estado DESC, fecha_fin DESC";
        
        try (Connection conn = Conexion.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                descuentos.add(construirDescuento(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return descuentos;
    }

    /**
     * Valida si un código de descuento es válido y se puede aplicar
     */
    public boolean validarCodigo(String codigo, double monto) {
        Descuento desc = buscarPorCodigo(codigo);
        
        if (desc == null) return false;
        
        return desc.esVigente() && (desc.getMontoMinimo() == 0 || monto >= desc.getMontoMinimo());
    }

    /**
     * Incrementa el contador de usos de un descuento
     */
    public boolean incrementarUsos(int idDescuento) {
        String sql = "UPDATE DESCUENTOS SET usos_actuales = usos_actuales + 1 WHERE id_descuento = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idDescuento);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Desactiva un descuento
     */
    public boolean desactivar(int idDescuento) {
        String sql = "UPDATE DESCUENTOS SET estado = 'INACTIVO' WHERE id_descuento = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idDescuento);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Construye un objeto Descuento a partir de un ResultSet
     */
    private Descuento construirDescuento(ResultSet rs) throws SQLException {
        Descuento desc = new Descuento();
        desc.setIdDescuento(rs.getInt("id_descuento"));
        desc.setCodigo(rs.getString("codigo"));
        desc.setDescripcion(rs.getString("descripcion"));
        desc.setPorcentaje(rs.getDouble("porcentaje"));
        desc.setMontoMinimo(rs.getDouble("monto_minimo"));
        
        java.sql.Date fechaInicio = rs.getDate("fecha_inicio");
        if (fechaInicio != null) {
            desc.setFechaInicio(new java.util.Date(fechaInicio.getTime()));
        }
        
        java.sql.Date fechaFin = rs.getDate("fecha_fin");
        if (fechaFin != null) {
            desc.setFechaFin(new java.util.Date(fechaFin.getTime()));
        }
        
        desc.setUsosMaximos(rs.getInt("usos_maximos"));
        desc.setUsosActuales(rs.getInt("usos_actuales"));
        desc.setEstado(rs.getString("estado"));
        
        return desc;
    }
}
