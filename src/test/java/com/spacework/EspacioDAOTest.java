package com.spacework;

import com.spacework.dao.EspacioDAO;
import com.spacework.model.Espacio;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class EspacioDAOTest {

    private final EspacioDAO dao = new EspacioDAO();

    @Test
    public void testInsertarYListar() throws SQLException {
        Espacio e = new Espacio();
        e.setNombre("Sala Test JUnit");
        e.setTipo("SALA_REUNION");
        e.setCapacidad(10);
        e.setUbicacion("Piso 1");
        e.setPrecioPorHora(50.0);

        dao.insertar(e);

        List<Espacio> lista = dao.listar();
        boolean encontrado = lista.stream()
                .anyMatch(esp -> "Sala Test JUnit".equals(esp.getNombre()));
        assertTrue("El espacio insertado debe aparecer en la lista", encontrado);
    }

    @Test
    public void testActualizar() throws SQLException {
        // Requiere un espacio existente con id=1. Ajusta el id según tus datos de prueba.
        List<Espacio> lista = dao.listar();
        if (lista.isEmpty()) return; // Sin datos, omitir

        Espacio e = lista.get(0);
        String nombreOriginal = e.getNombre();
        e.setNombre(nombreOriginal + " (editado)");
        dao.actualizar(e);

        Espacio actualizado = dao.buscarPorId(e.getIdEspacio());
        assertNotNull(actualizado);
        assertTrue(actualizado.getNombre().contains("(editado)"));

        // Restaurar
        e.setNombre(nombreOriginal);
        dao.actualizar(e);
    }

    @Test
    public void testDesactivar() throws SQLException {
        // Inserta uno temporal y lo desactiva
        Espacio e = new Espacio();
        e.setNombre("Espacio Temporal Borrar");
        e.setTipo("OFICINA");
        e.setCapacidad(5);
        e.setUbicacion("Test");
        e.setPrecioPorHora(30.0);
        dao.insertar(e);

        // Buscar el recién insertado
        List<Espacio> lista = dao.listar();
        Espacio insertado = lista.stream()
                .filter(esp -> "Espacio Temporal Borrar".equals(esp.getNombre()))
                .findFirst().orElse(null);
        assertNotNull("Debe encontrarse el espacio insertado", insertado);

        dao.desactivar(insertado.getIdEspacio());

        // Ya no debe aparecer en la lista de activos
        List<Espacio> listaPost = dao.listar();
        boolean sigue = listaPost.stream()
                .anyMatch(esp -> esp.getIdEspacio() == insertado.getIdEspacio());
        assertFalse("El espacio desactivado no debe aparecer en la lista", sigue);
    }
}
