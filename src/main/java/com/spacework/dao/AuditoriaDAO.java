package com.spacework.dao;

import com.spacework.model.AuditoriaLog;
import com.spacework.util.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuditoriaDAO {

    public void registrar(AuditoriaLog log) throws SQLException {
        String sql = "INSERT INTO AUDITORIA (id_auditoria, tabla, id_registro, tipo_cambio, usuario, fecha_hora, cambios) " +
                     "VALUES (SEQ_AUDITORIA.NEXTVAL, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, log.getTabla());
            pstmt.setInt(2, log.getIdRegistro());
            pstmt.setString(3, log.getTipoCambio());
            pstmt.setString(4, log.getUsuario());
            pstmt.setTimestamp(5, new java.sql.Timestamp(log.getFechaHora().getTime()));
            pstmt.setString(6, log.getCambios());
            pstmt.executeUpdate();
        }
    }

    public List<AuditoriaLog> listarPorTabla(String tabla, int limite) throws SQLException {
        List<AuditoriaLog> lista = new ArrayList<>();
        String sql = "SELECT id_auditoria, tabla, id_registro, tipo_cambio, usuario, fecha_hora, cambios " +
                     "FROM AUDITORIA WHERE tabla = ? ORDER BY fecha_hora DESC FETCH FIRST ? ROWS ONLY";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tabla);
            pstmt.setInt(2, limite);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new AuditoriaLog(
                        rs.getInt("id_auditoria"),
                        rs.getString("tabla"),
                        rs.getInt("id_registro"),
                        rs.getString("tipo_cambio"),
                        rs.getString("usuario"),
                        new Date(rs.getTimestamp("fecha_hora").getTime()),
                        rs.getString("cambios")
                    ));
                }
            }
        }
        return lista;
    }
}
