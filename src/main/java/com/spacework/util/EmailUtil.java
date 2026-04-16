package com.spacework.util;

/**
 * Utilidad para enviar correos electrónicos de confirmación de pago y solicitud de evaluación.
 * Delega en MailService, que lee credenciales desde mail.properties.
 */
public class EmailUtil {

    /**
     * Envía un correo de confirmación de pago al cliente.
     */
    public static void enviarConfirmacionPago(String para, String nombreCliente,
                                              int idReserva, double monto, String metodoPago) {
        String asunto = "SpaceWork - Pago Confirmado - Reserva #" + idReserva;
        String html   = buildHtmlPago(nombreCliente, idReserva, monto, metodoPago);
        boolean enviado = MailService.enviarCorreo(para, asunto, html);
        if (!enviado) {
            System.out.println("⚠️  No se pudo enviar confirmación de pago a: " + para);
        }
    }

    /**
     * Envía un correo con enlace para completar evaluación
     */
    public static boolean enviarFormularioEvaluacion(String para, String nombreCliente,
                                                   int idReserva, String token, String baseUrl) {
        String asunto = "SpaceWork - ¿Cómo fue tu experiencia? - Reserva #" + idReserva;
        String enlaceEvaluacion = baseUrl + "/evaluaciones/formulario?token=" + token;
        String html = buildHtmlEvaluacion(nombreCliente, idReserva, enlaceEvaluacion);
        boolean enviado = MailService.enviarCorreo(para, asunto, html);
        if (!enviado) {
            System.out.println("⚠️  No se pudo enviar formulario de evaluación a: " + para);
        }
        return enviado;
    }

    public static String buildHtmlPago(String nombre, int idReserva, double monto, String metodo) {
        return "<!DOCTYPE html>"
            + "<html><head><meta charset='utf-8'></head>"
            + "<body style='font-family:Arial,sans-serif;background:#f4f6f9;padding:20px;margin:0'>"
            + "<div style='max-width:520px;margin:auto;background:white;border-radius:10px;padding:30px;box-shadow:0 4px 12px rgba(0,0,0,0.1)'>"
            + "<div style='text-align:center;margin-bottom:20px'>"
            + "<h1 style='color:#2c7be5;margin:0;font-size:1.8em'>SpaceWork</h1>"
            + "<p style='color:#6c757d;margin:4px 0 0'>Sistema de Reservas</p>"
            + "</div>"
            + "<hr style='border:none;border-top:1px solid #dee2e6;margin:20px 0'>"
            + "<h2 style='color:#28a745;margin-top:0'>✅ Pago Confirmado</h2>"
            + "<p style='color:#495057'>Estimado/a <strong>" + nombre + "</strong>,</p>"
            + "<p style='color:#495057'>Su pago ha sido procesado exitosamente. A continuación el resumen:</p>"
            + "<table style='width:100%;border-collapse:collapse;margin:20px 0'>"
            + "<tr style='background:#e9f3ff'>"
            + "<td style='padding:12px 15px;border:1px solid #dee2e6;font-weight:bold;color:#495057'>N° de Reserva</td>"
            + "<td style='padding:12px 15px;border:1px solid #dee2e6;color:#2c7be5;font-weight:bold'>#" + idReserva + "</td>"
            + "</tr>"
            + "<tr>"
            + "<td style='padding:12px 15px;border:1px solid #dee2e6;font-weight:bold;color:#495057'>Monto Pagado</td>"
            + "<td style='padding:12px 15px;border:1px solid #dee2e6;color:#28a745;font-weight:bold;font-size:1.1em'>S/. " + String.format("%.2f", monto) + "</td>"
            + "</tr>"
            + "<tr style='background:#f8f9fa'>"
            + "<td style='padding:12px 15px;border:1px solid #dee2e6;font-weight:bold;color:#495057'>Método de Pago</td>"
            + "<td style='padding:12px 15px;border:1px solid #dee2e6'>" + metodo + "</td>"
            + "</tr>"
            + "</table>"
            + "<div style='background:#d4edda;border:1px solid #c3e6cb;border-radius:6px;padding:15px;margin-top:20px'>"
            + "<p style='margin:0;color:#155724;font-size:0.95em'>🏛️ Su espacio ha sido reservado y confirmado. ¡Disfrute su experiencia en SpaceWork!</p>"
            + "</div>"
            + "<hr style='border:none;border-top:1px solid #dee2e6;margin:20px 0'>"
            + "<p style='color:#adb5bd;font-size:0.8em;text-align:center;margin:0'>Este es un mensaje automático de SpaceWork · No responda este correo</p>"
            + "</div></body></html>";
    }

    public static String buildHtmlEvaluacion(String nombre, int idReserva, String enlaceEvaluacion) {
        return "<!DOCTYPE html>"
            + "<html><head><meta charset='utf-8'></head>"
            + "<body style='font-family:Arial,sans-serif;background:#f4f6f9;padding:20px;margin:0'>"
            + "<div style='max-width:520px;margin:auto;background:white;border-radius:10px;padding:30px;box-shadow:0 4px 12px rgba(0,0,0,0.1)'>"
            + "<div style='text-align:center;margin-bottom:20px'>"
            + "<h1 style='color:#2c7be5;margin:0;font-size:1.8em'>SpaceWork</h1>"
            + "<p style='color:#6c757d;margin:4px 0 0'>Sistema de Reservas</p>"
            + "</div>"
            + "<hr style='border:none;border-top:1px solid #dee2e6;margin:20px 0'>"
            + "<h2 style='color:#ff9800;margin-top:0'>⭐ ¿Cómo fue tu experiencia?</h2>"
            + "<p style='color:#495057'>Estimado/a <strong>" + nombre + "</strong>,</p>"
            + "<p style='color:#495057'>Tu reserva #" + idReserva + " se ha completado exitosamente. Nos gustaría conocer tu opinión sobre nuestro servicio.</p>"
            + "<p style='color:#495057;font-size:0.95em;margin-bottom:25px'>Por favor, dedica unos minutos para calificar tu experiencia:</p>"
            + "<div style='text-align:center;margin:25px 0'>"
            + "<a href='" + enlaceEvaluacion + "' style='display:inline-block;background:#ff9800;color:white;text-decoration:none;padding:15px 30px;border-radius:6px;font-weight:bold;font-size:1.05em'>Completar Evaluación</a>"
            + "</div>"
            + "<p style='color:#6c757d;font-size:0.9em;text-align:center;margin-top:20px'>O copia este enlace en tu navegador:</p>"
            + "<p style='background:#f8f9fa;padding:12px;border-radius:6px;word-break:break-all;font-size:0.8em;color:#495057'>" + enlaceEvaluacion + "</p>"
            + "<div style='background:#e3f2fd;border:1px solid #bbdefb;border-radius:6px;padding:15px;margin-top:20px'>"
            + "<p style='margin:0;color:#1565c0;font-size:0.95em'>📝 Tu participación nos ayuda a mejorar continuamente. ¡Gracias!</p>"
            + "</div>"
            + "<hr style='border:none;border-top:1px solid #dee2e6;margin:20px 0'>"
            + "<p style='color:#adb5bd;font-size:0.8em;text-align:center;margin:0'>Este es un mensaje automático de SpaceWork · No responda este correo</p>"
            + "</div></body></html>";
    }
}
