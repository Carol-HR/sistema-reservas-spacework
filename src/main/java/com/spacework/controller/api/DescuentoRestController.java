package com.spacework.controller.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.spacework.model.Descuento;
import com.spacework.dao.DescuentoDAO;
import com.spacework.controller.DescuentoController;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Date;

/**
 * Handler HTTP REST para operaciones CRUD de Descuentos
 * GET /api/descuentos -> listar todos
 * GET /api/descuentos/{id} -> obtener uno
 * POST /api/descuentos -> crear
 * PUT /api/descuentos/{id} -> actualizar
 */
public class DescuentoRestController implements HttpHandler {

    private DescuentoDAO descuentoDAO = new DescuentoDAO();
    private DescuentoController descuentoController = new DescuentoController();

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
                if (path.matches(".*/descuentos$")) {
                    handleGetAllDescuentos(exchange);
                } else if (path.matches(".*/descuentos/\\d+$")) {
                    int id = extractId(path);
                    handleGetDescuento(exchange, id);
                } else if (path.contains("codigo")) {
                    String codigo = path.substring(path.lastIndexOf("/") + 1);
                    handleGetPorCodigo(exchange, codigo);
                } else {
                    sendError(exchange, 404, "Ruta no encontrada");
                }
            } else if ("POST".equals(method)) {
                handleCreateDescuento(exchange);
            } else if ("PUT".equals(method)) {
                int id = extractId(path);
                if (path.contains("desactivar")) {
                    handleDesactivarDescuento(exchange, id);
                } else {
                    handleUpdateDescuento(exchange, id);
                }
            } else if ("DELETE".equals(method)) {
                int id = extractId(path);
                handleDesactivarDescuento(exchange, id);
            } else {
                sendError(exchange, 405, "Método no permitido");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error interno: " + e.getMessage());
        }
    }

    private void handleGetAllDescuentos(HttpExchange exchange) throws IOException {
        try {
            java.util.List<Descuento> descuentos = descuentoDAO.listar();
            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\": true, \"count\": ").append(descuentos.size()).append(", \"data\": [");
            for (int i = 0; i < descuentos.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(descuentos.get(i)));
            }
            sb.append("]}");
            sendResponse(exchange, 200, sb.toString());
        } catch (Exception e) {
            sendError(exchange, 500, "Error al listar descuentos: " + e.getMessage());
        }
    }

    private void handleGetDescuento(HttpExchange exchange, int id) throws IOException {
        try {
            Descuento descuento = descuentoDAO.buscarPorId(id);
            if (descuento == null) {
                sendError(exchange, 404, "Descuento no encontrado");
            } else {
                sendResponse(exchange, 200, "{\"success\": true, \"data\": " + toJson(descuento) + "}");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error al obtener: " + e.getMessage());
        }
    }

    private void handleGetPorCodigo(HttpExchange exchange, String codigo) throws IOException {
        try {
            Descuento descuento = descuentoDAO.buscarPorCodigo(codigo);
            if (descuento == null) {
                sendError(exchange, 404, "Código no válido");
            } else {
                sendResponse(exchange, 200, "{\"success\": true, \"data\": " + toJson(descuento) + "}");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleCreateDescuento(HttpExchange exchange) throws IOException {
        try {
            String body = readBody(exchange);
            String codigo = extractJsonString(body, "codigo").toUpperCase();
            String descripcion = extractJsonString(body, "descripcion");
            double porcentaje = extractJsonDouble(body, "porcentaje");
            double montoMinimo = extractJsonDouble(body, "montoMinimo");

            java.util.Date ahora = new java.util.Date();
            java.util.Date fechaFin = new java.util.Date(ahora.getTime() + 30 * 24 * 60 * 60 * 1000);

            if (descuentoController.registrarDescuento(codigo, descripcion, porcentaje, montoMinimo,
                    new Date(ahora.getTime()), new Date(fechaFin.getTime()), 0)) {
                sendResponse(exchange, 201, "{\"success\": true, \"message\": \"Descuento registrado\"}");
            } else {
                sendError(exchange, 400, "Error al registrar");
            }
        } catch (Exception e) {
            sendError(exchange, 400, "Datos inválidos: " + e.getMessage());
        }
    }

    private void handleUpdateDescuento(HttpExchange exchange, int id) throws IOException {
        try {
            String body = readBody(exchange);
            String descripcion = extractJsonString(body, "descripcion");
            double porcentaje = extractJsonDouble(body, "porcentaje");
            double montoMinimo = extractJsonDouble(body, "montoMinimo");

            java.util.Date fecha = new java.util.Date();

            if (descuentoController.actualizarDescuento(id, descripcion, porcentaje,
                    montoMinimo, new Date(fecha.getTime()))) {
                sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Descuento actualizado\"}");
            } else {
                sendError(exchange, 400, "Error al actualizar");
            }
        } catch (Exception e) {
            sendError(exchange, 400, "Error: " + e.getMessage());
        }
    }

    private void handleDesactivarDescuento(HttpExchange exchange, int id) throws IOException {
        try {
            if (descuentoController.desactivarDescuento(id)) {
                sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Descuento desactivado\"}");
            } else {
                sendError(exchange, 400, "Error al desactivar");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private String toJson(Descuento desc) {
        Double montoMin = desc.getMontoMinimo();
        Object usosMax = desc.getUsosMaximos();
        return "{\"idDescuento\": " + desc.getIdDescuento() + ", \"codigo\": \"" + desc.getCodigo() +
                "\", \"descripcion\": \"" + desc.getDescripcion() + "\", \"porcentaje\": " + desc.getPorcentaje() +
                ", \"montoMinimo\": " + (montoMin != null ? montoMin : 0) +
                ", \"usosActuales\": " + desc.getUsosActuales() + ", \"usosMaximos\": " +
                (usosMax != null ? usosMax : 0) + ", \"estado\": \"" + desc.getEstado() + "\"}";
    }

    protected int extractId(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
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

    protected double extractJsonDouble(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*([\\d.]+)";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return Double.parseDouble(m.group(1));
        }
        throw new IllegalArgumentException("Campo no encontrado: " + key);
    }

    protected String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        throw new IllegalArgumentException("Campo no encontrado: " + key);
    }
}
