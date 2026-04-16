package com.spacework.controller;

import com.spacework.dao.EspacioDAO;
import com.spacework.model.Espacio;

import java.sql.SQLException;
import java.util.List;

public class EspacioController {

    private final EspacioDAO espacioDAO = new EspacioDAO();

    public List<Espacio> listarEspacios() throws SQLException {
        return espacioDAO.listar();
    }

    public Espacio buscarPorId(int idEspacio) throws SQLException {
        return espacioDAO.buscarPorId(idEspacio);
    }

    public void registrarEspacio(String nombre, String tipo, int capacidad,
                                  String ubicacion, double precioPorHora) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del espacio es obligatorio.");
        }
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor a cero.");
        }
        if (precioPorHora <= 0) {
            throw new IllegalArgumentException("El precio por hora debe ser mayor a cero.");
        }
        Espacio e = new Espacio();
        e.setNombre(nombre.trim());
        e.setTipo(tipo);
        e.setCapacidad(capacidad);
        e.setUbicacion(ubicacion != null ? ubicacion.trim() : "");
        e.setPrecioPorHora(precioPorHora);
        espacioDAO.insertar(e);
    }

    public void actualizarEspacio(int idEspacio, String nombre, String tipo,
                                   int capacidad, String ubicacion, double precioPorHora) throws SQLException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del espacio es obligatorio.");
        }
        if (capacidad <= 0) {
            throw new IllegalArgumentException("La capacidad debe ser mayor a cero.");
        }
        if (precioPorHora <= 0) {
            throw new IllegalArgumentException("El precio por hora debe ser mayor a cero.");
        }
        Espacio e = new Espacio();
        e.setIdEspacio(idEspacio);
        e.setNombre(nombre.trim());
        e.setTipo(tipo);
        e.setCapacidad(capacidad);
        e.setUbicacion(ubicacion != null ? ubicacion.trim() : "");
        e.setPrecioPorHora(precioPorHora);
        espacioDAO.actualizar(e);
    }

    public void desactivarEspacio(int idEspacio) throws SQLException {
        espacioDAO.desactivar(idEspacio);
    }
}
