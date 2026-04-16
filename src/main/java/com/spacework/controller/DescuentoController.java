package com.spacework.controller;

import com.spacework.model.Descuento;
import com.spacework.dao.DescuentoDAO;
import java.util.Date;
import java.util.List;

/**
 * Controlador para gestión de Descuentos
 * Maneja la lógica de negocio relacionada con promociones y descuentos
 */
public class DescuentoController {
    
    private DescuentoDAO descuentoDAO;

    public DescuentoController() {
        this.descuentoDAO = new DescuentoDAO();
    }

    /**
     * Registra un nuevo descuento
     */
    public boolean registrarDescuento(String codigo, String descripcion, double porcentaje,
                                     double montoMinimo, Date fechaInicio, Date fechaFin,
                                     int usosMaximos) {
        if (porcentaje < 0 || porcentaje > 100) {
            System.out.println("Error: Porcentaje debe estar entre 0 y 100");
            return false;
        }
        
        Descuento desc = new Descuento();
        desc.setCodigo(codigo.toUpperCase());
        desc.setDescripcion(descripcion);
        desc.setPorcentaje(porcentaje);
        desc.setMontoMinimo(montoMinimo);
        desc.setFechaInicio(fechaInicio);
        desc.setFechaFin(fechaFin);
        desc.setUsosMaximos(usosMaximos);
        desc.setUsosActuales(0);
        desc.setEstado("ACTIVO");
        
        return descuentoDAO.insertar(desc);
    }

    /**
     * Obtiene todos los descuentos
     */
    public List<Descuento> obtenerTodos() {
        return descuentoDAO.listar();
    }

    /**
     * Busca un descuento por código
     */
    public Descuento obtenerPorCodigo(String codigo) {
        return descuentoDAO.buscarPorCodigo(codigo.toUpperCase());
    }

    /**
     * Busca un descuento por ID
     */
    public Descuento obtenerPorId(int idDescuento) {
        return descuentoDAO.buscarPorId(idDescuento);
    }

    /**
     * Valida si un código puede ser aplicado a un monto
     */
    public boolean validarCodigo(String codigo, double monto) {
        return descuentoDAO.validarCodigo(codigo.toUpperCase(), monto);
    }

    /**
     * Aplica un descuento a un monto
     */
    public double aplicarDescuento(String codigo, double monto) {
        Descuento desc = obtenerPorCodigo(codigo);
        if (desc != null && validarCodigo(codigo, monto)) {
            double descuentoAmount = monto * (desc.getPorcentaje() / 100.0);
            descuentoDAO.incrementarUsos(desc.getIdDescuento());
            return monto - descuentoAmount;
        }
        return monto;
    }

    /**
     * Actualiza un descuento
     */
    public boolean actualizarDescuento(int idDescuento, String descripcion, double porcentaje,
                                      double montoMinimo, Date fechaFin) {
        Descuento desc = descuentoDAO.buscarPorId(idDescuento);
        if (desc != null) {
            desc.setDescripcion(descripcion);
            desc.setPorcentaje(porcentaje);
            desc.setMontoMinimo(montoMinimo);
            desc.setFechaFin(fechaFin);
            return descuentoDAO.actualizar(desc);
        }
        return false;
    }

    /**
     * Desactiva un descuento
     */
    public boolean desactivarDescuento(int idDescuento) {
        return descuentoDAO.desactivar(idDescuento);
    }
}
