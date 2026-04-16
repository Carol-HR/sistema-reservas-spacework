package com.spacework.dao;

import com.spacework.model.Cliente;
import com.spacework.model.Espacio;
import com.spacework.model.Reserva;
import com.spacework.util.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO {

    public void insertar(Reserva r) throws SQLException {
        String sql = "INSERT INTO RESERVAS (id_reserva, id_cliente, id_espacio, fecha_inicio, fecha_fin, monto_total, estado) "
                   + "VALUES (SEQ_RESERVAS.NEXTVAL, ?, ?, ?, ?, ?, 'PENDIENTE')";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, r.getCliente().getIdCliente());
            ps.setInt(2, r.getEspacio().getIdEspacio());
            ps.setTimestamp(3, new Timestamp(r.getFechaInicio().getTime()));
            ps.setTimestamp(4, new Timestamp(r.getFechaFin().getTime()));
            ps.setDouble(5, r.getMontoTotal());
            ps.executeUpdate();
            conn.commit();
        } finally {
            Conexion.cerrar(conn);
        }
    }

    public void cambiarEstado(int idReserva, String nuevoEstado) throws SQLException {
        String sql = "UPDATE RESERVAS SET estado = ? WHERE id_reserva = ?";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idReserva);
            ps.executeUpdate();
            conn.commit();
        } finally {
            Conexion.cerrar(conn);
        }
    }

    public boolean verificarDisponibilidad(int idEspacio, Timestamp inicio, Timestamp fin) throws SQLException {
        String sql = "SELECT COUNT(*) FROM RESERVAS "
                   + "WHERE id_espacio = ? AND estado NOT IN ('CANCELADA') "
                   + "AND (fecha_inicio < ? AND fecha_fin > ?)";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idEspacio);
            ps.setTimestamp(2, fin);
            ps.setTimestamp(3, inicio);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) == 0;
            return true;
        } finally {
            Conexion.cerrar(conn);
        }
    }

    public List<Reserva> listarPorCliente(int idCliente) throws SQLException {
        String sql = "SELECT r.*, c.nombre cn, c.apellido ca, c.dni, "
                   + "e.nombre en, e.tipo, e.precio_por_hora "
                   + "FROM RESERVAS r "
                   + "JOIN CLIENTES c ON r.id_cliente = c.id_cliente "
                   + "JOIN ESPACIOS e ON r.id_espacio = e.id_espacio "
                   + "WHERE r.id_cliente = ? ORDER BY r.fecha_inicio DESC";
        List<Reserva> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idCliente);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearReserva(rs));
        } finally {
            Conexion.cerrar(conn);
        }
        return lista;
    }

    public List<Reserva> listarPorEspacio(int idEspacio) throws SQLException {
        String sql = "SELECT r.*, c.nombre cn, c.apellido ca, c.dni, "
                   + "e.nombre en, e.tipo, e.precio_por_hora "
                   + "FROM RESERVAS r "
                   + "JOIN CLIENTES c ON r.id_cliente = c.id_cliente "
                   + "JOIN ESPACIOS e ON r.id_espacio = e.id_espacio "
                   + "WHERE r.id_espacio = ? ORDER BY r.fecha_inicio";
        List<Reserva> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idEspacio);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) lista.add(mapearReserva(rs));
        } finally {
            Conexion.cerrar(conn);
        }
        return lista;
    }

    public List<Reserva> listarTodas() throws SQLException {
        String sql = "SELECT r.*, c.nombre cn, c.apellido ca, c.dni, "
                   + "e.nombre en, e.tipo, e.precio_por_hora "
                   + "FROM RESERVAS r "
                   + "JOIN CLIENTES c ON r.id_cliente = c.id_cliente "
                   + "JOIN ESPACIOS e ON r.id_espacio = e.id_espacio "
                   + "ORDER BY r.fecha_inicio DESC";
        List<Reserva> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            ResultSet rs = conn.prepareStatement(sql).executeQuery();
            while (rs.next()) lista.add(mapearReserva(rs));
        } finally {
            Conexion.cerrar(conn);
        }
        return lista;
    }

    public Reserva buscarPorId(int idReserva) throws SQLException {
        String sql = "SELECT r.*, c.nombre cn, c.apellido ca, c.dni, "
                   + "e.nombre en, e.tipo, e.precio_por_hora "
                   + "FROM RESERVAS r "
                   + "JOIN CLIENTES c ON r.id_cliente = c.id_cliente "
                   + "JOIN ESPACIOS e ON r.id_espacio = e.id_espacio "
                   + "WHERE r.id_reserva = ?";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idReserva);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapearReserva(rs);
        } finally {
            Conexion.cerrar(conn);
        }
        return null;
    }

    private Reserva mapearReserva(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setNombre(rs.getString("cn"));
        c.setApellido(rs.getString("ca"));
        c.setDni(rs.getString("dni"));

        Espacio e = new Espacio();
        e.setIdEspacio(rs.getInt("id_espacio"));
        e.setNombre(rs.getString("en"));
        e.setTipo(rs.getString("tipo"));
        e.setPrecioPorHora(rs.getDouble("precio_por_hora"));

        Reserva r = new Reserva();
        r.setIdReserva(rs.getInt("id_reserva"));
        r.setCliente(c);
        r.setEspacio(e);
        r.setFechaInicio(rs.getTimestamp("fecha_inicio"));
        r.setFechaFin(rs.getTimestamp("fecha_fin"));
        r.setMontoTotal(rs.getDouble("monto_total"));
        r.setEstado(rs.getString("estado"));
        r.setFechaCreacion(rs.getDate("fecha_creacion"));
        return r;
    }
}
