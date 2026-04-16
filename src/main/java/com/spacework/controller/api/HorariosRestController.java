package com.spacework.controller.api;

import com.spacework.dao.HorarioBloqueadoDAO;
import com.spacework.model.HorarioBloqueado;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HorariosRestController implements HttpHandler {

    private HorarioBloqueadoDAO horarioBloqueadoDAO = new HorarioBloqueadoDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("OPTIONS".equals(method)) {
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
                return;
            }

            String responseBody = "";
            int statusCode = 200;

            if ("GET".equals(method)) {
                if (path.matches(".*/horarios$")) {
                    responseBody = listarHorarios();
                } else {
                    statusCode = 404;
                    responseBody = toJson(false, "No encontrado");
                }
            } else if ("POST".equals(method) && path.matches(".*/horarios$")) {
                String body = readRequestBody(exchange);
                responseBody = crearHorario(body);
            } else if ("DELETE".equals(method) && path.matches(".*/horarios/\\d+$")) {
                int id = extractId(path);
                responseBody = eliminarHorario(id);
            } else {
                statusCode = 405;
                responseBody = toJson(false, "Método no permitido");
            }

            byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        } catch (Exception e) {
            String error = toJson(false, "Error: " + e.getMessage());
            byte[] response = error.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(500, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, len, StandardCharsets.UTF_8));
        }
        return sb.toString();
    }

    private String listarHorarios() {
        try {
            List<HorarioBloqueado> horarios = horarioBloqueadoDAO.listarTodos();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < horarios.size(); i++) {
                HorarioBloqueado h = horarios.get(i);
                if (i > 0) json.append(",");
                json.append(horarioBloqueadoToJson(h));
            }
            json.append("]");
            return toJson(true, horarios.size(), json.toString());
        } catch (Exception e) {
            return toJson(false, "Error: " + e.getMessage());
        }
    }

    private String crearHorario(String jsonBody) {
        try {
            // Parse JSON: {"idEspacio": 1, "fechaInicio": "2025-03-15 10:00", "fechaFin": "2025-03-15 12:00", "razon": "Mantenimiento", "usuarioCreador": "admin"}
            int idEspacio = extractIntField(jsonBody, "idEspacio");
            String fechaInicioStr = extractStringField(jsonBody, "fechaInicio");
            String fechaFinStr = extractStringField(jsonBody, "fechaFin");
            String razon = extractStringField(jsonBody, "razon");
            String usuarioCreador = "admin"; // Por defecto

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date fechaInicio = sdf.parse(fechaInicioStr);
            Date fechaFin = sdf.parse(fechaFinStr);

            HorarioBloqueado h = new HorarioBloqueado(idEspacio, fechaInicio, fechaFin, razon, usuarioCreador);

            horarioBloqueadoDAO.registrar(h);
            return toJson(true, "Horario bloqueado creado");
        } catch (Exception e) {
            return toJson(false, "Error: " + e.getMessage());
        }
    }

    private String eliminarHorario(int id) {
        try {
            horarioBloqueadoDAO.eliminar(id);
            return toJson(true, "Horario desbloqueado");
        } catch (Exception e) {
            return toJson(false, "Error: " + e.getMessage());
        }
    }

    private String horarioBloqueadoToJson(HorarioBloqueado h) {
        if (h == null) return "{}";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "{" +
                "\"idHorarioBloqueado\":" + h.getIdBloqueo() + "," +
                "\"idEspacio\":" + h.getIdEspacio() + "," +
                "\"fechaInicio\":\"" + (h.getFechaInicio() != null ? sdf.format(h.getFechaInicio()) : "") + "\"," +
                "\"fechaFin\":\"" + (h.getFechaFin() != null ? sdf.format(h.getFechaFin()) : "") + "\"," +
                "\"razon\":\"" + (h.getRazon() != null ? h.getRazon().replace("\"", "\\\"") : "") + "\"," +
                "\"usuarioCreador\":\"" + (h.getUsuarioCreador() != null ? h.getUsuarioCreador() : "") + "\"" +
                "}";
    }

    private String toJson(boolean success, String message) {
        return "{\"success\":" + success + ",\"message\":\"" + message + "\"}";
    }

    private String toJson(boolean success, int count, String dataArray) {
        return "{\"success\":" + success + ",\"count\":" + count + ",\"data\":" + dataArray + "}";
    }

    private int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }

    private int extractIntField(String json, String field) {
        String pattern = "\"" + field + "\":";
        int idx = json.indexOf(pattern);
        if (idx == -1) return 0;
        idx += pattern.length();
        int endIdx = json.indexOf(",", idx);
        if (endIdx == -1) endIdx = json.indexOf("}", idx);
        String value = json.substring(idx, endIdx).trim();
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private String extractStringField(String json, String field) {
        String pattern = "\"" + field + "\":\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return "";
        idx += pattern.length();
        int endIdx = json.indexOf("\"", idx);
        if (endIdx == -1) return "";
        return json.substring(idx, endIdx);
    }
}
