package com.spacework.controller.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.spacework.model.Notificacion;
import com.spacework.dao.NotificacionDAO;
import com.spacework.controller.NotificacionController;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Handler HTTP REST para operaciones CRUD de Notificaciones
 * GET /api/notificaciones -> listar todas
 * GET /api/notificaciones/{id} -> obtener una
 * POST /api/notificaciones -> crear
 * PUT /api/notificaciones/{id}/marcar-leido -> marcar como leída
 * DELETE /api/notificaciones/{id} -> eliminar
 */
public class NotificacionRestController implements HttpHandler {

    private NotificacionDAO notificacionDAO = new NotificacionDAO();
    private NotificacionController notificacionController = new NotificacionController();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
                return;
            }

            if ("GET".equals(method)) {
                if (path.matches(".*/notificaciones$")) {
                    handleGetAll(exchange);
                } else if (path.contains("sin-leer")) {
                    int idUsuario = extractIdFromPath(path);
                    handleGetSinLeer(exchange, idUsuario);
                } else if (path.matches(".*/notificaciones/\\d+$")) {
                    int id = extractId(path);
                    handleGetNotificacion(exchange, id);
                } else {
                    sendError(exchange, 404, "Ruta no encontrada");
                }
            } else if ("POST".equals(method)) {
                handleCreateNotificacion(exchange);
            } else if ("PUT".equals(method)) {
                int id = extractId(path);
                if (path.contains("marcar-leido")) {
                    handleMarcarLeido(exchange, id);
                } else {
                    sendError(exchange, 405, "Método no permitido");
                }
            } else if ("DELETE".equals(method)) {
                int id = extractId(path);
                if (path.contains("todas")) {
                    handleEliminarTodas(exchange, id);
                } else {
                    handleDeleteNotificacion(exchange, id);
                }
            } else {
                sendError(exchange, 405, "Método no permitido");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error interno: " + e.getMessage());
        }
    }

    private void handleGetAll(HttpExchange exchange) throws IOException {
        try {
            java.util.List<Notificacion> notificaciones = notificacionDAO.listar();
            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\": true, \"count\": ").append(notificaciones.size()).append(", \"data\": [");
            for (int i = 0; i < notificaciones.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(notificaciones.get(i)));
            }
            sb.append("]}");
            sendResponse(exchange, 200, sb.toString());
        } catch (Exception e) {
            sendError(exchange, 500, "Error al listar: " + e.getMessage());
        }
    }

    private void handleGetSinLeer(HttpExchange exchange, int idUsuario) throws IOException {
        try {
            java.util.List<Notificacion> notificaciones = notificacionDAO.listarSinLeer(idUsuario);
            int conteo = notificacionDAO.contarSinLeer(idUsuario);
            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\": true, \"conteo\": ").append(conteo).append(", \"data\": [");
            for (int i = 0; i < notificaciones.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(notificaciones.get(i)));
            }
            sb.append("]}");
            sendResponse(exchange, 200, sb.toString());
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleGetNotificacion(HttpExchange exchange, int id) throws IOException {
        try {
            Notificacion notif = notificacionDAO.buscarPorId(id);
            if (notif == null) {
                sendError(exchange, 404, "Notificación no encontrada");
            } else {
                sendResponse(exchange, 200, "{\"success\": true, \"data\": " + toJson(notif) + "}");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleCreateNotificacion(HttpExchange exchange) throws IOException {
        try {
            String body = readBody(exchange);
            int idUsuario = extractJsonInt(body, "idUsuario");
            String tipo = extractJsonString(body, "tipo");
            String asunto = extractJsonString(body, "asunto");
            String mensaje = extractJsonString(body, "mensaje");

            if (notificacionController.registrarNotificacion(idUsuario, tipo, asunto, mensaje)) {
                sendResponse(exchange, 201, "{\"success\": true, \"message\": \"Notificación creada\"}");
            } else {
                sendError(exchange, 400, "Error al crear");
            }
        } catch (Exception e) {
            sendError(exchange, 400, "Datos inválidos: " + e.getMessage());
        }
    }

    private void handleMarcarLeido(HttpExchange exchange, int id) throws IOException {
        try {
            if (notificacionDAO.marcarComoLeida(id)) {
                sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Marcado como leído\"}");
            } else {
                sendError(exchange, 400, "Error al marcar");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleDeleteNotificacion(HttpExchange exchange, int id) throws IOException {
        try {
            if (notificacionDAO.eliminar(id)) {
                sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Notificación eliminada\"}");
            } else {
                sendError(exchange, 400, "Error al eliminar");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleEliminarTodas(HttpExchange exchange, int idUsuario) throws IOException {
        try {
            if (notificacionDAO.eliminarTodas(idUsuario)) {
                sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Todas eliminadas\"}");
            } else {
                sendError(exchange, 400, "Error al eliminar");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private String toJson(Notificacion notif) {
        String icono = getIconoTipo(notif.getTipo());
        return "{\"idNotificacion\": " + notif.getIdNotificacion() + ", \"idUsuario\": " + notif.getIdUsuario() +
                ", \"tipo\": \"" + notif.getTipo() + "\", \"icono\": \"" + icono + "\", \"asunto\": \"" + 
                notif.getAsunto() + "\", \"mensaje\": \"" + notif.getMensaje() + 
                "\", \"leida\": " + (notif.isLeida() ? "true" : "false") + ", \"fechaCreacion\": \"" + 
                notif.getFechaCreacion() + "\"}";
    }

    private String getIconoTipo(String tipo) {
        switch (tipo) {
            case "RESERVA": return "📅";
            case "PAGO": return "💳";
            case "RECORDATORIO": return "⏰";
            case "PROMOCION": return "🎁";
            case "SISTEMA": return "⚙️";
            default: return "📬";
        }
    }

    protected int extractId(String path) {
        String[] parts = path.split("/");
        for (String part : parts) {
            if (part.matches("\\d+")) {
                return Integer.parseInt(part);
            }
        }
        throw new IllegalArgumentException("ID no encontrado en la ruta");
    }

    protected int extractIdFromPath(String path) {
        String[] parts = path.split("/");
        if (parts.length > 0) {
            return Integer.parseInt(parts[parts.length - 1]);
        }
        throw new IllegalArgumentException("ID no encontrado");
    }

    protected String readBody(HttpExchange exchange) throws IOException {
        byte[] buffer = new byte[2048];
        int len = exchange.getRequestBody().read(buffer);
        return new String(buffer, 0, len, StandardCharsets.UTF_8);
    }

    protected void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    protected void sendError(HttpExchange exchange, int code, String message) throws IOException {
        String response = "{\"success\": false, \"error\": \"" + message + "\"}";
        sendResponse(exchange, code, response);
    }

    protected int extractJsonInt(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*(\\d+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        throw new IllegalArgumentException("Campo no encontrado: " + key);
    }

    protected String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]*?)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return "";
    }
}
