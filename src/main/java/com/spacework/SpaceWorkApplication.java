package com.spacework;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.spacework.controller.api.EvaluacionFormularioHandler;
import com.spacework.dao.*;
import com.spacework.model.*;
import com.spacework.util.HashUtil;
import com.spacework.util.EmailUtil;
import com.spacework.util.Conexion;
import java.sql.ResultSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.text.SimpleDateFormat;

/**
 * Servidor HTTP REST API - SpaceWork
 * Modo WEB ONLY - Sin interfaz gráfica de desktop
 * 
 * @author SpaceWork Dev Team
 * @version 2.0 (Web REST API)
 */
public class SpaceWorkApplication {

    private static final int PORT = 8080;
    private static final String STATIC_DIR = "target/classes/static";

    public static void main(String[] args) throws IOException {
        printBanner();
        
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", PORT), 0);
        
        // Ruta pública para evaluaciones desde email (sin login requerido)
        server.createContext("/evaluaciones/formulario", new EvaluacionFormularioHandler());
        
        // Ruta para archivos estáticos (CSS, JS, imágenes)
        server.createContext("/css/", new StaticFileHandler("css"));
        server.createContext("/js/", new StaticFileHandler("js"));
        server.createContext("/api/", new ApiHandler());
        
        // Ruta raíz - sirve index.html y SPA
        server.createContext("/", new RootHandler());
        
        server.setExecutor(null);
        server.start();
        sincronizarPagos();
        System.out.println("\n✅ Servidor WEB iniciado exitosamente");
        System.out.println("🌐 URL: http://localhost:" + PORT);
        System.out.println("📧 Evaluaciones: http://localhost:" + PORT + "/evaluaciones/formulario?token=xxx&calificacion=5");
        System.out.println("\n⏹️  Presiona Ctrl+C para detener\n");
    }

    private static void sincronizarPagos() {
        try {
            Connection conn = com.spacework.util.Conexion.getConexion();
            // Crear secuencia si no existe
            try {
                conn.createStatement().executeUpdate(
                    "CREATE SEQUENCE SEQ_PAGOS START WITH 100 INCREMENT BY 1 NOCACHE NOCYCLE");
                conn.commit();
                System.out.println("✅ Secuencia SEQ_PAGOS creada");
            } catch (Exception ignored) { /* ya existe */ }

            // Insertar pagos para reservas sin pago
            String sql = "INSERT INTO PAGOS (id_pago, id_reserva, monto, metodo_pago, estado_pago, fecha_pago, fecha_creacion) "
                       + "SELECT SEQ_PAGOS.NEXTVAL, r.id_reserva, r.monto_total, 'EFECTIVO', "
                       + "CASE r.estado WHEN 'COMPLETADA' THEN 'COMPLETADO' WHEN 'CANCELADA' THEN 'RECHAZADO' ELSE 'PENDIENTE' END, "
                       + "r.fecha_inicio, SYSDATE "
                       + "FROM RESERVAS r "
                       + "WHERE r.id_reserva NOT IN (SELECT DISTINCT id_reserva FROM PAGOS)";
            int rows = conn.createStatement().executeUpdate(sql);
            conn.commit();
            conn.close();
            if (rows > 0) System.out.println("✅ " + rows + " pagos sincronizados con reservas existentes");
        } catch (Exception e) {
            System.out.println("⚠️  Sincronización de pagos: " + e.getMessage());
        }
    }

    private static void printBanner() {
        System.out.println("\n╔═══════════════════════════════════════════════════╗");
        System.out.println("║     SPACEWORK - Sistema de Gestión de Reservas    ║");
        System.out.println("║           Servidor WEB - SPA (HTML5)              ║");
        System.out.println("║                                                   ║");
        System.out.println("║  🚀 Frontend: HTML5 + CSS3 + JavaScript            ║");
        System.out.println("║  📡 Backend: Java SE 8 + HttpServer               ║");
        System.out.println("║  💾 Base de Datos: Oracle 11g XE                  ║");
        System.out.println("║  ❌ Desktop UI: DESHABILITADO (Web Only)          ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
    }

    /**
     * Handler para archivos estáticos (CSS, JS)
     */
    static class StaticFileHandler implements HttpHandler {
        private String directory;
        
        public StaticFileHandler(String directory) {
            this.directory = directory;
        }
        
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            String filePath = STATIC_DIR + "/" + path.substring(1);
            
            File file = new File(filePath);
            
            if (file.exists() && file.isFile()) {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                
                // Detectar MIME type
                String mimeType = "text/plain";
                if (path.endsWith(".css")) mimeType = "text/css";
                else if (path.endsWith(".js")) mimeType = "application/javascript";
                else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) mimeType = "image/jpeg";
                else if (path.endsWith(".png")) mimeType = "image/png";
                else if (path.endsWith(".gif")) mimeType = "image/gif";
                
                exchange.getResponseHeaders().set("Content-Type", mimeType);
                // Sin cache para JS/CSS, permitir cache solo para imágenes
                if (path.endsWith(".js") || path.endsWith(".css")) {
                    exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
                    exchange.getResponseHeaders().set("Pragma", "no-cache");
                } else {
                    exchange.getResponseHeaders().set("Cache-Control", "max-age=3600");
                }
                exchange.sendResponseHeaders(200, fileContent.length);
                exchange.getResponseBody().write(fileContent);
            } else {
                exchange.sendResponseHeaders(404, -1);
            }
            exchange.close();
        }
    }

    /**
     * Handler para rutas API - Conecta con base de datos
     */
    static class ApiHandler implements HttpHandler {
        private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // CORS preflight
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();

            try {
                // AUTH
                if (path.equals("/api/auth/login") && method.equals("POST")) {
                    handleLogin(exchange);
                }
                // RESERVAS
                else if (path.equals("/api/reservas") && method.equals("GET")) {
                    handleGetReservas(exchange);
                } else if (path.equals("/api/reservas") && method.equals("POST")) {
                    handlePostReserva(exchange);
                } else if (path.matches("/api/reservas/\\d+/confirmar") && method.equals("PUT")) {
                    int id = Integer.parseInt(path.split("/")[3]);
                    Reserva reserva = new ReservaDAO().buscarPorId(id);
                    if (reserva == null) {
                        sendJson(exchange, 404, "{\"success\":false,\"error\":\"Reserva no encontrada\"}");
                        return;
                    }
                    // Solo cambiar a CONFIRMADA (sin crear pago)
                    new ReservaDAO().cambiarEstado(id, "CONFIRMADA");
                    sendJson(exchange, 200, "{\"success\":true}");
                } else if (path.matches("/api/reservas/\\d+/completar") && method.equals("PUT")) {
                    int id = Integer.parseInt(path.split("/")[3]);
                    Reserva reserva = new ReservaDAO().buscarPorId(id);
                    if (reserva == null) {
                        sendJson(exchange, 404, "{\"success\":false,\"error\":\"Reserva no encontrada\"}");
                        return;
                    }
                    // Cambiar a COMPLETADA y crear pago PENDIENTE
                    new ReservaDAO().cambiarEstado(id, "COMPLETADA");
                    Pago pago = new Pago();
                    pago.setIdReserva(id);
                    pago.setMonto(reserva.getMontoTotal());
                    pago.setMetodoPago("EFECTIVO"); // valor por defecto, el usuario lo cambia al pagar
                    pago.setEstadoPago("PENDIENTE");
                    new PagoDAO().insertar(pago);
                    sendJson(exchange, 200, "{\"success\":true}");
                } else if (path.matches("/api/reservas/\\d+") && method.equals("DELETE")) {
                    int id = Integer.parseInt(path.split("/")[3]);
                    new ReservaDAO().cambiarEstado(id, "CANCELADA");
                    sendJson(exchange, 200, "{\"success\":true}");
                }
                // ESPACIOS
                else if (path.equals("/api/espacios") && method.equals("GET")) {
                    handleGetEspacios(exchange);
                } else if (path.equals("/api/espacios") && method.equals("POST")) {
                    handlePostEspacio(exchange);
                } else if (path.matches("/api/espacios/\\d+") && method.equals("GET")) {
                    int id = Integer.parseInt(path.split("/")[3]);
                    Espacio e = new EspacioDAO().buscarPorId(id);
                    if (e != null) sendJson(exchange, 200, "{\"success\":true,\"data\":" + espacioToJson(e) + "}");
                    else sendJson(exchange, 404, "{\"success\":false,\"error\":\"No encontrado\"}");
                } else if (path.matches("/api/espacios/\\d+") && method.equals("PUT")) {
                    handlePutEspacio(exchange, Integer.parseInt(path.split("/")[3]));
                } else if (path.matches("/api/espacios/\\d+") && method.equals("DELETE")) {
                    int id = Integer.parseInt(path.split("/")[3]);
                    new EspacioDAO().desactivar(id);
                    sendJson(exchange, 200, "{\"success\":true}");
                }
                // CLIENTES
                else if (path.equals("/api/clientes") && method.equals("GET")) {
                    handleGetClientes(exchange);
                } else if (path.equals("/api/clientes") && method.equals("POST")) {
                    handlePostCliente(exchange);
                } else if (path.matches("/api/clientes/\\d+") && method.equals("GET")) {
                    int id = Integer.parseInt(path.split("/")[3]);
                    List<Cliente> todos = new ClienteDAO().listar();
                    Cliente found = null;
                    for (Cliente c : todos) { if (c.getIdCliente() == id) { found = c; break; } }
                    if (found != null) sendJson(exchange, 200, "{\"success\":true,\"data\":" + clienteToJson(found) + "}");
                    else sendJson(exchange, 404, "{\"success\":false,\"error\":\"No encontrado\"}");
                } else if (path.matches("/api/clientes/\\d+") && method.equals("PUT")) {
                    handlePutCliente(exchange, Integer.parseInt(path.split("/")[3]));
                } else if (path.matches("/api/clientes/\\d+") && method.equals("DELETE")) {
                    int id = Integer.parseInt(path.split("/")[3]);
                    new ClienteDAO().desactivar(id);
                    sendJson(exchange, 200, "{\"success\":true}");
                }
                // PAGOS
                else if (path.equals("/api/pagos") && method.equals("GET")) {
                    handleGetPagos(exchange);
                } else if (path.equals("/api/pagos") && method.equals("POST")) {
                    handlePostPago(exchange);
                } else if (path.matches("/api/pagos/\\d+/pagar") && method.equals("PUT")) {
                    int id = Integer.parseInt(path.split("/")[3]);
                    handlePagarPago(exchange, id);
                }
                // DESCUENTOS
                else if (path.equals("/api/descuentos") && method.equals("GET")) {
                    handleGetDescuentos(exchange);
                } else if (path.equals("/api/descuentos") && method.equals("POST")) {
                    handlePostDescuento(exchange);
                } else if (path.matches("/api/descuentos/\\d+") && method.equals("GET")) {
                    int id = Integer.parseInt(path.split("/")[3]);
                    Descuento d = new DescuentoDAO().buscarPorId(id);
                    if (d != null) sendJson(exchange, 200, "{\"success\":true,\"data\":" + descuentoToJson(d) + "}");
                    else sendJson(exchange, 404, "{\"success\":false,\"error\":\"No encontrado\"}");
                } else if (path.matches("/api/descuentos/\\d+") && method.equals("PUT")) {
                    handlePutDescuento(exchange, Integer.parseInt(path.split("/")[3]));
                } else if (path.matches("/api/descuentos/\\d+") && method.equals("DELETE")) {
                    int id = Integer.parseInt(path.split("/")[3]);
                    new DescuentoDAO().desactivar(id);
                    sendJson(exchange, 200, "{\"success\":true}");
                } else if (path.equals("/api/descuentos/validar") && method.equals("POST")) {
                    handleValidarDescuento(exchange);
                }
                // EVALUACIONES
                else if (path.equals("/api/evaluaciones") && method.equals("GET")) {
                    handleGetEvaluaciones(exchange);
                } else if (path.equals("/api/evaluaciones") && method.equals("POST")) {
                    handlePostEvaluacion(exchange);
                }
                // HORARIOS BLOQUEADOS
                else if (path.equals("/api/horarios") && method.equals("GET")) {
                    handleGetHorarios(exchange);
                } else if (path.equals("/api/horarios") && method.equals("POST")) {
                    handlePostHorario(exchange);
                } else if (path.matches("/api/horarios/\\d+") && method.equals("DELETE")) {
                    int id = Integer.parseInt(path.split("/")[3]);
                    new HorarioBloqueadoDAO().eliminar(id);
                    sendJson(exchange, 200, "{\"success\":true}");
                }
                // NOTIFICACIONES
                else if (path.equals("/api/notificaciones") && method.equals("GET")) {
                    handleGetNotificaciones(exchange);
                }
                // ENVIAR EVALUACIÓN (desde notificación)
                else if (path.matches("/api/evaluaciones/enviar/\\d+") && method.equals("POST")) {
                    int idNotificacion = Integer.parseInt(path.split("/")[4]);
                    handleEnviarEvaluacionDesdeNotificacion(exchange, idNotificacion);
                }
                // MARCAR NOTIFICACIÓN COMO LEÍDA
                else if (path.matches("/api/notificaciones/\\d+/leida") && method.equals("PUT")) {
                    int idNotif = Integer.parseInt(path.split("/")[3]);
                    try {
                        Connection connN = Conexion.getConexion();
                        java.sql.PreparedStatement psN = connN.prepareStatement(
                            "UPDATE NOTIFICACIONES SET leida=1 WHERE id_notificacion=?");
                        psN.setInt(1, idNotif);
                        psN.executeUpdate();
                        connN.commit();
                        Conexion.cerrar(connN);
                        sendJson(exchange, 200, "{\"success\":true}");
                    } catch (Exception ex) {
                        sendJson(exchange, 500, "{\"success\":false,\"error\":\"" + esc(ex.getMessage()) + "\"}");
                    }
                }
                // CALENDARIO SEMANAL
                else if (path.equals("/api/calendario/semanal") && method.equals("GET")) {
                    handleGetCalendarioSemanal(exchange);
                }
                else {
                    sendJson(exchange, 404, "{\"success\":false,\"error\":\"Endpoint no encontrado: " + path + "\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                sendJson(exchange, 500, "{\"success\":false,\"error\":\"" + esc(e.getMessage()) + "\"}");
            }
        }

        // ---- AUTH ----
        private void handleLogin(HttpExchange exchange) throws Exception {
            String body = readBody(exchange);
            String username = extractJson(body, "username");
            String password = extractJson(body, "password");
            if (username == null || password == null) {
                sendJson(exchange, 400, "{\"success\":false,\"error\":\"Faltan credenciales\"}");
                return;
            }
            String hash = HashUtil.sha256(password);
            Usuario u = new UsuarioDAO().autenticar(username, hash);
            if (u != null) {
                String userJson = "{\"idUsuario\":" + u.getIdUsuario()
                    + ",\"username\":\"" + esc(u.getUsername()) + "\""
                    + ",\"nombre\":\"" + esc(u.getNombre()) + "\""
                    + ",\"email\":\"" + esc(u.getEmail()) + "\""
                    + ",\"rol\":\"" + esc(u.getRol()) + "\"}";
                sendJson(exchange, 200, "{\"success\":true,\"user\":" + userJson + "}");
            } else {
                sendJson(exchange, 401, "{\"success\":false,\"error\":\"Credenciales inválidas\"}");
            }
        }

        // ---- RESERVAS ----
        private void handleGetReservas(HttpExchange exchange) throws Exception {
            List<Reserva> list = new ReservaDAO().listarTodas();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                Reserva r = list.get(i);
                sb.append("{")
                  .append("\"idReserva\":").append(r.getIdReserva()).append(",")
                  .append("\"idCliente\":").append(r.getCliente().getIdCliente()).append(",")
                  .append("\"nombreCliente\":\"").append(esc(r.getCliente().getNombre())).append(" ").append(esc(r.getCliente().getApellido())).append("\",")
                  .append("\"idEspacio\":").append(r.getEspacio().getIdEspacio()).append(",")
                  .append("\"nombreEspacio\":\"").append(esc(r.getEspacio().getNombre())).append("\",")
                  .append("\"fechaInicio\":\"").append(dateFormat.format(r.getFechaInicio())).append("\",")
                  .append("\"fechaFin\":\"").append(dateFormat.format(r.getFechaFin())).append("\",")
                  .append("\"montoTotal\":").append(r.getMontoTotal()).append(",")
                  .append("\"estado\":\"").append(r.getEstado()).append("\"")
                  .append("}");
            }
            sb.append("]");
            sendJson(exchange, 200, "{\"success\":true,\"data\":" + sb + "}");
        }

        private void handlePostReserva(HttpExchange exchange) throws Exception {
            String body = readBody(exchange);
            int idCliente = Integer.parseInt(extractJsonNum(body, "idCliente"));
            int idEspacio = Integer.parseInt(extractJsonNum(body, "idEspacio"));
            String fechaIni = extractJson(body, "fechaInicio");
            String fechaFin = extractJson(body, "fechaFin");

            Espacio esp = new EspacioDAO().buscarPorId(idEspacio);
            List<Cliente> clientes = new ClienteDAO().listar();
            Cliente cli = null;
            for (Cliente c : clientes) { if (c.getIdCliente() == idCliente) { cli = c; break; } }

            if (esp == null || cli == null) {
                sendJson(exchange, 400, "{\"success\":false,\"error\":\"Cliente o espacio no encontrado\"}");
                return;
            }

            String fi = fechaIni.replace("T", " ").replace("Z", "").trim();
            String ff = fechaFin.replace("T", " ").replace("Z", "").trim();
            if (fi.length() == 16) fi += ":00";
            if (ff.length() == 16) ff += ":00";
            Timestamp tsIni = Timestamp.valueOf(fi);
            Timestamp tsFin = Timestamp.valueOf(ff);
            // Validar disponibilidad antes de insertar
            boolean disponible = new ReservaDAO().verificarDisponibilidad(idEspacio, tsIni, tsFin);
            if (!disponible) {
                sendJson(exchange, 409, "{\"success\":false,\"error\":\"El espacio ya tiene una reserva en ese rango de horario\"}");
                return;
            }

            double horas = (tsFin.getTime() - tsIni.getTime()) / (1000.0 * 60 * 60);
            double monto = esp.getPrecioPorHora() * horas;

            Reserva r = new Reserva();
            r.setCliente(cli);
            r.setEspacio(esp);
            r.setFechaInicio(tsIni);
            r.setFechaFin(tsFin);
            r.setMontoTotal(monto);
            r.setEstado("PENDIENTE");
            new ReservaDAO().insertar(r);
            // Obtener el ID recién creado
            List<Reserva> todas = new ReservaDAO().listarPorCliente(idCliente);
            int newId = todas.isEmpty() ? 0 : todas.get(todas.size()-1).getIdReserva();
            // El pago se creará cuando se CONFIRME la reserva
            sendJson(exchange, 200, "{\"success\":true,\"idReserva\":" + newId + ",\"monto\":" + monto + "}");
        }

        // ---- ESPACIOS ----
        private void handleGetEspacios(HttpExchange exchange) throws Exception {
            List<Espacio> list = new EspacioDAO().listar();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(espacioToJson(list.get(i)));
            }
            sb.append("]");
            sendJson(exchange, 200, "{\"success\":true,\"data\":" + sb + "}");
        }

        private String espacioToJson(Espacio e) {
            return "{\"idEspacio\":" + e.getIdEspacio()
                + ",\"nombre\":\"" + esc(e.getNombre()) + "\""
                + ",\"tipo\":\"" + esc(e.getTipo()) + "\""
                + ",\"capacidad\":" + e.getCapacidad()
                + ",\"ubicacion\":\"" + esc(e.getUbicacion()) + "\""
                + ",\"precioPorHora\":" + e.getPrecioPorHora()
                + ",\"estado\":\"" + esc(e.getEstado()) + "\"}";
        }

        private void handlePostEspacio(HttpExchange exchange) throws Exception {
            String body = readBody(exchange);
            Espacio e = new Espacio();
            e.setNombre(extractJson(body, "nombre"));
            e.setTipo(extractJson(body, "tipo"));
            e.setCapacidad(Integer.parseInt(extractJsonNum(body, "capacidad")));
            e.setUbicacion(extractJson(body, "ubicacion"));
            e.setPrecioPorHora(Double.parseDouble(extractJsonNum(body, "precioPorHora")));
            e.setEstado("ACTIVO");
            new EspacioDAO().insertar(e);
            sendJson(exchange, 200, "{\"success\":true}");
        }

        private void handlePutEspacio(HttpExchange exchange, int id) throws Exception {
            String body = readBody(exchange);
            Espacio e = new EspacioDAO().buscarPorId(id);
            if (e == null) { sendJson(exchange, 404, "{\"success\":false}"); return; }
            if (extractJson(body, "nombre") != null) e.setNombre(extractJson(body, "nombre"));
            if (extractJson(body, "tipo") != null) e.setTipo(extractJson(body, "tipo"));
            if (extractJsonNum(body, "capacidad") != null) e.setCapacidad(Integer.parseInt(extractJsonNum(body, "capacidad")));
            if (extractJson(body, "ubicacion") != null) e.setUbicacion(extractJson(body, "ubicacion"));
            if (extractJsonNum(body, "precioPorHora") != null) e.setPrecioPorHora(Double.parseDouble(extractJsonNum(body, "precioPorHora")));
            new EspacioDAO().actualizar(e);
            sendJson(exchange, 200, "{\"success\":true}");
        }

        // ---- CLIENTES ----
        private void handleGetClientes(HttpExchange exchange) throws Exception {
            List<Cliente> list = new ClienteDAO().listar();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(clienteToJson(list.get(i)));
            }
            sb.append("]");
            sendJson(exchange, 200, "{\"success\":true,\"data\":" + sb + "}");
        }

        private String clienteToJson(Cliente c) {
            return "{\"idCliente\":" + c.getIdCliente()
                + ",\"nombre\":\"" + esc(c.getNombre()) + "\""
                + ",\"apellido\":\"" + esc(c.getApellido()) + "\""
                + ",\"nombreCompleto\":\"" + esc(c.getNombre()) + " " + esc(c.getApellido()) + "\""
                + ",\"dni\":\"" + esc(c.getDni()) + "\""
                + ",\"email\":\"" + esc(c.getEmail()) + "\""
                + ",\"telefono\":\"" + esc(c.getTelefono()) + "\"}";
        }

        private void handlePostCliente(HttpExchange exchange) throws Exception {
            String body = readBody(exchange);
            Cliente c = new Cliente();
            c.setNombre(extractJson(body, "nombre"));
            c.setApellido(extractJson(body, "apellido"));
            c.setDni(extractJson(body, "dni"));
            c.setEmail(extractJson(body, "email"));
            c.setTelefono(extractJson(body, "telefono"));
            new ClienteDAO().insertar(c);
            sendJson(exchange, 200, "{\"success\":true}");
        }

        private void handlePutCliente(HttpExchange exchange, int id) throws Exception {
            String body = readBody(exchange);
            List<Cliente> todos = new ClienteDAO().listar();
            Cliente c = null;
            for (Cliente x : todos) { if (x.getIdCliente() == id) { c = x; break; } }
            if (c == null) { sendJson(exchange, 404, "{\"success\":false}"); return; }
            if (extractJson(body, "nombre") != null) c.setNombre(extractJson(body, "nombre"));
            if (extractJson(body, "apellido") != null) c.setApellido(extractJson(body, "apellido"));
            if (extractJson(body, "dni") != null) c.setDni(extractJson(body, "dni"));
            if (extractJson(body, "email") != null) c.setEmail(extractJson(body, "email"));
            if (extractJson(body, "telefono") != null) c.setTelefono(extractJson(body, "telefono"));
            new ClienteDAO().actualizar(c);
            sendJson(exchange, 200, "{\"success\":true}");
        }

        // ---- PAGOS ----
        private void handleGetPagos(HttpExchange exchange) throws Exception {
            String sql = "SELECT p.id_pago, p.id_reserva, p.monto, p.metodo_pago, p.estado_pago, "
                       + "p.fecha_creacion, p.fecha_pago, NVL(p.descuento_aplicado, 0) AS descuento_aplicado, "
                       + "c.nombre || ' ' || c.apellido AS nombre_cliente, c.email AS email_cliente "
                       + "FROM PAGOS p "
                       + "JOIN RESERVAS r ON p.id_reserva = r.id_reserva "
                       + "JOIN CLIENTES c ON r.id_cliente = c.id_cliente "
                       + "ORDER BY p.estado_pago ASC, p.fecha_creacion DESC";
            Connection conn = null;
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            try {
                conn = Conexion.getConexion();
                ResultSet rs = conn.prepareStatement(sql).executeQuery();
                while (rs.next()) {
                    if (!first) sb.append(",");
                    first = false;
                    sb.append("{")
                      .append("\"idPago\":").append(rs.getInt("id_pago")).append(",")
                      .append("\"idReserva\":").append(rs.getInt("id_reserva")).append(",")
                      .append("\"monto\":").append(rs.getDouble("monto")).append(",")
                      .append("\"metodoPago\":\"").append(rs.getString("metodo_pago") != null ? esc(rs.getString("metodo_pago")) : "").append("\",")
                      .append("\"estadoPago\":\"").append(esc(rs.getString("estado_pago"))).append("\",")
                      .append("\"nombreCliente\":\"").append(esc(rs.getString("nombre_cliente"))).append("\",")
                      .append("\"emailCliente\":\"").append(esc(rs.getString("email_cliente"))).append("\",")
                      .append("\"fechaCreacion\":\"").append(rs.getDate("fecha_creacion") != null ? rs.getDate("fecha_creacion").toString() : "").append("\",")
                      .append("\"fechaPago\":\"").append(rs.getDate("fecha_pago") != null ? rs.getDate("fecha_pago").toString() : "").append("\"")
                      .append("}");
                }
            } finally {
                Conexion.cerrar(conn);
            }
            sb.append("]");
            sendJson(exchange, 200, "{\"success\":true,\"data\":" + sb + "}");
        }

        private void handlePagarPago(HttpExchange exchange, int idPago) throws Exception {
            String body = readBody(exchange);
            String metodoPago = extractJson(body, "metodoPago");
            if (metodoPago == null || metodoPago.trim().isEmpty()) {
                sendJson(exchange, 400, "{\"success\":false,\"error\":\"Método de pago requerido\"}");
                return;
            }

            // Descuento aplicado (opcionales)
            String montoFinalStr    = extractJsonNum(body, "montoFinal");
            String idDescuentoStr   = extractJsonNum(body, "idDescuento");
            double montoFinal       = (montoFinalStr != null)  ? Double.parseDouble(montoFinalStr)   : 0;
            int    idDescuento      = (idDescuentoStr != null) ? Integer.parseInt(idDescuentoStr)    : 0;

            // Obtener el pago
            Pago pago = new PagoDAO().buscarPorId(idPago);
            if (pago == null) {
                sendJson(exchange, 404, "{\"success\":false,\"error\":\"Pago no encontrado\"}");
                return;
            }
            if ("COMPLETADO".equals(pago.getEstadoPago())) {
                sendJson(exchange, 400, "{\"success\":false,\"error\":\"El pago ya fue procesado\"}");
                return;
            }

            // Calcular descuento aplicado
            double montoOriginal   = pago.getMonto();
            double montoAPagar     = (montoFinal > 0 && montoFinal < montoOriginal) ? montoFinal : montoOriginal;
            double descuentoMonto  = montoOriginal - montoAPagar;

            // Marcar pago como COMPLETADO con descuento
            boolean ok = new PagoDAO().pagar(idPago, metodoPago, montoAPagar, idDescuento, descuentoMonto);
            if (!ok) {
                sendJson(exchange, 500, "{\"success\":false,\"error\":\"Error al procesar el pago\"}");
                return;
            }

            // Incrementar usos del descuento si se aplicó
            if (idDescuento > 0) {
                new DescuentoDAO().incrementarUsos(idDescuento);
            }

            // Marcar reserva como COMPLETADA
            new ReservaDAO().cambiarEstado(pago.getIdReserva(), "COMPLETADA");

            // Obtener datos del cliente para confirmación de pago
            String sqlCliente = "SELECT c.nombre || ' ' || c.apellido AS nombre_cliente, c.email, c.id_cliente "
                              + "FROM RESERVAS r JOIN CLIENTES c ON r.id_cliente = c.id_cliente "
                              + "WHERE r.id_reserva = ?";
            Connection conn = null;
            try {
                conn = Conexion.getConexion();
                java.sql.PreparedStatement ps = conn.prepareStatement(sqlCliente);
                ps.setInt(1, pago.getIdReserva());
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String emailCliente  = rs.getString("email");
                    String nombreCliente = rs.getString("nombre_cliente");
                    int idCliente = rs.getInt("id_cliente");
                    if (emailCliente != null && !emailCliente.isEmpty()) {
                        // SOLO enviar confirmación de pago
                        com.spacework.util.EmailUtil.enviarConfirmacionPago(
                            emailCliente, nombreCliente,
                            pago.getIdReserva(), montoAPagar, metodoPago
                        );

                        // Generar token pero NO enviar email automáticamente
                        String token = java.util.UUID.randomUUID().toString();
                        java.util.Date ahora       = new java.util.Date();
                        java.util.Date expiracion  = new java.util.Date(ahora.getTime() + (30L * 24 * 60 * 60 * 1000));

                        TokenEvaluacion tokenEval = new TokenEvaluacion();
                        tokenEval.setIdPago(idPago);
                        tokenEval.setToken(token);
                        tokenEval.setEmailCliente(emailCliente);
                        tokenEval.setFechaCreacion(ahora);
                        tokenEval.setFechaExpiracion(expiracion);
                        tokenEval.setUtilizado(0);

                        boolean tokenGuardado = new TokenEvaluacionDAO().crearToken(tokenEval);
                        if (tokenGuardado) {
                            // Obtener id_usuario del administrador para la notificación interna
                            int idUsuarioAdmin = 1;
                            try {
                                java.sql.PreparedStatement psAdmin = conn.prepareStatement("SELECT MIN(id_usuario) FROM USUARIOS WHERE estado='ACTIVO'");
                                ResultSet rsAdmin = psAdmin.executeQuery();
                                if (rsAdmin.next()) idUsuarioAdmin = rsAdmin.getInt(1);
                                psAdmin.close();
                            } catch (Exception ignored) {}

                            // Crear notificación de evaluación pendiente (para el admin)
                            String sqlNotif = "INSERT INTO NOTIFICACIONES (id_notificacion, id_usuario, tipo, asunto, mensaje, leida, fecha_creacion) "
                                            + "VALUES (SEQ_NOTIFICACIONES.NEXTVAL, ?, 'EVALUACION', ?, ?, 0, SYSDATE)";
                            java.sql.PreparedStatement psNotif = conn.prepareStatement(sqlNotif);
                            psNotif.setInt(1, idUsuarioAdmin);
                            psNotif.setString(2, "Solicitar evaluación - Reserva #" + pago.getIdReserva());
                            psNotif.setString(3, "Token: " + token + " | Email: " + emailCliente + " | Cliente: " + nombreCliente);
                            psNotif.executeUpdate();
                            conn.commit();
                            psNotif.close();
                            System.out.println("[Pagos] ✅ Notificación EVALUACION creada - Reserva #" + pago.getIdReserva() + " para " + emailCliente);
                        }
                    }
                }
            } finally {
                Conexion.cerrar(conn);
            }

            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Pago procesado correctamente\""
                + ",\"descuentoAplicado\":" + descuentoMonto
                + ",\"montoFinal\":" + montoAPagar + "}");
        }

        private void handlePostPago(HttpExchange exchange) throws Exception {
            String body = readBody(exchange);
            Pago p = new Pago();
            p.setIdReserva(Integer.parseInt(extractJsonNum(body, "idReserva")));
            p.setMonto(Double.parseDouble(extractJsonNum(body, "monto")));
            p.setMetodoPago(extractJson(body, "metodoPago"));
            p.setEstadoPago("PENDIENTE");
            
            // Insertar el pago
            PagoDAO pagoDAO = new PagoDAO();
            boolean ok = pagoDAO.insertar(p);
            
            if (ok) {
                try {
                    // Obtener el ID del pago recién insertado
                    String sqlMaxId = "SELECT MAX(id_pago) as id_pago FROM PAGOS WHERE id_reserva = ?";
                    Connection conn = Conexion.getConexion();
                    java.sql.PreparedStatement ps = conn.prepareStatement(sqlMaxId);
                    ps.setInt(1, p.getIdReserva());
                    ResultSet rs = ps.executeQuery();
                    int idPago = 0;
                    if (rs.next()) {
                        idPago = rs.getInt("id_pago");
                    }
                    Conexion.cerrar(conn);
                    
                    // Generar token de evaluación
                    String token = java.util.UUID.randomUUID().toString();
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.add(java.util.Calendar.DATE, 30);
                    
                    // Obtener datos del cliente
                    String sqlCliente = "SELECT c.nombre || ' ' || c.apellido AS nombre_cliente, c.email, c.id_cliente "
                                      + "FROM RESERVAS r JOIN CLIENTES c ON r.id_cliente = c.id_cliente "
                                      + "WHERE r.id_reserva = ?";
                    conn = Conexion.getConexion();
                    ps = conn.prepareStatement(sqlCliente);
                    ps.setInt(1, p.getIdReserva());
                    rs = ps.executeQuery();
                    
                    if (rs.next()) {
                        String emailCliente  = rs.getString("email");
                        int idClienteNotif   = rs.getInt("id_cliente");
                        
                        // Guardar token en base de datos
                        com.spacework.model.TokenEvaluacion tokenEval = new com.spacework.model.TokenEvaluacion();
                        tokenEval.setIdPago(idPago);
                        tokenEval.setToken(token);
                        tokenEval.setEmailCliente(emailCliente);
                        tokenEval.setFechaExpiracion(new java.util.Date(cal.getTimeInMillis()));
                        new com.spacework.dao.TokenEvaluacionDAO().crearToken(tokenEval);

                        // Crear notificación EVALUACION (sin enviar email aún)
                        if (emailCliente != null && !emailCliente.isEmpty()) {
                            // Obtener id_usuario del admin (FK → USUARIOS)
                            int idUsuarioAdm = 1;
                            try {
                                java.sql.PreparedStatement psAdmin = conn.prepareStatement("SELECT MIN(id_usuario) FROM USUARIOS WHERE estado='ACTIVO'");
                                ResultSet rsAdmin = psAdmin.executeQuery();
                                if (rsAdmin.next()) idUsuarioAdm = rsAdmin.getInt(1);
                                psAdmin.close();
                            } catch (Exception ignored) {}

                            String sqlNotif = "INSERT INTO NOTIFICACIONES (id_notificacion, id_usuario, tipo, asunto, mensaje, leida, fecha_creacion) "
                                           + "VALUES (SEQ_NOTIFICACIONES.NEXTVAL, ?, 'EVALUACION', ?, ?, 0, SYSDATE)";
                            java.sql.PreparedStatement psNotif = conn.prepareStatement(sqlNotif);
                            psNotif.setInt(1, idUsuarioAdm);
                            psNotif.setString(2, "Solicitar evaluación - Reserva #" + p.getIdReserva());
                            psNotif.setString(3, "Token: " + token + " | Email: " + emailCliente);
                            psNotif.executeUpdate();
                            conn.commit();
                            psNotif.close();
                            System.out.println("[Pagos] Notificación EVALUACION creada para reserva #" + p.getIdReserva());
                        }
                    }
                    Conexion.cerrar(conn);
                } catch (Exception e) {
                    System.out.println("⚠️  Error generando token de evaluación: " + e.getMessage());
                }
            }
            
            sendJson(exchange, 200, "{\"success\":" + ok + "}");
        }

        // ---- DESCUENTOS ----
        private String descuentoToJson(Descuento d) {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            return "{"
                + "\"idDescuento\":" + d.getIdDescuento() + ","
                + "\"codigo\":\"" + esc(d.getCodigo()) + "\","
                + "\"descripcion\":\"" + esc(d.getDescripcion()) + "\","
                + "\"porcentaje\":" + d.getPorcentaje() + ","
                + "\"montoMinimo\":" + d.getMontoMinimo() + ","
                + "\"usosActuales\":" + d.getUsosActuales() + ","
                + "\"usosMaximos\":" + d.getUsosMaximos() + ","
                + "\"estado\":\"" + esc(d.getEstado()) + "\","
                + "\"fechaInicio\":\"" + (d.getFechaInicio() != null ? sdf.format(d.getFechaInicio()) : "") + "\","
                + "\"fechaFin\":\"" + (d.getFechaFin() != null ? sdf.format(d.getFechaFin()) : "") + "\""
                + "}";
        }

        private void handlePutDescuento(HttpExchange exchange, int id) throws Exception {
            String body = readBody(exchange);
            Descuento d = new DescuentoDAO().buscarPorId(id);
            if (d == null) { sendJson(exchange, 404, "{\"success\":false,\"error\":\"No encontrado\"}"); return; }
            if (extractJson(body, "codigo") != null) d.setCodigo(extractJson(body, "codigo").toUpperCase().trim());
            if (extractJson(body, "descripcion") != null) d.setDescripcion(extractJson(body, "descripcion"));
            if (extractJsonNum(body, "porcentaje") != null) d.setPorcentaje(Double.parseDouble(extractJsonNum(body, "porcentaje")));
            if (extractJsonNum(body, "montoMinimo") != null) d.setMontoMinimo(Double.parseDouble(extractJsonNum(body, "montoMinimo")));
            if (extractJsonNum(body, "usosMaximos") != null) d.setUsosMaximos(Integer.parseInt(extractJsonNum(body, "usosMaximos")));
            if (extractJson(body, "estado") != null) d.setEstado(extractJson(body, "estado"));
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                if (extractJson(body, "fechaInicio") != null) d.setFechaInicio(sdf.parse(extractJson(body, "fechaInicio")));
                if (extractJson(body, "fechaFin") != null) d.setFechaFin(sdf.parse(extractJson(body, "fechaFin")));
            } catch (Exception ex) { /* keep existing dates */ }
            boolean ok = new DescuentoDAO().actualizar(d);
            sendJson(exchange, 200, "{\"success\":" + ok + "}");
        }

        private void handleGetDescuentos(HttpExchange exchange) throws Exception {
            List<Descuento> list = new DescuentoDAO().listar();
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(descuentoToJson(list.get(i)));
            }
            sb.append("]");
            sendJson(exchange, 200, "{\"success\":true,\"data\":" + sb + "}");
        }

        // ---- DESCUENTOS POST / VALIDAR ----
        private void handlePostDescuento(HttpExchange exchange) throws Exception {
            String body = readBody(exchange);
            String codigo = extractJson(body, "codigo");
            String descripcion = extractJson(body, "descripcion");
            String porcentajeStr = extractJsonNum(body, "porcentaje");
            String montoStr = extractJsonNum(body, "montoMinimo");
            String fIni = extractJson(body, "fechaInicio");
            String fFin = extractJson(body, "fechaFin");
            String usosStr = extractJsonNum(body, "usosMaximos");

            if (codigo == null || codigo.isEmpty() || porcentajeStr == null || descripcion == null || fIni == null || fFin == null) {
                sendJson(exchange, 400, "{\"success\":false,\"error\":\"Faltan campos obligatorios\"}");
                return;
            }

            Descuento d = new Descuento();
            d.setCodigo(codigo.toUpperCase().trim());
            d.setDescripcion(descripcion);
            d.setPorcentaje(Double.parseDouble(porcentajeStr));
            d.setMontoMinimo(montoStr != null ? Double.parseDouble(montoStr) : 0);
            d.setUsosMaximos(usosStr != null ? Integer.parseInt(usosStr) : 100);
            d.setUsosActuales(0);
            d.setEstado("ACTIVO");
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                d.setFechaInicio(sdf.parse(fIni));
                d.setFechaFin(sdf.parse(fFin));
            } catch (Exception ex) {
                sendJson(exchange, 400, "{\"success\":false,\"error\":\"Formato de fecha inválido (yyyy-MM-dd)\"}");
                return;
            }
            boolean ok = new DescuentoDAO().insertar(d);
            sendJson(exchange, 200, "{\"success\":" + ok + "}");
        }

        private void handleValidarDescuento(HttpExchange exchange) throws Exception {
            String body = readBody(exchange);
            String codigo = extractJson(body, "codigo");
            String montoStr = extractJsonNum(body, "monto");
            double monto = montoStr != null ? Double.parseDouble(montoStr) : 0;

            if (codigo == null || codigo.trim().isEmpty()) {
                sendJson(exchange, 400, "{\"success\":false,\"error\":\"Código requerido\"}");
                return;
            }
            Descuento d = new DescuentoDAO().buscarPorCodigo(codigo.toUpperCase().trim());
            if (d == null) {
                sendJson(exchange, 404, "{\"success\":false,\"error\":\"Código de descuento no encontrado\"}");
                return;
            }
            boolean valido = new DescuentoDAO().validarCodigo(codigo.toUpperCase().trim(), monto);
            if (!valido) {
                sendJson(exchange, 400, "{\"success\":false,\"error\":\"Descuento inválido, vencido o monto mínimo no cumplido (S/. " + d.getMontoMinimo() + ")\"}");
                return;
            }
            sendJson(exchange, 200, "{\"success\":true,\"porcentaje\":" + d.getPorcentaje()
                + ",\"descripcion\":\"" + esc(d.getDescripcion()) + "\""
                + ",\"idDescuento\":" + d.getIdDescuento() + "}");
        }

        // ---- EVALUACIONES ----
        private void handleGetEvaluaciones(HttpExchange exchange) throws Exception {
            String sql = "SELECT e.id_evaluacion, e.id_reserva, e.id_cliente, e.calificacion, " +
                         "e.comentario, e.fecha_evaluacion, c.nombre, c.apellido, c.email " +
                         "FROM EVALUACIONES e " +
                         "JOIN CLIENTES c ON e.id_cliente = c.id_cliente " +
                         "ORDER BY e.fecha_evaluacion DESC";
            
            Connection conn = Conexion.getConexion();
            java.sql.Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            
            while (rs.next()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("{");
                sb.append("\"idEvaluacion\":").append(rs.getInt("id_evaluacion")).append(",");
                sb.append("\"idReserva\":").append(rs.getInt("id_reserva")).append(",");
                sb.append("\"idCliente\":").append(rs.getInt("id_cliente")).append(",");
                sb.append("\"calificacion\":").append(rs.getInt("calificacion")).append(",");
                sb.append("\"comentario\":\"").append(esc(rs.getString("comentario"))).append("\",");
                sb.append("\"nombreCliente\":\"").append(esc(rs.getString("nombre") + " " + rs.getString("apellido"))).append("\",");
                sb.append("\"emailCliente\":\"").append(esc(rs.getString("email"))).append("\",");
                java.util.Date fe = rs.getDate("fecha_evaluacion");
                sb.append("\"fechaEvaluacion\":\"").append(fe != null ? dateFormat.format(fe) : "").append("\"");
                sb.append("}");
            }
            sb.append("]");
            Conexion.cerrar(conn);
            sendJson(exchange, 200, "{\"success\":true,\"data\":" + sb + "}");
        }

        private void handlePostEvaluacion(HttpExchange exchange) throws Exception {
            String body = readBody(exchange);
            int idReserva = Integer.parseInt(extractJsonNum(body, "idReserva"));
            int calificacion = Integer.parseInt(extractJsonNum(body, "calificacion"));
            String comentario = extractJson(body, "comentario");

            Reserva reserva = new ReservaDAO().buscarPorId(idReserva);
            if (reserva == null) {
                sendJson(exchange, 404, "{\"success\":false,\"error\":\"Reserva no encontrada\"}");
                return;
            }

            // Crear evaluación
            Evaluacion evaluacion = new Evaluacion();
            evaluacion.setIdReserva(idReserva);
            evaluacion.setCalificacion(calificacion);
            evaluacion.setComentario(comentario != null ? comentario : "");
            evaluacion.setFechaEvaluacion(new java.util.Date());
            new EvaluacionDAO().insertar(evaluacion);

            // Marcar reserva como COMPLETADA
            new ReservaDAO().cambiarEstado(idReserva, "COMPLETADA");

            sendJson(exchange, 200, "{\"success\":true,\"message\":\"Evaluación registrada\"}");
        }

        // ---- NOTIFICACIONES ----
        private void handleGetNotificaciones(HttpExchange exchange) throws Exception {
            String sql = "SELECT n.id_notificacion, n.id_usuario, n.tipo, n.asunto, n.mensaje, "
                       + "n.leida, n.fecha_creacion "
                       + "FROM NOTIFICACIONES n ORDER BY n.fecha_creacion DESC";
            Connection conn = Conexion.getConexion();
            java.sql.Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            while (rs.next()) {
                if (!first) sb.append(",");
                first = false;
                sb.append("{");
                sb.append("\"idNotificacion\":").append(rs.getInt("id_notificacion")).append(",");
                sb.append("\"idUsuario\":").append(rs.getInt("id_usuario")).append(",");
                sb.append("\"tipo\":\"").append(esc(rs.getString("tipo"))).append("\",");
                sb.append("\"asunto\":\"").append(esc(rs.getString("asunto"))).append("\",");
                sb.append("\"mensaje\":\"").append(esc(rs.getString("mensaje"))).append("\",");
                sb.append("\"leida\":").append(rs.getInt("leida")).append(",");
                java.util.Date fc = rs.getDate("fecha_creacion");
                sb.append("\"fechaCreacion\":\"").append(fc != null ? dateFormat.format(fc) : "").append("\"");
                sb.append("}");
            }
            sb.append("]");
            Conexion.cerrar(conn);
            sendJson(exchange, 200, "{\"success\":true,\"data\":" + sb + "}");
        }

        private void handleGetCalendarioSemanal(HttpExchange exchange) throws Exception {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            java.text.SimpleDateFormat sdfDate = new java.text.SimpleDateFormat("yyyy-MM-dd");
            java.text.SimpleDateFormat sdfHora = new java.text.SimpleDateFormat("HH:mm");
            java.text.SimpleDateFormat sdfFull = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Connection conn = Conexion.getConexion();
            
            // Obtener espacios activos
            String sqlEspacios = "SELECT id_espacio, nombre FROM ESPACIOS WHERE estado='ACTIVO' ORDER BY nombre";
            java.sql.Statement stmtE = conn.createStatement();
            ResultSet rsE = stmtE.executeQuery(sqlEspacios);
            StringBuilder espacios = new StringBuilder();
            boolean firstE = true;
            while (rsE.next()) {
                if (!firstE) espacios.append(",");
                firstE = false;
                espacios.append("{\"idEspacio\":").append(rsE.getInt("id_espacio"))
                        .append(",\"nombre\":\"").append(esc(rsE.getString("nombre"))).append("\"}");
            }
            
            // Generar bloques de 1 hora (8:00-9:00, 9:00-10:00, etc.) por 7 días
            StringBuilder bloques = new StringBuilder();
            boolean firstB = true;
            
            for (int dia = 0; dia < 7; dia++) {
                java.util.Date fecha = cal.getTime();
                String fechaStr = sdfDate.format(fecha);
                
                for (int hora = 8; hora < 18; hora += 1) {
                    if (!firstB) bloques.append(",");
                    firstB = false;
                    
                    String horaInicio = String.format("%02d:00", hora);
                    String horaFin = String.format("%02d:00", hora + 1);
                    
                    java.util.Date dtIni = sdfFull.parse(fechaStr + " " + horaInicio + ":00");
                    java.util.Date dtFin = sdfFull.parse(fechaStr + " " + horaFin + ":00");
                    java.sql.Timestamp tsIni = new java.sql.Timestamp(dtIni.getTime());
                    java.sql.Timestamp tsFin = new java.sql.Timestamp(dtFin.getTime());
                    
                    bloques.append("{\"fecha\":\"").append(fechaStr).append("\",\"hora\":\"")
                           .append(horaInicio).append("-").append(horaFin).append("\",\"espacios\":{");
                    
                    // Para cada espacio, verificar disponibilidad
                    stmtE = conn.createStatement();
                    rsE = stmtE.executeQuery(sqlEspacios);
                    boolean firstEsp = true;
                    while (rsE.next()) {
                        if (!firstEsp) bloques.append(",");
                        firstEsp = false;
                        
                        int idEsp = rsE.getInt("id_espacio");
                        
                        // Verificar RESERVAS
                        String sqlRes = "SELECT COUNT(*) cnt FROM RESERVAS WHERE id_espacio=? AND estado IN ('CONFIRMADA','COMPLETADA') " +
                                       "AND (fecha_inicio < ? AND fecha_fin > ?)";
                        java.sql.PreparedStatement psRes = conn.prepareStatement(sqlRes);
                        psRes.setInt(1, idEsp);
                        psRes.setTimestamp(2, tsFin);
                        psRes.setTimestamp(3, tsIni);
                        ResultSet rsRes = psRes.executeQuery();
                        int cntRes = rsRes.next() ? rsRes.getInt("cnt") : 0;
                        rsRes.close();
                        psRes.close();
                        
                        // Verificar HORARIOS_BLOQUEADOS
                        String sqlHor = "SELECT COUNT(*) cnt FROM HORARIOS_BLOQUEADOS WHERE id_espacio=? " +
                                       "AND (fecha_inicio < ? AND fecha_fin > ?)";
                        java.sql.PreparedStatement psHor = conn.prepareStatement(sqlHor);
                        psHor.setInt(1, idEsp);
                        psHor.setTimestamp(2, tsFin);
                        psHor.setTimestamp(3, tsIni);
                        ResultSet rsHor = psHor.executeQuery();
                        int cntHor = rsHor.next() ? rsHor.getInt("cnt") : 0;
                        rsHor.close();
                        psHor.close();
                        
                        String estado = cntRes > 0 ? "ocupado" : (cntHor > 0 ? "bloqueado" : "disponible");
                        bloques.append("\"").append(idEsp).append("\":\"").append(estado).append("\"");
                    }
                    rsE.close();
                    stmtE.close();
                    bloques.append("}}");
                }
                cal.add(java.util.Calendar.DAY_OF_MONTH, 1);
            }
            
            Conexion.cerrar(conn);
            String json = "{\"success\":true,\"data\":{\"espacios\":[" + espacios + "],\"bloques\":[" + bloques + "]}}";
            sendJson(exchange, 200, json);
        }

        // ---- HORARIOS BLOQUEADOS ----
        private void handleGetHorarios(HttpExchange exchange) throws Exception {
            String sql = "SELECT hb.id_bloqueo, hb.id_espacio, hb.fecha_inicio, hb.fecha_fin, hb.razon, " +
                         "e.nombre AS nombre_espacio FROM HORARIOS_BLOQUEADOS hb " +
                         "JOIN ESPACIOS e ON hb.id_espacio = e.id_espacio ORDER BY hb.fecha_inicio DESC";
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            try (java.sql.Connection conn = com.spacework.util.Conexion.getConexion();
                 java.sql.Statement stmt = conn.createStatement();
                 java.sql.ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    if (!first) sb.append(",");
                    first = false;
                    java.sql.Timestamp tsIni = rs.getTimestamp("fecha_inicio");
                    java.sql.Timestamp tsFin = rs.getTimestamp("fecha_fin");
                    sb.append("{")
                      .append("\"idHorarioBloqueado\":").append(rs.getInt("id_bloqueo")).append(",")
                      .append("\"idEspacio\":").append(rs.getInt("id_espacio")).append(",")
                      .append("\"nombreEspacio\":\"").append(esc(rs.getString("nombre_espacio"))).append("\",")
                      .append("\"fechaInicio\":\"").append(tsIni != null ? dateFormat.format(tsIni) : "").append("\",")
                      .append("\"fechaFin\":\"").append(tsFin != null ? dateFormat.format(tsFin) : "").append("\",")
                      .append("\"razon\":\"").append(esc(rs.getString("razon"))).append("\"")
                      .append("}");
                }
            }
            sb.append("]");
            sendJson(exchange, 200, "{\"success\":true,\"data\":" + sb + "}");
        }

        private void handlePostHorario(HttpExchange exchange) throws Exception {
            String body = readBody(exchange);
            int idEspacio = Integer.parseInt(extractJsonNum(body, "idEspacio"));
            String fechaIni = extractJson(body, "fechaInicio").replace("T", " ");
            String fechaFin = extractJson(body, "fechaFin").replace("T", " ");
            String razon = extractJson(body, "razon");
            // Timestamp.valueOf requires "yyyy-MM-dd HH:mm:ss"; append seconds if missing
            if (fechaIni != null && fechaIni.length() == 16) fechaIni = fechaIni + ":00";
            if (fechaFin != null && fechaFin.length() == 16) fechaFin = fechaFin + ":00";
            Timestamp tsIni = Timestamp.valueOf(fechaIni);
            Timestamp tsFin = Timestamp.valueOf(fechaFin);
            HorarioBloqueado h = new HorarioBloqueado(idEspacio, tsIni, tsFin, razon, "admin");
            new HorarioBloqueadoDAO().registrar(h);
            sendJson(exchange, 200, "{\"success\":true}");
        }

        // ---- HELPERS ----
        private String readBody(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            byte[] buf = new byte[8192];
            int n = is.read(buf);
            return n > 0 ? new String(buf, 0, n, "UTF-8") : "";
        }

        private String extractJson(String json, String key) {
            String pattern = "\"" + key + "\"";
            int idx = json.indexOf(pattern);
            if (idx < 0) return null;
            int colon = json.indexOf(":", idx + pattern.length());
            if (colon < 0) return null;
            int start = colon + 1;
            while (start < json.length() && json.charAt(start) == ' ') start++;
            if (start >= json.length()) return null;
            if (json.charAt(start) == '"') {
                int end = json.indexOf('"', start + 1);
                while (end > 0 && json.charAt(end - 1) == '\\') end = json.indexOf('"', end + 1);
                return end > 0 ? json.substring(start + 1, end) : null;
            }
            return null;
        }

        private String extractJsonNum(String json, String key) {
            String pattern = "\"" + key + "\"";
            int idx = json.indexOf(pattern);
            if (idx < 0) return null;
            int colon = json.indexOf(":", idx + pattern.length());
            if (colon < 0) return null;
            int start = colon + 1;
            while (start < json.length() && json.charAt(start) == ' ') start++;
            if (start >= json.length()) return null;
            char c = json.charAt(start);
            if (c == '"') {
                int end = json.indexOf('"', start + 1);
                return end > 0 ? json.substring(start + 1, end) : null;
            }
            if (Character.isDigit(c) || c == '-') {
                int end = start + 1;
                while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.')) end++;
                return json.substring(start, end);
            }
            return null;
        }

        private void handleEnviarEvaluacionDesdeNotificacion(HttpExchange exchange, int idNotificacion) throws Exception {
            try {
                // Obtener datos de la notificación (token y email)
                String sqlNotif = "SELECT mensaje FROM NOTIFICACIONES WHERE id_notificacion = ?";
                Connection conn = Conexion.getConexion();
                java.sql.PreparedStatement ps = conn.prepareStatement(sqlNotif);
                ps.setInt(1, idNotificacion);
                ResultSet rs = ps.executeQuery();
                
                if (!rs.next()) {
                    sendJson(exchange, 404, "{\"success\":false,\"error\":\"Notificación no encontrada\"}");
                    return;
                }
                
                String mensaje = rs.getString("mensaje");
                // Formato: "Token: {token} | Email: {email}"
                String token = mensaje.split("Token: ")[1].split(" \\| ")[0].trim();
                // Obtener solo el email (sin nada extra después)
                String emailRaw = mensaje.split("Email: ")[1].trim();
                // Cortar en espacio, pipe, o newline por si hay texto adicional
                String emailCliente = emailRaw.split("[ \\|\\n\\r]")[0].trim();
                
                // Obtener datos de la reserva desde el token
                String sqlToken = "SELECT nt.id_pago, nt.email_cliente FROM TOKENS_EVALUACION nt WHERE nt.token = ?";
                java.sql.PreparedStatement psToken = conn.prepareStatement(sqlToken);
                psToken.setString(1, token);
                ResultSet rsToken = psToken.executeQuery();
                
                if (!rsToken.next()) {
                    sendJson(exchange, 400, "{\"success\":false,\"error\":\"Token inválido\"}");
                    Conexion.cerrar(conn);
                    return;
                }
                
                int idPago = rsToken.getInt("id_pago");
                
                // Obtener datos de la reserva
                String sqlReserva = "SELECT r.id_cliente, c.nombre || ' ' || c.apellido AS nombre_cliente, e.nombre AS espacio_nombre, r.id_reserva, r.fecha_inicio "
                                  + "FROM PAGOS p JOIN RESERVAS r ON p.id_reserva = r.id_reserva "
                                  + "JOIN CLIENTES c ON r.id_cliente = c.id_cliente "
                                  + "JOIN ESPACIOS e ON r.id_espacio = e.id_espacio "
                                  + "WHERE p.id_pago = ?";
                java.sql.PreparedStatement psReserva = conn.prepareStatement(sqlReserva);
                psReserva.setInt(1, idPago);
                ResultSet rsReserva = psReserva.executeQuery();
                
                if (rsReserva.next()) {
                    String nombreCliente = rsReserva.getString("nombre_cliente");
                    int idReserva = rsReserva.getInt("id_reserva");

                    // Enviar email y verificar resultado real
                    boolean enviado = com.spacework.util.EmailUtil.enviarFormularioEvaluacion(
                        emailCliente, nombreCliente,
                        idReserva, token,
                        "http://localhost:8080"
                    );

                    if (enviado) {
                        System.out.println("[Notificaciones] ✅ Email evaluación enviado a: " + emailCliente);
                        sendJson(exchange, 200, "{\"success\":true,\"message\":\"Email de evaluación enviado a " + esc(emailCliente) + " | Cliente: " + esc(nombreCliente) + "\"}");
                    } else {
                        System.err.println("[Notificaciones] ✗ Falló envío a: " + emailCliente);
                        sendJson(exchange, 500, "{\"success\":false,\"error\":\"No se pudo enviar el correo a " + esc(emailCliente) + ". Verifica la configuración de mail.properties\"}");
                    }
                } else {
                    sendJson(exchange, 404, "{\"success\":false,\"error\":\"Reserva no encontrada\"}");
                }
                
                Conexion.cerrar(conn);
            } catch (Exception e) {
                sendJson(exchange, 500, "{\"success\":false,\"error\":\"" + esc(e.getMessage()) + "\"}");
            }
        }

        private String esc(String v) {
            if (v == null) return "";
            return v.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
        }

        private void sendJson(HttpExchange exchange, int code, String json) throws IOException {
            byte[] bytes = json.getBytes("UTF-8");
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(code, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        }
    }

    /**
     * Handler para ruta raíz - sirve index.html (SPA)
     */
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Si es una ruta raíz o sin extensión, sirve index.html (SPA)
            if (path.equals("/") || !path.contains(".")) {
                String indexPath = STATIC_DIR + "/index.html";
                File indexFile = new File(indexPath);
                
                if (indexFile.exists()) {
                    byte[] fileContent = Files.readAllBytes(indexFile.toPath());
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
                    exchange.sendResponseHeaders(200, fileContent.length);
                    exchange.getResponseBody().write(fileContent);
                } else {
                    String response = "<h1>404 - Archivo no encontrado</h1>";
                    exchange.getResponseHeaders().set("Content-Type", "text/html");
                    exchange.sendResponseHeaders(404, response.getBytes().length);
                    exchange.getResponseBody().write(response.getBytes());
                }
            } else {
                String response = "{\"error\": \"No encontrado\"}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(404, response.getBytes().length);
                exchange.getResponseBody().write(response.getBytes());
            }
            exchange.close();
        }
    }
}
