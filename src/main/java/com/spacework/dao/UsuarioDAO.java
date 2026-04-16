package com.spacework.dao;

import com.spacework.model.Usuario;
import com.spacework.util.Conexion;

import java.sql.*;

public class UsuarioDAO {

    public Usuario autenticar(String username, String passwordHash) throws SQLException {
        String sql = "SELECT * FROM USUARIOS WHERE username = ? AND password_hash = ? AND estado = 'ACTIVO'";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapearUsuario(rs);
            }
            return null;
        } finally {
            Conexion.cerrar(conn);
        }
    }

    public Usuario buscarPorId(int idUsuario) throws SQLException {
        String sql = "SELECT * FROM USUARIOS WHERE id_usuario = ?";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapearUsuario(rs);
            }
            return null;
        } finally {
            Conexion.cerrar(conn);
        }
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setNombre(rs.getString("nombre"));
        u.setEmail(rs.getString("email"));
        u.setRol(rs.getString("rol"));
        u.setEstado(rs.getString("estado"));
        u.setFechaRegistro(rs.getDate("fecha_registro"));
        return u;
    }

    public void actualizarContraseña(String username, String nuevoHash) throws SQLException {
        String sql = "UPDATE USUARIOS SET password_hash = ? WHERE username = ?";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nuevoHash);
            ps.setString(2, username);
            ps.executeUpdate();
            conn.commit();
        } finally {
            Conexion.cerrar(conn);
        }
    }
}
