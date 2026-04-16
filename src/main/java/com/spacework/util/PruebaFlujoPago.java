package com.spacework.util;

import com.spacework.model.*;
import com.spacework.dao.*;
import com.spacework.controller.PagoController;
import com.spacework.controller.EvaluacionController;
import java.util.Date;

/**
 * PRUEBA: Simula el flujo completo de pago → email → evaluación
 * Ejecutar esta clase para ver cómo funciona el sistema
 */
public class PruebaFlujoPago {

    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println("\n" + repeat("=", 80));
        System.out.println("        PRUEBA FLUJO: PAGO → EMAIL CON FORMULARIO → EVALUACIÓN");
        System.out.println(repeat("=", 80) + "\n");

        try {
            // ===== PASO 1: SIMULAR CLIENTE PAGANDO =====
            System.out.println("📋 PASO 1: Cliente realiza PAGO");
            System.out.println(repeat("-", 80));
            
            // Crear datos simulados
            Cliente cliente = new Cliente();
            cliente.setIdCliente(1);
            cliente.setNombre("Juan");
            cliente.setApellido("Perez");
            cliente.setEmail("juancmduel@gmail.com");
            
            Reserva reserva = new Reserva();
            reserva.setIdReserva(1);
            reserva.setCliente(cliente);
            Espacio espacio = new Espacio();
            espacio.setIdEspacio(1);
            espacio.setNombre("Sala de Reuniones Premium");
            reserva.setEspacio(espacio);
            reserva.setFechaInicio(new Date());
            
            Pago pago = new Pago();
            pago.setIdPago(1);
            pago.setIdReserva(1);
            pago.setMonto(150.00);
            pago.setMetodoPago("TARJETA");
            pago.setEstadoPago("COMPLETADO");
            
            System.out.println("✓ Pago registrado:");
            System.out.println("  - ID Pago: " + pago.getIdPago());
            System.out.println("  - Monto: S/. " + pago.getMonto());
            System.out.println("  - Cliente: " + cliente.getNombre() + " " + cliente.getApellido());
            System.out.println("  - Email: " + cliente.getEmail());
            
            // ===== PASO 2: GENERAR TOKEN Y EMAIL =====
            System.out.println("\n📧 PASO 2: Sistema genera TOKEN y envía EMAIL al cliente");
            System.out.println(repeat("-", 80));
            
            String token = java.util.UUID.randomUUID().toString();
            java.util.Date ahora = new java.util.Date();
            java.util.Date expiracion = new java.util.Date(ahora.getTime() + (30L * 24 * 60 * 60 * 1000));
            
            System.out.println("✓ Token generado: " + token);
            System.out.println("✓ Válido hasta: " + 
                    new java.text.SimpleDateFormat("dd/MM/yyyy").format(expiracion));
            
            // ===== PASO 3: MOSTRAR EMAIL QUE RECIBE EL CLIENTE =====
            System.out.println("\n📬 PASO 3: Email recibido por el cliente:");
            System.out.println(repeat("-", 80));
            mostrarEmailConFormulario(cliente, espacio, token);
            
            // ===== PASO 4: SIMULAR QUE CLIENTE CALIFICA =====
            System.out.println("\n⭐ PASO 4: Cliente marca nivel de atención y envía");
            System.out.println(repeat("-", 80));
            
            int calificacionCliente = 5;  // Cliente marca 5 estrellas
            String comentarioCliente = "Excelente espacio, muy profesionales";
            
            System.out.println("✓ Cliente seleccionó: " + calificacionCliente + " ⭐");
            System.out.println("✓ Comentario: \"" + comentarioCliente + "\"");
            System.out.println("✓ Token enviado: " + token);
            
            // ===== PASO 5: SERVIDOR RECIBE Y VALIDA =====
            System.out.println("\n🔐 PASO 5: Servidor valida token y registra evaluación");
            System.out.println(repeat("-", 80));
            
            // Simular validación de token
            boolean tokenValido = validarToken(token);
            System.out.println(tokenValido ? "✓ Token válido" : "✗ Token inválido");
            
            if (tokenValido) {
                // Registrar evaluación en BD
                System.out.println("✓ Registrando evaluación en BD...");
                System.out.println("  INSERT INTO EVALUACIONES");
                System.out.println("  (id_evaluacion, id_reserva, id_cliente, calificacion, comentario, fecha_evaluacion)");
                System.out.println("  VALUES (SEQ_EVALUACIONES.NEXTVAL, 1, 1, " + calificacionCliente + ", '" 
                        + comentarioCliente + "', SYSDATE)");
                System.out.println("✓ Evaluación registrada exitosamente");
                
                // Marcar token como utilizado
                System.out.println("✓ Token marcado como utilizado");
                System.out.println("  UPDATE TOKENS_EVALUACION");
                System.out.println("  SET utilizado = 1");
                System.out.println("  WHERE token = '" + token + "'");
            }
            
            // ===== PASO 6: CONFIRMAR EVALUACIÓN =====
            System.out.println("\n✅ PASO 6: Confirmación final");
            System.out.println(repeat("-", 80));
            System.out.println("✓ Evaluación de cliente registrada correctamente");
            System.out.println("✓ Calificación: " + calificacionCliente + "/5 ⭐");
            System.out.println("✓ Fecha: " + new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
            System.out.println("✓ Estado en BD: COMPLETADA");
            
            // Mostrar resumen
            System.out.println("\n" + repeat("=", 80));
            System.out.println("✅ PRUEBA COMPLETADA EXITOSAMENTE");
            System.out.println(repeat("=", 80) + "\n");
            
        } catch (Exception e) {
            System.err.println("❌ Error during test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Muestra cómo se vería el email que recibe el cliente
     */
    private static void mostrarEmailConFormulario(Cliente cliente, Espacio espacio, String token) {
        // Mostrar el HTML del email
        String emailHTML = construirEmailConFormulario(cliente, espacio, token);
        System.out.println("\n[EMAIL VISUAL]");
        System.out.println("De: SpaceWork <juancmduel@gmail.com>");
        System.out.println("Para: " + cliente.getEmail());
        System.out.println("Asunto: Califica tu experiencia en " + espacio.getNombre() + " ⭐ - SpaceWork");
        System.out.println("\n[CUERPO DEL EMAIL]:\n");
        System.out.println(emailHTML);
    }

    /**
     * Construye el HTML del email con formulario incrustado
     */
    private static String construirEmailConFormulario(Cliente cliente, Espacio espacio, String token) {
        return "╔════════════════════════════════════════════════════════════════╗\n" +
               "║                    ¡Gracias por tu reserva! 🎉                  ║\n" +
               "║                                                                ║\n" +
               "║ Hola " + String.format("%-45s", cliente.getNombre()) + "║\n" +
               "║                                                                ║\n" +
               "║ Tu pago ha sido procesado exitosamente.                       ║\n" +
               "║ Nos encantaría conocer tu opinión sobre tu experiencia.       ║\n" +
               "║                                                                ║\n" +
               "║ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ ║\n" +
               "║ DETALLES DE TU RESERVA:                                       ║\n" +
               "║ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ ║\n" +
               "║                                                                ║\n" +
               "║ 📍 Espacio: " + String.format("%-40s", espacio.getNombre()) + "║\n" +
               "║ 💰 Monto: S/. 150.00                                          ║\n" +
               "║ 📅 Fecha: " + new java.text.SimpleDateFormat("dd/MM/yyyy").format(new Date()) + "                              ║\n" +
               "║                                                                ║\n" +
               "║ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ ║\n" +
               "║ CALIFICA TU EXPERIENCIA (selecciona estrellas):                ║\n" +
               "║ ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ ║\n" +
               "║                                                                ║\n" +
               "║ Nivel de Atención:                                            ║\n" +
               "║                                                                ║\n" +
               "║   [☆] [☆] [☆] [☆] [☆]   (haz clic en las estrellas)         ║\n" +
               "║    1   2   3   4   5                                          ║\n" +
               "║                                                                ║\n" +
               "║ Comentario (opcional):                                        ║\n" +
               "║ ┌───────────────────────────────────────────────────────────┐ ║\n" +
               "║ │ [Escribe aquí tu comentario...]                           │ ║\n" +
               "║ └───────────────────────────────────────────────────────────┘ ║\n" +
               "║                                                                ║\n" +
               "║ ┌─────────────────────────────────────────────────────────────┐║\n" +
               "║ │            [ENVIAR EVALUACIÓN]                             ││\n" +
               "║ └─────────────────────────────────────────────────────────────┘║\n" +
               "║                                                                ║\n" +
               "║ Token: " + token.substring(0, 32) + "...                   ║\n" +
               "║ Válido por: 30 días                                           ║\n" +
               "║                                                                ║\n" +
               "║ © 2026 SpaceWork Perú S.A.C.                                  ║\n" +
               "╚════════════════════════════════════════════════════════════════╝";
    }

    /**
     * Valida el token (simulación)
     */
    private static boolean validarToken(String token) {
        // En producción, buscaría en TOKENS_EVALUACION
        // Por ahora, simula validación exitosa
        return token != null && !token.isEmpty() && token.length() == 36;
    }
}
