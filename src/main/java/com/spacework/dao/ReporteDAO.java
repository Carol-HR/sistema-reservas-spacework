package com.spacework.dao;

import com.spacework.util.Conexion;

import java.sql.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class ReporteDAO {

    /**
     * Ingresos mensuales: suma de montos de reservas COMPLETADAS agrupados por año-mes.
     * @param anio  año a consultar (ej. 2025)
     * @return mapa ordenado con clave "MM/YYYY" y valor suma de montos
     */
    public Map<String, Double> ingresosMensuales(int anio) throws SQLException {
        String sql = "SELECT TO_CHAR(fecha_inicio, 'MM/YYYY') mes, SUM(monto_total) total "
                   + "FROM RESERVAS "
                   + "WHERE estado = 'CONFIRMADA' AND EXTRACT(YEAR FROM fecha_inicio) = ? "
                   + "GROUP BY TO_CHAR(fecha_inicio, 'MM/YYYY') "
                   + "ORDER BY MIN(fecha_inicio)";
        Map<String, Double> resultado = new LinkedHashMap<>();
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, anio);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                resultado.put(rs.getString("mes"), rs.getDouble("total"));
            }
        } finally {
            Conexion.cerrar(conn);
        }
        return resultado;
    }

    /**
     * Cantidad de reservas por estado (para gráfico de distribución).
     * @return mapa con clave = estado, valor = cantidad
     */
    public Map<String, Integer> reservasPorEstado() throws SQLException {
        String sql = "SELECT estado, COUNT(*) cantidad FROM RESERVAS GROUP BY estado ORDER BY estado";
        Map<String, Integer> resultado = new LinkedHashMap<>();
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            while (rs.next()) {
                resultado.put(rs.getString("estado"), rs.getInt("cantidad"));
            }
        } finally {
            Conexion.cerrar(conn);
        }
        return resultado;
    }

    /**
     * Ocupación por espacio: cantidad de reservas NO canceladas por espacio.
     * @return mapa con clave = nombre del espacio, valor = cantidad de reservas
     */
    public Map<String, Integer> ocupacionPorEspacio() throws SQLException {
        String sql = "SELECT e.nombre, COUNT(r.id_reserva) cantidad "
                   + "FROM ESPACIOS e "
                   + "LEFT JOIN RESERVAS r ON e.id_espacio = r.id_espacio "
                   + "    AND r.estado NOT IN ('CANCELADA') "
                   + "GROUP BY e.nombre "
                   + "ORDER BY cantidad DESC";
        Map<String, Integer> resultado = new LinkedHashMap<>();
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            while (rs.next()) {
                resultado.put(rs.getString("nombre"), rs.getInt("cantidad"));
            }
        } finally {
            Conexion.cerrar(conn);
        }
        return resultado;
    }
}
