package com.spacework.controller.api;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import com.spacework.model.Cliente;
import com.spacework.dao.ClienteDAO;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Handler HTTP para operaciones CRUD de Clientes
 */
public class ClienteRestController implements HttpHandler {

    private ClienteDAO clienteDAO = new ClienteDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("GET".equals(method)) {
                if (path.matches(".*/clientes$")) {
                    handleGetAllClientes(exchange);
                } else if (path.matches(".*/clientes/dni/.*")) {
                    String dni = path.substring(path.lastIndexOf("/") + 1);
                    handleGetClienteByDni(exchange, dni);
                } else if (path.matches(".*/clientes/\\d+$")) {
                    int id = extractId(path);
                    handleGetCliente(exchange, id);
                } else {
                    sendError(exchange, 404, "Ruta no encontrada");
                }
            } else if ("POST".equals(method)) {
                handleCreateCliente(exchange);
            } else if ("PUT".equals(method)) {
                int id = extractId(path);
                handleUpdateCliente(exchange, id);
            } else if ("DELETE".equals(method)) {
                int id = extractId(path);
                handleDeleteCliente(exchange, id);
            } else {
                sendError(exchange, 405, "Método no permitido");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleGetAllClientes(HttpExchange exchange) throws IOException {
        try {
            java.util.List<Cliente> clientes = clienteDAO.listar();
            StringBuilder sb = new StringBuilder();
            sb.append("{\"success\": true, \"count\": ").append(clientes.size()).append(", \"data\": [");
            for (int i = 0; i < clientes.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(toJson(clientes.get(i)));
            }
            sb.append("]}");
            sendResponse(exchange, 200, sb.toString());
        } catch (Exception e) {
            sendError(exchange, 500, "Error al listar clientes");
        }
    }

    private void handleGetCliente(HttpExchange exchange, int id) throws IOException {
        try {
            // ClienteDAO no tiene buscarPorId, usar listar y filtrar
            java.util.List<Cliente> clientes = clienteDAO.listar();
            Cliente cliente = null;
            for (Cliente c : clientes) {
                if (c.getIdCliente() == id) {
                    cliente = c;
                    break;
                }
            }
            if (cliente == null) {
                sendError(exchange, 404, "Cliente no encontrado");
            } else {
                String json = "{\"success\": true, \"data\": " + toJson(cliente) + "}";
                sendResponse(exchange, 200, json);
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleGetClienteByDni(HttpExchange exchange, String dni) throws IOException {
        try {
            Cliente cliente = clienteDAO.buscarPorDni(dni);
            if (cliente == null) {
                sendError(exchange, 404, "Cliente no encontrado");
            } else {
                String json = "{\"success\": true, \"data\": " + toJson(cliente) + "}";
                sendResponse(exchange, 200, json);
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleCreateCliente(HttpExchange exchange) throws IOException {
        try {
            byte[] buffer = new byte[1024];
            int len = exchange.getRequestBody().read(buffer);
            String body = new String(buffer, 0, len, StandardCharsets.UTF_8);
            
            // Parse JSON: {"nombre": "Juan", "apellido": "Pérez", "dni": "12345678", "email": "juan@example.com", "telefono": "555-1234"}
            String nombre = extractJsonString(body, "nombre");
            String apellido = extractJsonString(body, "apellido");
            String dni = extractJsonString(body, "dni");
            String email = extractJsonString(body, "email");
            String telefono = extractJsonString(body, "telefono");
            
            Cliente cliente = new Cliente();
            cliente.setNombre(nombre);
            cliente.setApellido(apellido);
            cliente.setDni(dni);
            cliente.setEmail(email);
            cliente.setTelefono(telefono);
            
            clienteDAO.insertar(cliente);
            String json = "{\"success\": true, \"message\": \"Cliente creado exitosamente\"}";
            sendResponse(exchange, 201, json);
        } catch (Exception e) {
            sendError(exchange, 500, "Error al crear cliente: " + e.getMessage());
        }
    }

    private void handleUpdateCliente(HttpExchange exchange, int id) throws IOException {
        try {
            java.io.InputStream is = exchange.getRequestBody();
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf)) != -1) baos.write(buf, 0, n);
            String body = baos.toString("UTF-8");

            String nombre = extractJsonString(body, "nombre");
            String apellido = extractJsonString(body, "apellido");
            String email = extractJsonString(body, "email");
            String telefono = extractJsonString(body, "telefono");

            Cliente cliente = new Cliente();
            cliente.setIdCliente(id);
            cliente.setNombre(nombre);
            cliente.setApellido(apellido);
            cliente.setEmail(email);
            cliente.setTelefono(telefono);

            clienteDAO.actualizar(cliente);
            String json = "{\"success\": true, \"message\": \"Cliente actualizado\"}";
            sendResponse(exchange, 200, json);
        } catch (Exception e) {
            sendError(exchange, 500, "Error al actualizar: " + e.getMessage());
        }
    }

    private void handleDeleteCliente(HttpExchange exchange, int id) throws IOException {
        try {
            clienteDAO.desactivar(id);
            String json = "{\"success\": true, \"message\": \"Cliente eliminado\"}";
            sendResponse(exchange, 200, json);
        } catch (Exception e) {
            sendError(exchange, 500, "Error al eliminar: " + e.getMessage());
        }
    }

    private String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int idx = json.indexOf(pattern);
        if (idx == -1) return "";
        idx += pattern.length();
        int endIdx = json.indexOf("\"", idx);
        if (endIdx == -1) return "";
        return json.substring(idx, endIdx);
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
        if (obj instanceof Cliente) {
            Cliente c = (Cliente) obj;
            return String.format(
                "{\"idCliente\": %d, \"nombre\": \"%s\", \"apellido\": \"%s\", \"nombreCompleto\": \"%s\", \"dni\": \"%s\", \"email\": \"%s\", \"telefono\": \"%s\"}",
                c.getIdCliente(), esc(c.getNombre()), esc(c.getApellido()),
                esc(c.getNombreCompleto()), c.getDni(), esc(c.getEmail()), esc(c.getTelefono())
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
