package com.spacework.controller.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.spacework.model.Reserva;
import com.spacework.model.Cliente;
import com.spacework.model.Espacio;
import com.spacework.dao.ReservaDAO;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Handler HTTP para operaciones CRUD de Reservas
 * GET /reservas -> listar todas
 * GET /reservas/{id} -> obtener una
 */
import com.spacework.dao.EspacioDAO;

public class ReservaRestController implements HttpHandler {

    private ReservaDAO reservaDAO = new ReservaDAO();
    private EspacioDAO espacioDAO = new EspacioDAO();

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
                if (path.matches(".*/reservas$")) {
                    handleGetAllReservas(exchange);
                } else if (path.matches(".*/reservas/\\d+$")) {
                    int id = extractId(path);
                    handleGetReserva(exchange, id);
                } else {
                    sendError(exchange, 404, "Ruta no encontrada");
                }
            } else if ("POST".equals(method)) {
                handleCreateReserva(exchange);
            } else if ("PUT".equals(method)) {
                int id = extractId(path);
                if (path.contains("confirmar")) {
                    handleConfirmarReserva(exchange, id);
                } else if (path.contains("completar")) {
                    handleCompletarReserva(exchange, id);
                } else {
                    handleUpdateReserva(exchange, id);
                }
            } else if ("DELETE".equals(method)) {
                int id = extractId(path);
                handleCancelarReserva(exchange, id);
            } else {
                sendError(exchange, 405, "Método no permitido");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error interno: " + e.getMessage());
        }
    }

    private void handleGetAllReservas(HttpExchange exchange) throws IOException {
        try {
            java.util.List<Reserva> reservas = reservaDAO.listarTodas();
            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\": true, \"count\": ").append(reservas.size()).append(", \"data\": [");
            for (int i = 0; i < reservas.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(reservas.get(i)));
            }
            sb.append("]}");
            sendResponse(exchange, 200, sb.toString());
        } catch (Exception e) {
            sendError(exchange, 500, "Error al listar reservas: " + e.getMessage());
        }
    }

    private void handleGetReserva(HttpExchange exchange, int id) throws IOException {
        try {
            // ReservaDAO no tiene buscarPorId, usar listarTodas y filtrar
            java.util.List<Reserva> reservas = reservaDAO.listarTodas();
            Reserva reserva = null;
            for (Reserva r : reservas) {
                if (r.getIdReserva() == id) {
                    reserva = r;
                    break;
                }
            }
            if (reserva == null) {
                sendError(exchange, 404, "Reserva no encontrada");
            } else {
                String json = "{\"success\": true, \"data\": " + toJson(reserva) + "}";
                sendResponse(exchange, 200, json);
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleCreateReserva(HttpExchange exchange) throws IOException {
        try {
            byte[] buffer = new byte[2048];
            int len = exchange.getRequestBody().read(buffer);
            String body = new String(buffer, 0, len, StandardCharsets.UTF_8);

            int idCliente  = extractJsonInt(body, "idCliente");
            int idEspacio  = extractJsonInt(body, "idEspacio");
            String fechaIniStr = extractJsonString(body, "fechaInicio");
            String fechaFinStr = extractJsonString(body, "fechaFin");

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            java.util.Date fechaIni = sdf.parse(fechaIniStr);
            java.util.Date fechaFin = sdf.parse(fechaFinStr);

            // Calcular monto según precio del espacio
            Espacio espacio = espacioDAO.buscarPorId(idEspacio);
            if (espacio == null) { sendError(exchange, 400, "Espacio no encontrado"); return; }

            long horas = (fechaFin.getTime() - fechaIni.getTime()) / (1000 * 60 * 60);
            
            double monto = espacio.getPrecioPorHora() * horas;

            Cliente cliente = new Cliente();
            cliente.setIdCliente(idCliente);

            Reserva r = new Reserva();
            r.setCliente(cliente);
            r.setEspacio(espacio);
            r.setFechaInicio(fechaIni);
            r.setFechaFin(fechaFin);
            r.setMontoTotal(monto);

            reservaDAO.insertar(r);
            sendResponse(exchange, 201, "{\"success\": true, \"message\": \"Reserva creada exitosamente\", \"monto\": " + monto + "}");
        } catch (Exception e) {
            sendError(exchange, 500, "Error al crear reserva: " + e.getMessage());
        }
    }

    private void handleUpdateReserva(HttpExchange exchange, int id) throws IOException {
        sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Actualizar reserva - EN DESARROLLO\"}");
    }

    private void handleConfirmarReserva(HttpExchange exchange, int id) throws IOException {
        try {
            reservaDAO.cambiarEstado(id, "CONFIRMADA");
            sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Reserva confirmada\"}");
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleCompletarReserva(HttpExchange exchange, int id) throws IOException {
        try {
            reservaDAO.cambiarEstado(id, "COMPLETADA");
            sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Reserva completada\"}");
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleCancelarReserva(HttpExchange exchange, int id) throws IOException {
        try {
            reservaDAO.cambiarEstado(id, "CANCELADA");
            sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Reserva cancelada\"}");
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return "";
        idx += pattern.length();
        int end = json.indexOf("\"", idx);
        return end == -1 ? "" : json.substring(idx, end);
    }

    private int extractJsonInt(String json, String key) {
        String pattern = "\"" + key + "\":";
        int idx = json.indexOf(pattern);
        if (idx == -1) return 0;
        idx += pattern.length();
        int end = json.indexOf(",", idx);
        if (end == -1) end = json.indexOf("}", idx);
        try { return Integer.parseInt(json.substring(idx, end).trim()); } catch (Exception e) { return 0; }
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        try {
            return Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private String toJson(Object obj) {
        if (obj instanceof Reserva) {
            Reserva r = (Reserva) obj;
            Cliente c = r.getCliente();
            Espacio e = r.getEspacio();
            String clienteNombre = (c != null) ? esc(c.getNombreCompleto()) : "";
            String espacioNombre = (e != null) ? esc(e.getNombre()) : "";
            String espacioTipo = (e != null) ? esc(e.getTipo()) : "";
            double precio = (e != null) ? e.getPrecioPorHora() : 0;
            String fechaIni = (r.getFechaInicio() != null) ? r.getFechaInicio().toString() : "";
            String fechaFin = (r.getFechaFin() != null) ? r.getFechaFin().toString() : "";
            String fechaCrea = (r.getFechaCreacion() != null) ? r.getFechaCreacion().toString() : "";
            return String.format(
                "{\"idReserva\": %d, \"estado\": \"%s\", \"montoTotal\": %.2f, " +
                "\"fechaInicio\": \"%s\", \"fechaFin\": \"%s\", \"fechaCreacion\": \"%s\", " +
                "\"cliente\": {\"idCliente\": %d, \"nombre\": \"%s\"}, " +
                "\"espacio\": {\"idEspacio\": %d, \"nombre\": \"%s\", \"tipo\": \"%s\", \"precioPorHora\": %.2f}}",
                r.getIdReserva(), esc(r.getEstado()), r.getMontoTotal(),
                fechaIni, fechaFin, fechaCrea,
                (c != null ? c.getIdCliente() : 0), clienteNombre,
                (e != null ? e.getIdEspacio() : 0), espacioNombre, espacioTipo, precio
            );
        }
        return "{}";
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private <T> String toJsonArray(java.util.List<T> list) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) json.append(",");
            json.append(toJson(list.get(i)));
        }
        json.append("]");
        return json.toString();
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String json = String.format("{\"success\": false, \"error\": \"%s\"}", message);
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, response.length);
        OutputStream os = exchange.getResponseBody();
        os.write(response);
        os.close();
    }
}
