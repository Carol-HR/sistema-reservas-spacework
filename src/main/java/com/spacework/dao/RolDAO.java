package com.spacework.dao;

import com.spacework.model.Rol;
import com.spacework.util.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RolDAO {

    public List<Rol> listarRoles() throws SQLException {
        List<Rol> roles = new ArrayList<>();
        String sql = "SELECT id_rol, nombre, descripcion FROM ROLES ORDER BY nombre";
        try (Connection conn = Conexion.getConexion();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                roles.add(new Rol(rs.getInt("id_rol"), rs.getString("nombre"), rs.getString("descripcion")));
            }
        }
        return roles;
    }

    public Rol obtenerPorNombre(String nombre) throws SQLException {
        String sql = "SELECT id_rol, nombre, descripcion FROM ROLES WHERE nombre = ?";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Rol(rs.getInt("id_rol"), rs.getString("nombre"), rs.getString("descripcion"));
                }
            }
        }
        return null;
    }

    public void registrarRol(String nombre, String descripcion) throws SQLException {
        String sql = "INSERT INTO ROLES (id_rol, nombre, descripcion) VALUES (SEQ_ROLES.NEXTVAL, ?, ?)";
        try (Connection conn = Conexion.getConexion();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, descripcion);
            pstmt.executeUpdate();
        }
    }
}
