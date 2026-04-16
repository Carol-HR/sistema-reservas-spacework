package com.spacework.util;

/**
 * PRUEBA: Intenta enviar un correo REAL a tu email
 * Usando la configuración de mail.properties
 */
public class PruebaEnvioEmail {

    public static void main(String[] args) {
        System.out.println("\n" + repeat("=", 80));
        System.out.println("          ENVIANDO CORREO REAL DE PRUEBA A Tu EMAIL");
        System.out.println(repeat("=", 80) + "\n");

        try {
            String emailDestino = "juancmduel@gmail.com";
            String nombreCliente = "Juan Carlos";
            String nombreEspacio = "Sala de Reuniones Premium";
            String fechaReserva = "04/04/2026 14:00";
            String tokenEvaluacion = "a1b2c3d4-e5f6-4770-89ab-j1k2l3m4n5o6";

            System.out.println("📧 Enviando correo a: " + emailDestino);
            System.out.println("📝 Asunto: Califica tu experiencia en " + nombreEspacio + " ⭐\n");

            // Llamar al servicio de mail real
            boolean resultado = MailService.enviarTokenEvaluacion(
                    emailDestino,
                    nombreCliente,
                    nombreEspacio,
                    fechaReserva,
                    tokenEvaluacion
            );

            if (resultado) {
                System.out.println("\n✅ INTENTO DE ENVÍO COMPLETADO");
                System.out.println("\n📬 VERIFICACIÓN:");
                System.out.println("   ✓ Revisa tu inbox en: " + emailDestino);
                System.out.println("   ✓ Busca correos de: juancmduel@gmail.com");
                System.out.println("   ✓ Asunto: Califica tu experiencia en Sala de Reuniones Premium ⭐");
                System.out.println("\n💡 SI NO RECIBISTE EL CORREO:");
                System.out.println("   • Revisa la carpeta de SPAM/Correo no deseado");
                System.out.println("   • Verifica que la contraseña sea la correcta en mail.properties");
                System.out.println("   • Si usas Gmail, asegúrate de usar 'Contraseña de aplicación'");
            } else {
                System.out.println("\n⚠️  No se pudo enviar el correo");
                System.out.println("   Posibles causas:");
                System.out.println("   1. JavaMail no está disponible en el classpath");
                System.out.println("   2. Los datos de mail.properties son incorrectos");
                System.out.println("   3. No hay conexión a internet");
            }

            System.out.println("\n" + repeat("=", 80) + "\n");

        } catch (Exception e) {
            System.err.println("❌ Error al enviar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
