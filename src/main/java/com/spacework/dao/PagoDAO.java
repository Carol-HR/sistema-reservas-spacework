package com.spacework.dao;

import com.spacework.model.Pago;
import com.spacework.util.Conexion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la tabla PAGOS
 * Gestiona operaciones CRUD de pagos
 */
public class PagoDAO {

    /**
     * Inserta un nuevo pago en la base de datos
     */
    public boolean insertar(Pago pago) {
        String sql = "INSERT INTO PAGOS (id_pago, id_reserva, monto, metodo_pago, estado_pago, fecha_pago, fecha_creacion) " +
                     "VALUES (SEQ_PAGOS.NEXTVAL, ?, ?, ?, ?, ?, SYSDATE)";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, pago.getIdReserva());
            pstmt.setDouble(2, pago.getMonto());
            // Usar EFECTIVO como default si no tiene método definido
            String metodoPago = pago.getMetodoPago() != null && !pago.getMetodoPago().isEmpty() 
                                ? pago.getMetodoPago() 
                                : "EFECTIVO";
            pstmt.setString(3, metodoPago);
            pstmt.setString(4, pago.getEstadoPago());
            pstmt.setDate(5, pago.getFechaPago() != null ? new java.sql.Date(pago.getFechaPago().getTime()) : null);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza un pago existente
     */
    public boolean actualizar(Pago pago) {
        String sql = "UPDATE PAGOS SET id_reserva = ?, monto = ?, metodo_pago = ?, " +
                     "estado_pago = ?, fecha_pago = ? WHERE id_pago = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, pago.getIdReserva());
            pstmt.setDouble(2, pago.getMonto());
            pstmt.setString(3, pago.getMetodoPago());
            pstmt.setString(4, pago.getEstadoPago());
            pstmt.setDate(5, pago.getFechaPago() != null ? new java.sql.Date(pago.getFechaPago().getTime()) : null);
            pstmt.setInt(6, pago.getIdPago());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Busca un pago por su ID
     */
    public Pago buscarPorId(int idPago) {
        String sql = "SELECT * FROM PAGOS WHERE id_pago = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idPago);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return construirPago(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lista todos los pagos
     */
    public List<Pago> listar() {
        List<Pago> pagos = new ArrayList<>();
        String sql = "SELECT * FROM PAGOS ORDER BY fecha_creacion DESC";
        
        try (Connection conn = Conexion.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                pagos.add(construirPago(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pagos;
    }

    /**
     * Lista pagos por reserva
     */
    public List<Pago> listarPorReserva(int idReserva) {
        List<Pago> pagos = new ArrayList<>();
        String sql = "SELECT * FROM PAGOS WHERE id_reserva = ? ORDER BY fecha_creacion DESC";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idReserva);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                pagos.add(construirPago(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pagos;
    }

    /**
     * Cambia el estado de un pago
     */
    public boolean cambiarEstado(int idPago, String nuevoEstado) {
        String sql = "UPDATE PAGOS SET estado_pago = ? WHERE id_pago = ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, idPago);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cuenta pagos completados en un período
     */
    public int contarPagosCompletados(java.util.Date fechaInicio, java.util.Date fechaFin) {
        String sql = "SELECT COUNT(*) as total FROM PAGOS WHERE estado_pago = 'COMPLETADO' " +
                     "AND fecha_pago >= ? AND fecha_pago <= ?";
        
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, new java.sql.Date(fechaInicio.getTime()));
            pstmt.setDate(2, new java.sql.Date(fechaFin.getTime()));
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
     * Marca el pago como COMPLETADO, establece el método de pago y la fecha actual.
     */
    public boolean pagar(int idPago, String metodoPago, double montoFinal, int idDescuento, double descuentoAplicado) {
        String sql = "UPDATE PAGOS SET estado_pago = 'COMPLETADO', metodo_pago = ?, monto = ?, fecha_pago = SYSDATE, "
                   + "descuento_aplicado = ?, id_descuento = NULLIF(?, 0) WHERE id_pago = ?";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, metodoPago);
            ps.setDouble(2, montoFinal);
            ps.setDouble(3, descuentoAplicado);
            ps.setInt(4, idDescuento);
            ps.setInt(5, idPago);
            int rows = ps.executeUpdate();
            conn.commit();
            return rows > 0;
        } catch (SQLException e) {
            // Si las columnas aún no existen, usar query sin descuento
            try {
                if (conn != null) {
                    PreparedStatement ps2 = conn.prepareStatement(
                        "UPDATE PAGOS SET estado_pago = 'COMPLETADO', metodo_pago = ?, fecha_pago = SYSDATE WHERE id_pago = ?");
                    ps2.setString(1, metodoPago);
                    ps2.setInt(2, idPago);
                    int r = ps2.executeUpdate();
                    conn.commit();
                    return r > 0;
                }
            } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            Conexion.cerrar(conn);
        }
    }

    // Método compatible hacia atrás
    public boolean pagar(int idPago, String metodoPago) {
        return pagar(idPago, metodoPago, 0, 0, 0);
    }

    /**
     * Construye un objeto Pago a partir de un ResultSet
     */
    private Pago construirPago(ResultSet rs) throws SQLException {
        Pago pago = new Pago();
        pago.setIdPago(rs.getInt("id_pago"));
        pago.setIdReserva(rs.getInt("id_reserva"));
        pago.setMonto(rs.getDouble("monto"));
        pago.setMetodoPago(rs.getString("metodo_pago"));
        pago.setEstadoPago(rs.getString("estado_pago"));
        
        java.sql.Date fechaPago = rs.getDate("fecha_pago");
        if (fechaPago != null) {
            pago.setFechaPago(new java.util.Date(fechaPago.getTime()));
        }
        
        java.sql.Date fechaCreacion = rs.getDate("fecha_creacion");
        if (fechaCreacion != null) {
            pago.setFechaCreacion(new java.util.Date(fechaCreacion.getTime()));
        }
        
        return pago;
    }
}
