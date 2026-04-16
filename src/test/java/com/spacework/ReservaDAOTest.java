package com.spacework;

import com.spacework.dao.ClienteDAO;
import com.spacework.dao.EspacioDAO;
import com.spacework.dao.ReservaDAO;
import com.spacework.model.Cliente;
import com.spacework.model.Espacio;
import com.spacework.model.Reserva;
import org.junit.Test;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class ReservaDAOTest {

    private final ReservaDAO  reservaDAO  = new ReservaDAO();
    private final ClienteDAO  clienteDAO  = new ClienteDAO();
    private final EspacioDAO  espacioDAO  = new EspacioDAO();

    /**
     * Obtiene el primer cliente y espacio activos para usar en pruebas.
     * Si no hay datos, los tests se omiten silenciosamente.
     */
    private Cliente primerCliente() throws SQLException {
        List<Cliente> lista = clienteDAO.listar();
        return lista.isEmpty() ? null : lista.get(0);
    }

    private Espacio primerEspacio() throws SQLException {
        List<Espacio> lista = espacioDAO.listar();
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Test
    public void testInsertarYListar() throws SQLException {
        Cliente c = primerCliente();
        Espacio e = primerEspacio();
        if (c == null || e == null) return; // Sin datos de referencia, omitir

        // Fechas en el futuro lejano para no solapar con otras reservas
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 5);
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date inicio = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, 2);
        Date fin = cal.getTime();

        Reserva r = new Reserva();
        r.setCliente(c);
        r.setEspacio(e);
        r.setFechaInicio(inicio);
        r.setFechaFin(fin);
        r.setMontoTotal(e.getPrecioPorHora() * 2);

        reservaDAO.insertar(r);

        List<Reserva> lista = reservaDAO.listarPorCliente(c.getIdCliente());
        assertFalse("Debe haber al menos una reserva para el cliente", lista.isEmpty());
    }

    @Test
    public void testVerificarDisponibilidad() throws SQLException {
        Espacio e = primerEspacio();
        if (e == null) return;

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 10);
        cal.set(Calendar.HOUR_OF_DAY, 14);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Timestamp inicio = new Timestamp(cal.getTimeInMillis());
        cal.add(Calendar.HOUR_OF_DAY, 1);
        Timestamp fin = new Timestamp(cal.getTimeInMillis());

        boolean disponible = reservaDAO.verificarDisponibilidad(e.getIdEspacio(), inicio, fin);
        assertTrue("El espacio debe estar disponible en un rango de tiempo futuro sin reservas", disponible);
    }

    @Test
    public void testCambiarEstado() throws SQLException {
        Cliente c = primerCliente();
        Espacio e = primerEspacio();
        if (c == null || e == null) return;

        // Insertar reserva de prueba
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 6);
        cal.set(Calendar.HOUR_OF_DAY, 10);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date inicio = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        Date fin = cal.getTime();

        Reserva r = new Reserva();
        r.setCliente(c);
        r.setEspacio(e);
        r.setFechaInicio(inicio);
        r.setFechaFin(fin);
        r.setMontoTotal(e.getPrecioPorHora());
        reservaDAO.insertar(r);

        // Obtener la reserva recién creada
        List<Reserva> lista = reservaDAO.listarPorCliente(c.getIdCliente());
        assertFalse(lista.isEmpty());
        Reserva insertada = lista.get(0); // la más reciente (orden DESC)

        reservaDAO.cambiarEstado(insertada.getIdReserva(), "CONFIRMADA");

        // Verificar cambiando a cancelada
        reservaDAO.cambiarEstado(insertada.getIdReserva(), "CANCELADA");

        List<Reserva> listaPost = reservaDAO.listarPorCliente(c.getIdCliente());
        Reserva modificada = listaPost.stream()
                .filter(res -> res.getIdReserva() == insertada.getIdReserva())
                .findFirst().orElse(null);
        assertNotNull(modificada);
        assertEquals("CANCELADA", modificada.getEstado());
    }
}
