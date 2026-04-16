package com.spacework;

import com.spacework.dao.ClienteDAO;
import com.spacework.model.Cliente;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

public class ClienteDAOTest {

    private final ClienteDAO dao = new ClienteDAO();

    @Test
    public void testInsertarYBuscarPorDni() throws SQLException {
        // Usar un DNI que no exista en la BD
        String dniPrueba = "00000001";

        // Si ya existe de una prueba anterior, omitir inserción
        Cliente existente = dao.buscarPorDni(dniPrueba);
        if (existente == null) {
            Cliente c = new Cliente();
            c.setNombre("Juan");
            c.setApellido("Prueba");
            c.setDni(dniPrueba);
            c.setEmail("juan.prueba@test.com");
            c.setTelefono("999000001");
            dao.insertar(c);
        }

        Cliente encontrado = dao.buscarPorDni(dniPrueba);
        assertNotNull("El cliente debe existir tras la inserción", encontrado);
        assertEquals(dniPrueba, encontrado.getDni());
    }

    @Test
    public void testListar() throws SQLException {
        List<Cliente> lista = dao.listar();
        assertNotNull("La lista no debe ser null", lista);
    }

    @Test
    public void testActualizar() throws SQLException {
        List<Cliente> lista = dao.listar();
        if (lista.isEmpty()) return;

        Cliente c = lista.get(0);
        String emailOriginal = c.getEmail();
        c.setEmail("actualizado@test.com");
        dao.actualizar(c);

        Cliente actualizado = dao.buscarPorDni(c.getDni());
        assertNotNull(actualizado);
        assertEquals("actualizado@test.com", actualizado.getEmail());

        // Restaurar
        c.setEmail(emailOriginal);
        dao.actualizar(c);
    }

    @Test
    public void testDesactivar() throws SQLException {
        String dniTemp = "00000002";
        if (dao.buscarPorDni(dniTemp) == null) {
            Cliente c = new Cliente();
            c.setNombre("Temp");
            c.setApellido("Borrar");
            c.setDni(dniTemp);
            c.setEmail("temp@test.com");
            c.setTelefono("999000002");
            dao.insertar(c);
        }

        Cliente c = dao.buscarPorDni(dniTemp);
        assertNotNull(c);
        dao.desactivar(c.getIdCliente());

        // Tras desactivar, buscarPorDni con la consulta de activos debe retornar null
        // (buscarPorDni busca sin filtrar por estado, así que verificamos en la lista activa)
        List<Cliente> activos = dao.listar();
        boolean sigue = activos.stream().anyMatch(cli -> cli.getIdCliente() == c.getIdCliente());
        assertFalse("El cliente desactivado no debe aparecer en la lista de activos", sigue);
    }
}
