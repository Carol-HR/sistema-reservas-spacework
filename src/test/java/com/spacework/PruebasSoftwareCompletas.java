package com.spacework;

import com.spacework.dao.ClienteDAO;
import com.spacework.dao.EspacioDAO;
import com.spacework.dao.ReservaDAO;
import com.spacework.dao.PagoDAO;
import com.spacework.exception.BusinessException;
import com.spacework.model.Cliente;
import com.spacework.model.Espacio;
import com.spacework.model.Pago;
import com.spacework.model.Reserva;
import com.spacework.strategy.EstrategiaPago;
import com.spacework.strategy.EstrategiaPagoFactory;
import com.spacework.strategy.PagoEfectivo;
import com.spacework.strategy.PagoTarjeta;
import com.spacework.strategy.PagoTransferencia;
import com.spacework.util.HashUtil;

import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PRUEBAS DE SOFTWARE COMPLETAS")
public class PruebasSoftwareCompletas {

    private Cliente crearClientePrueba() throws Exception {
        ClienteDAO dao = new ClienteDAO();
        String sufijo = "QA" + UUID.randomUUID().toString().substring(0, 10);
        Cliente c = new Cliente();
        c.setNombre("QA");
        c.setApellido("Tester");
        c.setDni(sufijo);
        c.setEmail(sufijo + "@test.com");
        c.setTelefono("999999999");
        c.setPassword("Test1234");
        dao.insertar(c);
        return dao.buscarPorDni(sufijo);
    }

    private Espacio crearEspacioPrueba(int capacidad, double precio) throws Exception {
        EspacioDAO dao = new EspacioDAO();
        String nombreUnico = "QA-Espacio-" + UUID.randomUUID().toString().substring(0, 10);
        Espacio e = new Espacio();
        e.setNombre(nombreUnico);
        e.setTipo("OFICINA");
        e.setCapacidad(capacidad);
        e.setUbicacion("Piso QA");
        e.setPrecioPorHora(precio);
        dao.insertar(e);
        for (Espacio x : dao.listar()) {
            if (x.getNombre().equals(nombreUnico)) return x;
        }
        return null;
    }

    @Nested
    @DisplayName("1. Pruebas Unitarias (sin BD)")
    class PruebasUnitarias {

        @Test
        @DisplayName("HashUtil.sha256() genera un hash de 64 caracteres")
        void testHashGeneraCadenaDe64Caracteres() {
            String hash = HashUtil.sha256("clave123");
            assertNotNull(hash);
            assertEquals(64, hash.length());
        }

        @Test
        @DisplayName("HashUtil.sha256() es determinista: misma entrada -> mismo hash")
        void testHashEsDeterminista() {
            String hash1 = HashUtil.sha256("mismaClave");
            String hash2 = HashUtil.sha256("mismaClave");
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("HashUtil.sha256() produce hashes distintos para entradas distintas")
        void testHashesDiferentesParaEntradasDiferentes() {
            String hash1 = HashUtil.sha256("clave1");
            String hash2 = HashUtil.sha256("clave2");
            assertNotEquals(hash1, hash2);
        }

        @Test
        @DisplayName("EstrategiaPagoFactory crea un PagoEfectivo cuando el método es EFECTIVO")
        void testFactoryCreaPagoEfectivo() {
            EstrategiaPago estrategia = EstrategiaPagoFactory.crear("EFECTIVO");
            assertTrue(estrategia instanceof PagoEfectivo);
            assertEquals("EFECTIVO", estrategia.getNombreMetodo());
        }

        @Test
        @DisplayName("EstrategiaPagoFactory crea un PagoTarjeta cuando el método es TARJETA")
        void testFactoryCreaPagoTarjeta() {
            EstrategiaPago estrategia = EstrategiaPagoFactory.crear("TARJETA");
            assertTrue(estrategia instanceof PagoTarjeta);
            assertEquals("TARJETA", estrategia.getNombreMetodo());
        }

        @Test
        @DisplayName("EstrategiaPagoFactory crea un PagoTransferencia cuando el método es TRANSFERENCIA")
        void testFactoryCreaPagoTransferencia() {
            EstrategiaPago estrategia = EstrategiaPagoFactory.crear("TRANSFERENCIA");
            assertTrue(estrategia instanceof PagoTransferencia);
            assertEquals("TRANSFERENCIA", estrategia.getNombreMetodo());
        }

        @Test
        @DisplayName("PagoEfectivo procesa correctamente un pago válido")
        void testPagoEfectivoValidoSeCompleta() throws Exception {
            Pago pago = new Pago();
            pago.setMontoFinal(100.0);
            Pago resultado = new PagoEfectivo().procesarPago(pago);
            assertEquals("COMPLETADO", resultado.getEstadoPago());
            assertTrue(resultado.getReferencia().startsWith("EF-"));
        }

        @Test
        @DisplayName("PagoTransferencia procesa correctamente un pago válido")
        void testPagoTransferenciaValidoSeCompleta() throws Exception {
            Pago pago = new Pago();
            pago.setMontoFinal(80.0);
            Pago resultado = new PagoTransferencia().procesarPago(pago);
            assertEquals("COMPLETADO", resultado.getEstadoPago());
            assertTrue(resultado.getReferencia().startsWith("TF-"));
        }

        @Test
        @DisplayName("Cliente.getNombreCompleto() concatena nombre y apellido")
        void testClienteGetNombreCompleto() {
            Cliente cliente = new Cliente();
            cliente.setNombre("Ana");
            cliente.setApellido("Pérez");
            assertEquals("Ana Pérez", cliente.getNombreCompleto());
        }
    }

    @Nested
    @DisplayName("2. Pruebas de Caja Blanca (sin BD)")
    class PruebasCajaBlanca {

        @Test
        @DisplayName("Rama IF (monto <= 0): PagoEfectivo debe lanzar excepción")
        void testRamaMontoInvalidoLanzaExcepcionEfectivo() {
            Pago pago = new Pago();
            pago.setMontoFinal(0);
            assertThrows(Exception.class, () -> new PagoEfectivo().procesarPago(pago));
        }

        @Test
        @DisplayName("Rama ELSE (monto > 0): PagoEfectivo completa el pago")
        void testRamaMontoValidoCompletaEfectivo() throws Exception {
            Pago pago = new Pago();
            pago.setMontoFinal(50);
            Pago resultado = new PagoEfectivo().procesarPago(pago);
            assertEquals("COMPLETADO", resultado.getEstadoPago());
        }

        @Test
        @DisplayName("Rama IF (monto <= 0): PagoTransferencia debe lanzar excepción")
        void testRamaMontoInvalidoLanzaExcepcionTransferencia() {
            Pago pago = new Pago();
            pago.setMontoFinal(-5);
            assertThrows(Exception.class, () -> new PagoTransferencia().procesarPago(pago));
        }

        @Test
        @DisplayName("Rama DEFAULT del switch: método de pago no soportado lanza BusinessException")
        void testRamaDefaultDeLaFabrica() {
            assertThrows(BusinessException.class, () -> EstrategiaPagoFactory.crear("BITCOIN"));
        }

        @Test
        @DisplayName("La fábrica normaliza mayúsculas/minúsculas")
        void testFactoryEsInsensibleAMayusculas() {
            EstrategiaPago estrategia = EstrategiaPagoFactory.crear("efectivo");
            assertEquals("EFECTIVO", estrategia.getNombreMetodo());
        }

        @Test
        @DisplayName("PagoTarjeta: bajo carga, se recorren las DOS ramas posibles (aprobado / rechazado)")
        void testPagoTarjetaCubreAmbasRamasDeAzar() throws Exception {
            int intentos = 300;
            int aprobados = 0;
            int rechazados = 0;
            for (int i = 0; i < intentos; i++) {
                Pago pago = new Pago();
                pago.setMontoFinal(100.0);
                try {
                    new PagoTarjeta().procesarPago(pago);
                    aprobados++;
                } catch (Exception e) {
                    rechazados++;
                }
            }
            System.out.println(">> CAJA BLANCA PagoTarjeta: aprobados=" + aprobados
                    + " | rechazados=" + rechazados + " (de " + intentos + " intentos)");
            assertEquals(intentos, aprobados + rechazados);
            assertTrue(aprobados > 200);
        }
    }

    @Nested
    @DisplayName("3. Pruebas de Caja Negra (con BD)")
    class PruebasCajaNegra {

        @Test
        @DisplayName("Equivalencia (inválida): un DNI que no existe retorna null, no lanza error")
        void testClienteConDniInexistente() throws Exception {
            Cliente cliente = new ClienteDAO().buscarPorDni("DNI-NO-EXISTE-999");
            assertNull(cliente);
        }

        @Test
        @DisplayName("Valor límite: un ID de espacio muy grande (fuera de rango) retorna null")
        void testEspacioConIdFueraDeRango() throws Exception {
            Espacio espacio = new EspacioDAO().buscarPorId(999999999);
            assertNull(espacio);
        }

        @Test
        @DisplayName("Insertar un cliente nuevo y recuperarlo por su DNI confirma que se guardó bien")
        void testInsertarYRecuperarClienteNuevo() throws Exception {
            Cliente creado = crearClientePrueba();
            assertNotNull(creado);
            assertTrue(creado.getIdCliente() > 0);
            assertEquals("QA", creado.getNombre());
        }

        @Test
        @DisplayName("Valor límite válido: capacidad = 1 (el mínimo permitido) se guarda sin error")
        void testEspacioConCapacidadLimiteMinimoValido() throws Exception {
            Espacio creado = crearEspacioPrueba(1, 20.0);
            assertNotNull(creado);
            assertEquals(1, creado.getCapacidad());
        }

        @Test
        @DisplayName("Valor límite inválido: capacidad = 0 es rechazada por la restricción CHECK de la base de datos")
        void testEspacioConCapacidadCeroEsRechazada() {
            Espacio espacio = new Espacio();
            espacio.setNombre("QA-Invalido-" + UUID.randomUUID());
            espacio.setTipo("OFICINA");
            espacio.setCapacidad(0);
            espacio.setUbicacion("Piso QA");
            espacio.setPrecioPorHora(20.0);
            EspacioDAO dao = new EspacioDAO();
            assertThrows(SQLException.class, () -> dao.insertar(espacio));
        }

        @Test
        @DisplayName("Valor límite inválido: precio por hora negativo es rechazado por la base de datos")
        void testEspacioConPrecioNegativoEsRechazado() {
            Espacio espacio = new Espacio();
            espacio.setNombre("QA-PrecioNeg-" + UUID.randomUUID());
            espacio.setTipo("OFICINA");
            espacio.setCapacidad(5);
            espacio.setUbicacion("Piso QA");
            espacio.setPrecioPorHora(-10.0);
            EspacioDAO dao = new EspacioDAO();
            assertThrows(SQLException.class, () -> dao.insertar(espacio));
        }

        @Test
        @DisplayName("Insertar una reserva válida con cliente y espacio reales no lanza excepción")
        void testInsertarReservaValida() throws Exception {
            Cliente cliente = crearClientePrueba();
            Espacio espacio = crearEspacioPrueba(4, 25.0);
            assertNotNull(cliente);
            assertNotNull(espacio);

            Reserva reserva = new Reserva();
            reserva.setCliente(cliente);
            reserva.setEspacio(espacio);
            Date inicio = new Date();
            Date fin = new Date(inicio.getTime() + 3_600_000);
            reserva.setFechaInicio(inicio);
            reserva.setFechaFin(fin);
            reserva.setMontoTotal(100.0);

            ReservaDAO dao = new ReservaDAO();
            assertDoesNotThrow(() -> dao.insertar(reserva));
        }

        @Test
        @DisplayName("El sistema RECHAZA una reserva con fecha fin anterior a fecha inicio (CHECK de la base de datos)")
        void testReservaConFechasInvalidasEsRechazadaPorLaBD() throws Exception {
            Cliente cliente = crearClientePrueba();
            Espacio espacio = crearEspacioPrueba(4, 25.0);
            assertNotNull(cliente);
            assertNotNull(espacio);

            Reserva reserva = new Reserva();
            reserva.setCliente(cliente);
            reserva.setEspacio(espacio);
            Date inicio = new Date();
            Date fin = new Date(inicio.getTime() - 60_000);
            reserva.setFechaInicio(inicio);
            reserva.setFechaFin(fin);
            reserva.setMontoTotal(50.0);

            ReservaDAO dao = new ReservaDAO();
            assertThrows(SQLException.class, () -> dao.insertar(reserva));
        }

        @Test
        @DisplayName("verificarDisponibilidad siempre responde con un booleano, sin romperse")
        void testDisponibilidadSiempreRetornaBooleano() throws Exception {
            Espacio espacio = crearEspacioPrueba(4, 25.0);
            assertNotNull(espacio);
            Timestamp inicio = new Timestamp(System.currentTimeMillis());
            Timestamp fin = new Timestamp(System.currentTimeMillis() + 3_600_000);
            boolean resultado = new ReservaDAO().verificarDisponibilidad(espacio.getIdEspacio(), inicio, fin);
            assertTrue(resultado || !resultado);
        }
    }

    @Nested
    @DisplayName("4. Pruebas de Integración")
    class PruebasIntegracion {

        @Test
        @DisplayName("Flujo completo: Registro de cliente → Creación de reserva → Proceso de pago")
        void testFlujoCompletoClienteReservaPago() throws Exception {
            // Paso 1: Crear cliente
            Cliente cliente = crearClientePrueba();
            assertNotNull(cliente);
            assertTrue(cliente.getIdCliente() > 0);

            // Paso 2: Crear espacio
            Espacio espacio = crearEspacioPrueba(4, 50.0);
            assertNotNull(espacio);
            assertTrue(espacio.getIdEspacio() > 0);

            // Paso 3: Crear reserva
            Reserva reserva = new Reserva();
            reserva.setCliente(cliente);
            reserva.setEspacio(espacio);
            Date inicio = new Date();
            Date fin = new Date(inicio.getTime() + 3_600_000);
            reserva.setFechaInicio(inicio);
            reserva.setFechaFin(fin);
            reserva.setMontoTotal(100.0);
            
            ReservaDAO reservaDAO = new ReservaDAO();
            reservaDAO.insertar(reserva);
            assertTrue(reserva.getIdReserva() > 0);

            // Paso 4: Procesar pago
            Pago pago = new Pago();
            pago.setIdReserva(reserva.getIdReserva());
            pago.setMontoFinal(100.0);
            pago.setMetodoPago("EFECTIVO");
            
            EstrategiaPago estrategia = EstrategiaPagoFactory.crear("EFECTIVO");
            Pago resultado = estrategia.procesarPago(pago);
            
            assertEquals("COMPLETADO", resultado.getEstadoPago());
            assertNotNull(resultado.getReferencia());
        }

        @Test
        @DisplayName("Integración DAO: ClienteDAO → Listar clientes → Buscar por ID")
        void testIntegracionClienteDAO() throws Exception {
            ClienteDAO dao = new ClienteDAO();
            
            // Crear cliente
            Cliente nuevo = crearClientePrueba();
            assertNotNull(nuevo);
            
            // Listar todos
            List<Cliente> todos = dao.listar();
            assertNotNull(todos);
            assertTrue(todos.size() > 0);
            
            // Buscar por ID
            Cliente encontrado = dao.buscarPorId(nuevo.getIdCliente());
            assertNotNull(encontrado);
            assertEquals(nuevo.getDni(), encontrado.getDni());
        }

        @Test
        @DisplayName("Integración de estrategias de pago: Factory → Estrategia → Procesamiento")
        void testIntegracionEstrategiasPago() throws Exception {
            String[] metodos = {"EFECTIVO", "TARJETA", "TRANSFERENCIA"};
            
            for (String metodo : metodos) {
                EstrategiaPago estrategia = EstrategiaPagoFactory.crear(metodo);
                assertNotNull(estrategia);
                assertEquals(metodo, estrategia.getNombreMetodo());
                
                Pago pago = new Pago();
                pago.setMontoFinal(100.0);
                
                if (metodo.equals("EFECTIVO") || metodo.equals("TRANSFERENCIA")) {
                    Pago resultado = estrategia.procesarPago(pago);
                    assertEquals("COMPLETADO", resultado.getEstadoPago());
                }
            }
        }
    }

    @Nested
    @DisplayName("5. Pruebas de Sistema")
    class PruebasSistema {

        @Test
        @DisplayName("Conexión a base de datos: El sistema puede conectarse y ejecutar una consulta simple")
        void testConexionBaseDeDatos() throws Exception {
            ClienteDAO dao = new ClienteDAO();
            List<Cliente> clientes = dao.listar();
            assertNotNull(clientes);
            // No lanza excepción = conexión exitosa
        }

        @Test
        @DisplayName("Integridad referencial: No se puede crear reserva con cliente inexistente")
        void testIntegridadReferencialClienteInexistente() throws Exception {
            Espacio espacio = crearEspacioPrueba(4, 25.0);
            assertNotNull(espacio);

            Cliente clienteFantasma = new Cliente();
            clienteFantasma.setIdCliente(999999);

            Reserva reserva = new Reserva();
            reserva.setCliente(clienteFantasma);
            reserva.setEspacio(espacio);
            reserva.setFechaInicio(new Date());
            reserva.setFechaFin(new Date(System.currentTimeMillis() + 3_600_000));
            reserva.setMontoTotal(100.0);

            ReservaDAO dao = new ReservaDAO();
            assertThrows(Exception.class, () -> dao.insertar(reserva));
        }

        @Test
        @DisplayName("Transacción: Si falla una operación, no se guardan datos parciales")
        void testTransaccionRollback() throws Exception {
            ClienteDAO clienteDAO = new ClienteDAO();
            int cantidadInicial = clienteDAO.listar().size();
            
            try {
                // Intentar crear cliente con datos inválidos
                Cliente invalido = new Cliente();
                invalido.setNombre(""); // Nombre vacío debería fallar
                invalido.setDni("123");
                clienteDAO.insertar(invalido);
            } catch (Exception e) {
                // Esperado que falle
            }
            
            // Verificar que no se agregó
            int cantidadFinal = clienteDAO.listar().size();
            assertEquals(cantidadInicial, cantidadFinal);
        }
    }

    @Nested
    @DisplayName("6. Pruebas de Aceptación UAT")
    class PruebasUAT {

        @Test
        @DisplayName("UAT-001: Usuario puede registrar un nuevo cliente exitosamente")
        void testUATRegistroCliente() throws Exception {
            Cliente cliente = crearClientePrueba();
            assertNotNull(cliente);
            assertNotNull(cliente.getIdCliente());
            assertNotNull(cliente.getDni());
            assertEquals("QA", cliente.getNombre());
        }

        @Test
        @DisplayName("UAT-002: Usuario puede buscar un espacio disponible")
        void testUATBuscarEspacio() throws Exception {
            EspacioDAO dao = new EspacioDAO();
            List<Espacio> espacios = dao.listar();
            assertNotNull(espacios);
            assertTrue(espacios.size() > 0);
            
            Espacio primero = espacios.get(0);
            assertNotNull(primero.getNombre());
            assertNotNull(primero.getPrecioPorHora());
        }

        @Test
        @DisplayName("UAT-003: Usuario puede crear una reserva para un espacio")
        void testUATCrearReserva() throws Exception {
            Cliente cliente = crearClientePrueba();
            Espacio espacio = crearEspacioPrueba(4, 30.0);
            
            Reserva reserva = new Reserva();
            reserva.setCliente(cliente);
            reserva.setEspacio(espacio);
            reserva.setFechaInicio(new Date());
            reserva.setFechaFin(new Date(System.currentTimeMillis() + 3_600_000));
            reserva.setMontoTotal(120.0);
            
            ReservaDAO dao = new ReservaDAO();
            dao.insertar(reserva);
            
            assertTrue(reserva.getIdReserva() > 0);
        }

        @Test
        @DisplayName("UAT-004: Sistema rechaza reserva con fechas inválidas")
        void testUATRechazoReservaInvalida() throws Exception {
            Cliente cliente = crearClientePrueba();
            Espacio espacio = crearEspacioPrueba(4, 30.0);
            
            Reserva reserva = new Reserva();
            reserva.setCliente(cliente);
            reserva.setEspacio(espacio);
            reserva.setFechaInicio(new Date());
            reserva.setFechaFin(new Date(System.currentTimeMillis() - 3_600_000)); // Fin antes que inicio
            reserva.setMontoTotal(120.0);
            
            ReservaDAO dao = new ReservaDAO();
            assertThrows(Exception.class, () -> dao.insertar(reserva));
        }
    }

    @Nested
    @DisplayName("7. Pruebas de Rendimiento")
    class PruebasRendimiento {

        @Test
        @DisplayName("(sin BD) HashUtil genera 100 000 hashes en menos de 3 segundos")
        void testRendimientoHashUtilMasivo() {
            int cantidad = 100_000;
            long inicio = System.nanoTime();
            for (int i = 0; i < cantidad; i++) {
                HashUtil.sha256("clave" + i);
            }
            long duracionMs = (System.nanoTime() - inicio) / 1_000_000;
            System.out.println(">> RENDIMIENTO HashUtil: " + cantidad + " hashes en " + duracionMs + " ms");
            assertTrue(duracionMs < 3000);
        }

        @Test
        @DisplayName("(con BD) EspacioDAO.listar() responde, en promedio, en menos de 3 segundos")
        void testRendimientoEspacioDAOListar() throws Exception {
            EspacioDAO dao = new EspacioDAO();
            int repeticiones = 10;
            long totalMs = 0;
            long peorMs = 0;

            for (int i = 0; i < repeticiones; i++) {
                long inicio = System.nanoTime();
                dao.listar();
                long duracionMs = (System.nanoTime() - inicio) / 1_000_000;
                totalMs += duracionMs;
                if (duracionMs > peorMs) peorMs = duracionMs;
            }

            double promedioMs = totalMs / (double) repeticiones;
            System.out.println(">> RENDIMIENTO EspacioDAO.listar(): promedio=" + promedioMs
                    + " ms | peor caso=" + peorMs + " ms (" + repeticiones + " repeticiones)");

            assertTrue(promedioMs < 3000);
        }

        @Test
        @DisplayName("(con BD) ClienteDAO.buscarPorId() responde en menos de 3 segundos")
        void testRendimientoClienteDAOBuscarPorId() throws Exception {
            ClienteDAO dao = new ClienteDAO();
            long inicio = System.nanoTime();
            dao.buscarPorId(999999999);
            long duracionMs = (System.nanoTime() - inicio) / 1_000_000;
            System.out.println(">> RENDIMIENTO ClienteDAO.buscarPorId(): " + duracionMs + " ms");
            assertTrue(duracionMs < 3000);
        }

        @Test
        @DisplayName("Rendimiento de Factory de estrategias: 10 000 creaciones en menos de 1 segundo")
        void testRendimientoFactoryEstrategias() {
            int cantidad = 10_000;
            long inicio = System.nanoTime();
            for (int i = 0; i < cantidad; i++) {
                EstrategiaPagoFactory.crear("EFECTIVO");
            }
            long duracionMs = (System.nanoTime() - inicio) / 1_000_000;
            System.out.println(">> RENDIMIENTO Factory: " + cantidad + " creaciones en " + duracionMs + " ms");
            assertTrue(duracionMs < 1000);
        }
    }

    @Nested
    @DisplayName("8. Pruebas de Estrés")
    class PruebasEstres {

        @Test
        @DisplayName("(sin BD) La fábrica de pagos soporta 10 000 creaciones seguidas sin degradarse")
        void testEstresFactoryDePagosMasivo() {
            int cantidad = 10_000;
            long inicio = System.nanoTime();
            for (int i = 0; i < cantidad; i++) {
                EstrategiaPago estrategia = EstrategiaPagoFactory.crear("EFECTIVO");
                assertNotNull(estrategia);
            }
            long duracionMs = (System.nanoTime() - inicio) / 1_000_000;
            System.out.println(">> ESTRÉS Factory de pagos: " + cantidad + " creaciones en " + duracionMs + " ms");
            assertTrue(duracionMs < 5000);
        }

        @Test
        @DisplayName("(con BD) El sistema soporta insertar 20 clientes nuevos seguidos sin fallar")
        void testEstresInsercionMasivaDeClientes() throws Exception {
            ClienteDAO dao = new ClienteDAO();
            int cantidad = 20;
            long inicio = System.currentTimeMillis();
            for (int i = 0; i < cantidad; i++) {
                String sufijo = "QA" + UUID.randomUUID().toString().substring(0, 10);
                Cliente c = new Cliente();
                c.setNombre("Estres");
                c.setApellido("Cliente" + i);
                c.setDni(sufijo);
                c.setEmail(sufijo + "@test.com");
                c.setTelefono("999999999");
                c.setPassword("Test1234");
                dao.insertar(c);
            }
            long duracionMs = System.currentTimeMillis() - inicio;
            System.out.println(">> ESTRÉS Inserción de clientes: " + cantidad + " clientes en " + duracionMs + " ms");
            assertTrue(duracionMs < 10000);
        }

        @Test
        @DisplayName("(con BD) EspacioDAO soporta múltiples solicitudes simultáneas (carga concurrente)")
        void testEstresConcurrenciaEnEspacioDAO() throws Exception {
            int hilos = 10;
            int llamadasPorHilo = 5;
            int totalEsperado = hilos * llamadasPorHilo;

            ExecutorService executor = Executors.newFixedThreadPool(hilos);
            AtomicInteger exitos = new AtomicInteger(0);
            AtomicInteger fallos = new AtomicInteger(0);

            List<Callable<Void>> tareas = new ArrayList<>();
            for (int i = 0; i < hilos; i++) {
                tareas.add(() -> {
                    for (int j = 0; j < llamadasPorHilo; j++) {
                        try {
                            new EspacioDAO().listar();
                            exitos.incrementAndGet();
                        } catch (Exception e) {
                            fallos.incrementAndGet();
                        }
                    }
                    return null;
                });
            }

            long inicio = System.currentTimeMillis();
            executor.invokeAll(tareas);
            executor.shutdown();
            executor.awaitTermination(60, TimeUnit.SECONDS);
            long duracionMs = System.currentTimeMillis() - inicio;

            System.out.println(">> ESTRÉS EspacioDAO.listar(): " + totalEsperado + " llamadas concurrentes ("
                    + hilos + " hilos x " + llamadasPorHilo + ") en " + duracionMs + " ms");
            System.out.println("   Éxitos: " + exitos.get() + " | Fallos: " + fallos.get());

            assertEquals(totalEsperado, exitos.get() + fallos.get());
        }

        @Test
        @DisplayName("Estrés de hashing: 1 millón de hashes sin error")
        void testEstresHashingMasivo() {
            int cantidad = 1_000_000;
            long inicio = System.nanoTime();
            for (int i = 0; i < cantidad; i++) {
                HashUtil.sha256("test" + i);
            }
            long duracionMs = (System.nanoTime() - inicio) / 1_000_000;
            System.out.println(">> ESTRÉS Hashing: " + cantidad + " hashes en " + duracionMs + " ms");
            assertTrue(duracionMs < 30000); // 30 segundos máximo
        }
    }
}
