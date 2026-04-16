package com.spacework.controller.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.spacework.model.Pago;
import com.spacework.dao.PagoDAO;
import com.spacework.controller.PagoController;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Handler HTTP REST para operaciones CRUD de Pagos
 * GET /api/pagos -> listar todos
 * GET /api/pagos/{id} -> obtener uno
 * POST /api/pagos -> crear
 * PUT /api/pagos/{id} -> actualizar
 */
public class PagoRestController implements HttpHandler {

    private PagoDAO pagoDAO = new PagoDAO();
    private PagoController pagoController = new PagoController();

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
                if (path.matches(".*/pagos$")) {
                    handleGetAllPagos(exchange);
                } else if (path.matches(".*/pagos/\\d+$")) {
                    int id = extractId(path);
                    handleGetPago(exchange, id);
                } else {
                    sendError(exchange, 404, "Ruta no encontrada");
                }
            } else if ("POST".equals(method)) {
                handleCreatePago(exchange);
            } else if ("PUT".equals(method)) {
                int id = extractId(path);
                if (path.contains("completar")) {
                    handleCompletarPago(exchange, id);
                } else if (path.contains("rechazar")) {
                    handleRechazarPago(exchange, id);
                } else {
                    handleUpdatePago(exchange, id);
                }
            } else {
                sendError(exchange, 405, "Método no permitido");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error interno: " + e.getMessage());
        }
    }

    private void handleGetAllPagos(HttpExchange exchange) throws IOException {
        try {
            java.util.List<Pago> pagos = pagoDAO.listar();
            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\": true, \"count\": ").append(pagos.size()).append(", \"data\": [");
            for (int i = 0; i < pagos.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(pagos.get(i)));
            }
            sb.append("]}");
            sendResponse(exchange, 200, sb.toString());
        } catch (Exception e) {
            sendError(exchange, 500, "Error al listar pagos: " + e.getMessage());
        }
    }

    private void handleGetPago(HttpExchange exchange, int id) throws IOException {
        try {
            Pago pago = pagoDAO.buscarPorId(id);
            if (pago == null) {
                sendError(exchange, 404, "Pago no encontrado");
            } else {
                sendResponse(exchange, 200, "{\"success\": true, \"data\": " + toJson(pago) + "}");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error al obtener pago: " + e.getMessage());
        }
    }

    private void handleCreatePago(HttpExchange exchange) throws IOException {
        try {
            String body = readBody(exchange);
            // Parse JSON simple: {"idReserva": 1, "monto": 100.50, "metodoPago": "TARJETA"}
            int idReserva = extractJsonInt(body, "idReserva");
            double monto = extractJsonDouble(body, "monto");
            String metodoPago = extractJsonString(body, "metodoPago");

            if (pagoController.registrarPago(idReserva, monto, metodoPago)) {
                sendResponse(exchange, 201, "{\"success\": true, \"message\": \"Pago registrado\"}");
            } else {
                sendError(exchange, 400, "Error al registrar pago");
            }
        } catch (Exception e) {
            sendError(exchange, 400, "Datos inválidos: " + e.getMessage());
        }
    }

    private void handleUpdatePago(HttpExchange exchange, int id) throws IOException {
        try {
            String body = readBody(exchange);
            java.util.List<Pago> pagos = pagoDAO.listar();
            Pago pago = null;
            for (Pago p : pagos) {
                if (p.getIdPago() == id) {
                    pago = p;
                    break;
                }
            }
            if (pago == null) {
                sendError(exchange, 404, "Pago no encontrado");
            } else {
                if (pagoDAO.actualizar(pago)) {
                    sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Pago actualizado\"}");
                } else {
                    sendError(exchange, 500, "Error al actualizar");
                }
            }
        } catch (Exception e) {
            sendError(exchange, 400, "Error: " + e.getMessage());
        }
    }

    private void handleCompletarPago(HttpExchange exchange, int id) throws IOException {
        try {
            if (pagoController.completarPago(id)) {
                sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Pago completado\"}");
            } else {
                sendError(exchange, 400, "Error al completar pago");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleRechazarPago(HttpExchange exchange, int id) throws IOException {
        try {
            if (pagoController.rechazarPago(id)) {
                sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Pago rechazado\"}");
            } else {
                sendError(exchange, 400, "Error al rechazar pago");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private String toJson(Pago pago) {
        return "{\"idPago\": " + pago.getIdPago() + ", \"idReserva\": " + pago.getIdReserva() +
                ", \"monto\": " + pago.getMonto() + ", \"metodoPago\": \"" + pago.getMetodoPago() +
                "\", \"estadoPago\": \"" + pago.getEstadoPago() + "\", \"fechaCreacion\": \"" +
                (pago.getFechaCreacion() != null ? pago.getFechaCreacion().toString() : "") + "\"}";
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
