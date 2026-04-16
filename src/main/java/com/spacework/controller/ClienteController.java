package com.spacework.controller;

import com.spacework.dao.ClienteDAO;
import com.spacework.model.Cliente;

import java.sql.SQLException;
import java.util.List;

public class ClienteController {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    public List<Cliente> listarClientes() throws SQLException {
        return clienteDAO.listar();
    }

    public Cliente buscarPorDni(String dni) throws SQLException {
        return clienteDAO.buscarPorDni(dni);
    }

    public void registrarCliente(String nombre, String apellido, String dni,
                                  String email, String telefono) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (apellido == null || apellido.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }
        if (dni == null || !dni.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("El email no tiene un formato válido.");
        }
        // Verificar que el DNI no esté registrado
        if (clienteDAO.buscarPorDni(dni) != null) {
            throw new IllegalArgumentException("Ya existe un cliente con ese DNI.");
        }
        Cliente c = new Cliente();
        c.setNombre(nombre.trim());
        c.setApellido(apellido.trim());
        c.setDni(dni.trim());
        c.setEmail(email.trim());
        c.setTelefono(telefono != null ? telefono.trim() : "");
        clienteDAO.insertar(c);
    }

    public void actualizarCliente(int idCliente, String nombre, String apellido,
                                   String email, String telefono) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        if (apellido == null || apellido.trim().isEmpty()) {
            throw new IllegalArgumentException("El apellido es obligatorio.");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("El email no tiene un formato válido.");
        }
        Cliente c = new Cliente();
        c.setIdCliente(idCliente);
        c.setNombre(nombre.trim());
        c.setApellido(apellido.trim());
        c.setEmail(email.trim());
        c.setTelefono(telefono != null ? telefono.trim() : "");
        clienteDAO.actualizar(c);
    }

    public void desactivarCliente(int idCliente) throws SQLException {
        clienteDAO.desactivar(idCliente);
    }
}
