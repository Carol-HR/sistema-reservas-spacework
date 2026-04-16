package com.spacework.controller;

import com.spacework.dao.UsuarioDAO;
import com.spacework.model.Usuario;
import com.spacework.util.HashUtil;

import java.sql.SQLException;

public class AuthController {

    private static AuthController instancia;
    private static Usuario usuarioActual;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private AuthController() {}

    public static AuthController getInstance() {
        if (instancia == null) {
            instancia = new AuthController();
        }
        return instancia;
    }

    /**
     * Intenta autenticar al usuario.
     * @return true si las credenciales son válidas, false si no.
     */
    public boolean login(String username, String password) throws SQLException {
        String hash = HashUtil.sha256(password);
        Usuario u = usuarioDAO.autenticar(username, hash);
        if (u != null) {
            usuarioActual = u;
            return true;
        }
        return false;
    }

    public void logout() {
        usuarioActual = null;
    }

    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    public boolean estaAutenticado() {
        return usuarioActual != null;
    }

    public boolean esAdministrador() {
        return usuarioActual != null && "ADMINISTRADOR".equals(usuarioActual.getRol());
    }

    public void cambiarContraseña(String username, String contraseñaActual, String contraseñaNueva) throws SQLException {
        String hashActual = HashUtil.sha256(contraseñaActual);
        Usuario u = usuarioDAO.autenticar(username, hashActual);
        if (u == null) {
            throw new SQLException("Contraseña actual incorrecta.");
        }
        String hashNueva = HashUtil.sha256(contraseñaNueva);
        usuarioDAO.actualizarContraseña(username, hashNueva);
    }
}
