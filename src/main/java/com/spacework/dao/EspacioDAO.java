package com.spacework.dao;

import com.spacework.model.Espacio;
import com.spacework.util.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EspacioDAO {

    public List<Espacio> listar() throws SQLException {
        String sql = "SELECT id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora, estado, imagen_url FROM ESPACIOS WHERE estado = 'ACTIVO' ORDER BY nombre";
        List<Espacio> lista = new ArrayList<>();
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapearEspacio(rs));
            }
        } finally {
            Conexion.cerrar(conn);
        }
        return lista;
    }

    public Espacio buscarPorId(int idEspacio) throws SQLException {
        String sql = "SELECT id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora, estado, imagen_url FROM ESPACIOS WHERE id_espacio = ?";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idEspacio);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapearEspacio(rs);
            return null;
        } finally {
            Conexion.cerrar(conn);
        }
    }

    public void insertar(Espacio e) throws SQLException {
        String sql = "INSERT INTO ESPACIOS (id_espacio, nombre, tipo, capacidad, ubicacion, precio_por_hora, estado, imagen_url) VALUES (SEQ_ESPACIOS.NEXTVAL, ?, ?, ?, ?, ?, 'ACTIVO', ?)";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getTipo());
            ps.setInt(3, e.getCapacidad());
            ps.setString(4, e.getUbicacion());
            ps.setDouble(5, e.getPrecioPorHora());
            ps.setString(6, e.getUrlImagen());
            ps.executeUpdate();
            conn.commit();
        } finally {
            Conexion.cerrar(conn);
        }
    }

    public void actualizar(Espacio e) throws SQLException {
        String sql = "UPDATE ESPACIOS SET nombre=?, tipo=?, capacidad=?, ubicacion=?, precio_por_hora=?, imagen_url=? WHERE id_espacio=?";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getTipo());
            ps.setInt(3, e.getCapacidad());
            ps.setString(4, e.getUbicacion());
            ps.setDouble(5, e.getPrecioPorHora());
            ps.setString(6, e.getUrlImagen());
            ps.setInt(7, e.getIdEspacio());
            ps.executeUpdate();
            conn.commit();
        } finally {
            Conexion.cerrar(conn);
        }
    }

    public void desactivar(int idEspacio) throws SQLException {
        String sql = "UPDATE ESPACIOS SET estado = 'INACTIVO' WHERE id_espacio = ?";
        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idEspacio);
            ps.executeUpdate();
            conn.commit();
        } finally {
            Conexion.cerrar(conn);
        }
    }

    private Espacio mapearEspacio(ResultSet rs) throws SQLException {
        Espacio e = new Espacio();
        e.setIdEspacio(rs.getInt("id_espacio"));
        e.setNombre(rs.getString("nombre"));
        e.setTipo(rs.getString("tipo"));
        e.setCapacidad(rs.getInt("capacidad"));
        e.setUbicacion(rs.getString("ubicacion"));
        e.setPrecioPorHora(rs.getDouble("precio_por_hora"));
        e.setEstado(rs.getString("estado"));
        
        // Lee imagen_url (columna CLOB en Oracle)
        String imagen = null;
        try {
            Clob clob = rs.getClob("imagen_url");
            System.out.println("[DEBUG imagen] ID=" + rs.getInt("id_espacio") + " clob=" + (clob == null ? "NULL" : "len=" + clob.length()));
            if (clob != null && clob.length() > 0) {
                imagen = clob.getSubString(1, (int) Math.min(clob.length(), (long) Integer.MAX_VALUE));
                System.out.println("[DEBUG imagen] ID=" + rs.getInt("id_espacio") + " leidos=" + imagen.length() + " inicio=" + imagen.substring(0, Math.min(60, imagen.length())));
            }
        } catch (Exception ex1) {
            System.err.println("[DEBUG imagen] CLOB fallo ID=" + e.getIdEspacio() + " error: " + ex1.getClass().getSimpleName() + ": " + ex1.getMessage());
            try {
                imagen = rs.getString("imagen_url");
                System.out.println("[DEBUG imagen] getString ID=" + e.getIdEspacio() + " resultado=" + (imagen == null ? "NULL" : imagen.length() + " chars"));
            } catch (Exception ex2) {
                System.err.println("[DEBUG imagen] getString tambien fallo: " + ex2.getMessage());
            }
        }
        
        if (imagen != null && !imagen.trim().isEmpty()) {
            e.setUrlImagen(imagen.trim());
        }
        return e;
    }
}
