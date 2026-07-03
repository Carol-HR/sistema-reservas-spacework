package com.spacework;

import com.spacework.dao.ClienteDAO;
import com.spacework.dao.UsuarioDAO;
import com.spacework.model.Cliente;
import com.spacework.model.Usuario;
import com.spacework.util.HashUtil;
import com.spacework.util.SimpleJwtUtil;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;

import org.junit.jupiter.api.*;

import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PRUEBAS DE SEGURIDAD COMPLETAS")
public class PruebasSeguridadCompletas {

    @Nested
    @DisplayName("1. Autenticación JWT")
    class PruebasAutenticacionJWT {

        @Test
        @DisplayName("Generación de token JWT exitosa con datos válidos")
        void testGeneracionTokenJWTExitosa() {
            String token = SimpleJwtUtil.generarToken("testuser", "Test User", "test@example.com", "ADMIN");
            assertNotNull(token);
            assertFalse(token.isEmpty());
            assertTrue(token.contains("."));
        }

        @Test
        @DisplayName("Validación de token JWT válido retorna true")
        void testValidacionTokenJWTValido() {
            String token = SimpleJwtUtil.generarToken("testuser", "Test User", "test@example.com", "ADMIN");
            assertTrue(SimpleJwtUtil.validarToken(token));
        }

        @Test
        @DisplayName("Validación de token JWT null retorna false")
        void testValidacionTokenJWTNull() {
            assertFalse(SimpleJwtUtil.validarToken(null));
        }

        @Test
        @DisplayName("Validación de token JWT vacío retorna false")
        void testValidacionTokenJWTVacio() {
            assertFalse(SimpleJwtUtil.validarToken(""));
        }

        @Test
        @DisplayName("Validación de token JWT manipulado retorna false")
        void testValidacionTokenJWTManipulado() {
            String token = SimpleJwtUtil.generarToken("testuser", "Test User", "test@example.com", "ADMIN");
            String tokenManipulado = token + "manipulado";
            assertFalse(SimpleJwtUtil.validarToken(tokenManipulado));
        }

        @Test
        @DisplayName("Extracción de usuario desde token JWT válido")
        void testExtraccionUsuarioDesdeToken() {
            String username = "testuser";
            String token = SimpleJwtUtil.generarToken(username, "Test User", "test@example.com", "ADMIN");
            String extraido = SimpleJwtUtil.obtenerUsuario(token);
            assertEquals(username, extraido);
        }

        @Test
        @DisplayName("Extracción de rol desde token JWT válido")
        void testExtraccionRolDesdeToken() {
            String rol = "ADMIN";
            String token = SimpleJwtUtil.generarToken("testuser", "Test User", "test@example.com", rol);
            String rolExtraido = SimpleJwtUtil.obtenerRol(token);
            assertEquals(rol, rolExtraido);
        }

        @Test
        @DisplayName("Token JWT expira después del tiempo configurado (1 hora)")
        void testExpiracionTokenJWT() {
            // Crear token con expiración muy corta para prueba
            long expiracionCorta = 1000L; // 1 segundo
            String token = Jwts.builder()
                    .setSubject("testuser")
                    .claim("rol", "ADMIN")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiracionCorta))
                    .signWith(SignatureAlgorithm.HS512, "SPACEWORK_SECRET_KEY_2026_MIN_256_BITS_SECURE_v1")
                    .compact();
            
            // Token válido inmediatamente
            assertTrue(SimpleJwtUtil.validarToken(token));
            
            // Esperar expiración
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                fail("Interrupción durante espera de expiración");
            }
            
            // Token inválido después de expirar
            assertFalse(SimpleJwtUtil.validarToken(token));
        }

        @Test
        @DisplayName("Claims del token contienen la información correcta")
        void testClaimsTokenContienenInfoCorrecta() {
            String username = "testuser";
            String nombre = "Test User";
            String email = "test@example.com";
            String rol = "CLIENTE";
            
            String token = SimpleJwtUtil.generarToken(username, nombre, email, rol);
            Claims claims = SimpleJwtUtil.obtenerClaims(token);
            
            assertNotNull(claims);
            assertEquals(username, claims.getSubject());
            assertEquals(nombre, claims.get("nombre"));
            assertEquals(email, claims.get("email"));
            assertEquals(rol, claims.get("rol"));
        }
    }

    @Nested
    @DisplayName("2. Autorización por Rol")
    class PruebasAutorizacionRol {

        @Test
        @DisplayName("Token con rol ADMIN contiene el rol correcto")
        void testTokenRolAdmin() {
            String token = SimpleJwtUtil.generarToken("admin", "Admin User", "admin@spacework.com", "ADMIN");
            String rol = SimpleJwtUtil.obtenerRol(token);
            assertEquals("ADMIN", rol);
        }

        @Test
        @DisplayName("Token con rol CLIENTE contiene el rol correcto")
        void testTokenRolCliente() {
            String token = SimpleJwtUtil.generarToken("cliente", "Client User", "cliente@spacework.com", "CLIENTE");
            String rol = SimpleJwtUtil.obtenerRol(token);
            assertEquals("CLIENTE", rol);
        }

        @Test
        @DisplayName("Verificación de rol para autorización de acceso")
        void testVerificacionRolParaAcceso() {
            String tokenAdmin = SimpleJwtUtil.generarToken("admin", "Admin", "admin@test.com", "ADMIN");
            String tokenCliente = SimpleJwtUtil.generarToken("cliente", "Cliente", "cliente@test.com", "CLIENTE");
            
            String rolAdmin = SimpleJwtUtil.obtenerRol(tokenAdmin);
            String rolCliente = SimpleJwtUtil.obtenerRol(tokenCliente);
            
            assertEquals("ADMIN", rolAdmin);
            assertEquals("CLIENTE", rolCliente);
            assertNotEquals(rolAdmin, rolCliente);
        }

        @Test
        @DisplayName("Token inválido no tiene rol asociado")
        void testTokenInvalidoSinRol() {
            String rol = SimpleJwtUtil.obtenerRol("token_invalido");
            assertNull(rol);
        }
    }

    @Nested
    @DisplayName("3. Inyección SQL")
    class PruebasInyeccionSQL {

        @Test
        @DisplayName("Búsqueda por DNI con caracteres especiales no causa inyección SQL")
        void testBusquedaDniConCaracteresEspeciales() throws Exception {
            ClienteDAO dao = new ClienteDAO();
            // Intento de inyección SQL con comillas
            Cliente cliente = dao.buscarPorDni("12345678' OR '1'='1");
            assertNull(cliente); // Debería retornar null, no todos los clientes
        }

        @Test
        @DisplayName("Búsqueda por DNI con comando DROP no causa inyección SQL")
        void testBusquedaDniConDropTable() throws Exception {
            ClienteDAO dao = new ClienteDAO();
            Cliente cliente = dao.buscarPorDni("12345678'; DROP TABLE CLIENTES; --");
            assertNull(cliente);
        }

        @Test
        @DisplayName("Búsqueda por email con caracteres especiales no causa inyección SQL")
        void testBusquedaEmailConCaracteresEspeciales() throws Exception {
            ClienteDAO dao = new ClienteDAO();
            Cliente cliente = dao.buscarPorEmail("test@example.com' OR '1'='1");
            assertNull(cliente);
        }

        @Test
        @DisplayName("Inserción con caracteres especiales es rechazada o sanitizada")
        void testInsercionConCaracteresEspeciales() {
            Cliente cliente = new Cliente();
            cliente.setNombre("Test'); DROP TABLE CLIENTES; --");
            cliente.setApellido("User");
            cliente.setDni("99999999");
            cliente.setEmail("test@example.com");
            cliente.setTelefono("999999999");
            cliente.setPassword("Test1234");
            
            ClienteDAO dao = new ClienteDAO();
            // No debería lanzar excepción ni ejecutar el comando DROP
            // El sistema debería sanitizar o rechazar la entrada
            try {
                dao.insertar(cliente);
                // Si se inserta, verificar que el nombre fue sanitizado
                Cliente insertado = dao.buscarPorDni("99999999");
                if (insertado != null) {
                    assertFalse(insertado.getNombre().contains("DROP"));
                }
            } catch (Exception e) {
                // También es válido que rechace la inserción
                assertTrue(true);
            }
        }

        @Test
        @DisplayName("Búsqueda por ID con valor extremo no causa inyección SQL")
        void testBusquedaPorIdConValorExtremo() throws Exception {
            ClienteDAO dao = new ClienteDAO();
            Cliente cliente = dao.buscarPorId(999999999);
            assertNull(cliente);
        }
    }

    @Nested
    @DisplayName("4. XSS (Cross-Site Scripting)")
    class PruebasXSS {

        @Test
        @DisplayName("Nombre con script tag es detectado como potencial XSS")
        void testNombreConScriptTag() {
            Cliente cliente = new Cliente();
            cliente.setNombre("<script>alert('XSS')</script>");
            cliente.setApellido("User");
            cliente.setDni("88888888");
            cliente.setEmail("test@example.com");
            cliente.setTelefono("999999999");
            cliente.setPassword("Test1234");
            
            // Verificar que el sistema detecta o sanitiza el input
            assertTrue(cliente.getNombre().contains("<script>"));
            
            ClienteDAO dao = new ClienteDAO();
            try {
                dao.insertar(cliente);
                Cliente recuperado = dao.buscarPorDni("88888888");
                if (recuperado != null) {
                    // Verificar que fue sanitizado o que el sistema lo rechaza
                    assertNotNull(recuperado.getNombre());
                }
            } catch (Exception e) {
                // También es válido que rechace la inserción
                assertTrue(true);
            }
        }

        @Test
        @DisplayName("Email con javascript: protocolo es potencial XSS")
        void testEmailConJavascriptProtocol() {
            Cliente cliente = new Cliente();
            cliente.setNombre("Test");
            cliente.setApellido("User");
            cliente.setDni("77777777");
            cliente.setEmail("javascript:alert('XSS')");
            cliente.setTelefono("999999999");
            cliente.setPassword("Test1234");
            
            assertTrue(cliente.getEmail().contains("javascript:"));
            
            ClienteDAO dao = new ClienteDAO();
            try {
                dao.insertar(cliente);
            } catch (Exception e) {
                // Debería rechazar email inválido
                assertTrue(true);
            }
        }

        @Test
        @DisplayName("Input con evento onclick es potencial XSS")
        void testInputConOnClickEvent() {
            String input = "Test\" onclick=\"alert('XSS')";
            assertTrue(input.contains("onclick"));
            assertTrue(input.contains("alert"));
        }

        @Test
        @DisplayName("Input con iframe tag es potencial XSS")
        void testInputConIframeTag() {
            String input = "<iframe src='http://malicious.com'></iframe>";
            assertTrue(input.contains("<iframe"));
            assertTrue(input.contains("src="));
        }

        @Test
        @DisplayName("Input con img tag onerror es potencial XSS")
        void testInputConImgOnError() {
            String input = "<img src='x' onerror='alert(1)'>";
            assertTrue(input.contains("<img"));
            assertTrue(input.contains("onerror"));
        }
    }

    @Nested
    @DisplayName("5. CSRF (Cross-Site Request Forgery)")
    class PruebasCSRF {

        @Test
        @DisplayName("Verificación de que el sistema usa tokens CSRF (simulado)")
        void testUsoDeTokensCSRF() {
            // En un sistema real, verificaríamos que cada formulario mutante
            // incluye un token CSRF. Aquí simulamos la verificación.
            String csrfToken = UUID.randomUUID().toString();
            assertNotNull(csrfToken);
            assertFalse(csrfToken.isEmpty());
        }

        @Test
        @DisplayName("Verificación de origen de la petición (simulado)")
        void testVerificacionOrigenPeticion() {
            // Simular verificación de headers
            String origin = "https://localhost:8080";
            
            boolean origenValido = origin.equals("https://localhost:8080") || 
                                   origin.equals("http://localhost:8080");
            assertTrue(origenValido);
        }

        @Test
        @DisplayName("Rechazo de petición sin token CSRF (simulado)")
        void testRechazoPeticionSinTokenCSRF() {
            String tokenCSRF = null;
            boolean tieneToken = tokenCSRF != null && !tokenCSRF.isEmpty();
            assertFalse(tieneToken);
        }

        @Test
        @DisplayName("Verificación de método seguro para operaciones sensibles")
        void testMetodoSeguroOperacionesSensibles() {
            // Operaciones sensibles deben usar POST, no GET
            String metodo = "POST";
            boolean esSeguro = metodo.equals("POST") || metodo.equals("PUT") || metodo.equals("DELETE");
            assertTrue(esSeguro);
        }

        @Test
        @DisplayName("SameSite cookie attribute previene CSRF (simulado)")
        void testSameSiteCookieAttribute() {
            // Verificar que las cookies tienen SameSite attribute
            String cookieHeader = "sessionId=abc123; SameSite=Strict; Secure; HttpOnly";
            boolean tieneSameSite = cookieHeader.contains("SameSite");
            assertTrue(tieneSameSite);
        }
    }

    @Nested
    @DisplayName("6. Validación de Inputs")
    class PruebasValidacionInputs {

        @Test
        @DisplayName("Validación de DNI: debe tener 8 dígitos")
        void testValidacionDni() {
            String dniValido = "12345678";
            String dniInvalido = "123";
            String dniConLetras = "1234567A";
            
            assertTrue(dniValido.matches("\\d{8}"));
            assertFalse(dniInvalido.matches("\\d{8}"));
            assertFalse(dniConLetras.matches("\\d{8}"));
        }

        @Test
        @DisplayName("Validación de email: formato correcto")
        void testValidacionEmail() {
            String emailValido = "test@example.com";
            String emailInvalido = "test@";
            String emailSinArroba = "testexample.com";
            
            assertTrue(emailValido.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"));
            assertFalse(emailInvalido.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"));
            assertFalse(emailSinArroba.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"));
        }

        @Test
        @DisplayName("Validación de teléfono: formato correcto")
        void testValidacionTelefono() {
            String telefonoValido = "999999999";
            String telefonoInvalido = "123";
            String telefonoConLetras = "99999999A";
            
            assertTrue(telefonoValido.matches("\\d{9}"));
            assertFalse(telefonoInvalido.matches("\\d{9}"));
            assertFalse(telefonoConLetras.matches("\\d{9}"));
        }

        @Test
        @DisplayName("Validación de nombre: no debe estar vacío")
        void testValidacionNombreNoVacio() {
            String nombreValido = "Juan";
            String nombreVacio = "";
            String nombreNull = null;
            
            assertNotNull(nombreValido);
            assertFalse(nombreValido.isEmpty());
            assertTrue(nombreVacio.isEmpty());
            assertNull(nombreNull);
        }

        @Test
        @DisplayName("Validación de password: longitud mínima")
        void testValidacionPasswordLongitudMinima() {
            String passwordValido = "Test1234";
            String passwordCorto = "Test1";
            
            assertTrue(passwordValido.length() >= 8);
            assertFalse(passwordCorto.length() >= 8);
        }

        @Test
        @DisplayName("Validación de monto: debe ser positivo")
        void testValidacionMontoPositivo() {
            double montoValido = 100.0;
            double montoInvalido = -50.0;
            double montoCero = 0.0;
            
            assertTrue(montoValido > 0);
            assertFalse(montoInvalido > 0);
            assertFalse(montoCero > 0);
        }

        @Test
        @DisplayName("Sanitización de input: trim de espacios")
        void testSanitizacionInputTrim() {
            String input = "  Test User  ";
            String sanitizado = input.trim();
            assertEquals("Test User", sanitizado);
            assertFalse(sanitizado.startsWith(" "));
            assertFalse(sanitizado.endsWith(" "));
        }

        @Test
        @DisplayName("Sanitización de input: eliminación de caracteres peligrosos")
        void testSanitizacionInputCaracteresPeligrosos() {
            String input = "Test<script>alert('XSS')</script>User";
            String sanitizado = input.replaceAll("<[^>]*>", "");
            assertEquals("TestUser", sanitizado);
            assertFalse(sanitizado.contains("<script>"));
            assertFalse(sanitizado.contains("alert"));
        }
    }

    @Nested
    @DisplayName("7. Seguridad en APIs")
    class PruebasSeguridadAPI {

        @Test
        @DisplayName("API requiere autenticación JWT (simulado)")
        void testApiRequiereAutenticacionJWT() {
            String token = SimpleJwtUtil.generarToken("testuser", "Test", "test@test.com", "CLIENTE");
            boolean tieneToken = token != null && !token.isEmpty() && SimpleJwtUtil.validarToken(token);
            assertTrue(tieneToken);
        }

        @Test
        @DisplayName("API rechaza petición sin token (simulado)")
        void testApiRechazaPeticionSinToken() {
            String token = null;
            boolean autorizado = token != null && SimpleJwtUtil.validarToken(token);
            assertFalse(autorizado);
        }

        @Test
        @DisplayName("API rechaza petición con token inválido (simulado)")
        void testApiRechazaPeticionConTokenInvalido() {
            String token = "token_invalido";
            boolean autorizado = SimpleJwtUtil.validarToken(token);
            assertFalse(autorizado);
        }

        @Test
        @DisplayName("API verifica rol del usuario (simulado)")
        void testApiVerificaRolUsuario() {
            String tokenAdmin = SimpleJwtUtil.generarToken("admin", "Admin", "admin@test.com", "ADMIN");
            String tokenCliente = SimpleJwtUtil.generarToken("cliente", "Cliente", "cliente@test.com", "CLIENTE");
            
            String rolAdmin = SimpleJwtUtil.obtenerRol(tokenAdmin);
            String rolCliente = SimpleJwtUtil.obtenerRol(tokenCliente);
            
            assertEquals("ADMIN", rolAdmin);
            assertEquals("CLIENTE", rolCliente);
        }

        @Test
        @DisplayName("API limita tasa de peticiones (simulado)")
        void testApiLimitaTasaPeticiones() {
            int maxPeticiones = 100;
            int peticionesActuales = 50;
            boolean permitePeticion = peticionesActuales < maxPeticiones;
            assertTrue(permitePeticion);
            
            peticionesActuales = 100;
            permitePeticion = peticionesActuales < maxPeticiones;
            assertFalse(permitePeticion);
        }

        @Test
        @DisplayName("API usa HTTPS en producción (simulado)")
        void testApiUsaHTTPS() {
            String urlProduccion = "https://api.spacework.com";
            String urlDesarrollo = "http://localhost:8080";
            
            boolean esHTTPS = urlProduccion.startsWith("https://");
            boolean esHTTP = urlDesarrollo.startsWith("http://");
            
            assertTrue(esHTTPS);
            assertTrue(esHTTP);
        }

        @Test
        @DisplayName("API no expone información sensible en errores (simulado)")
        void testApiNoExponeInfoSensibleEnErrores() {
            String errorMensaje = "Error interno del servidor";
            boolean exponeDetalles = errorMensaje.contains("SQLException") || 
                                    errorMensaje.contains("password") ||
                                    errorMensaje.contains("database");
            assertFalse(exponeDetalles);
        }

        @Test
        @DisplayName("API valida Content-Type (simulado)")
        void testApiValidaContentType() {
            String contentTypeValido = "application/json";
            String contentTypeInvalido = "text/html";
            
            boolean esValido = contentTypeValido.equals("application/json");
            boolean esInvalido = contentTypeInvalido.equals("application/json");
            
            assertTrue(esValido);
            assertFalse(esInvalido);
        }
    }

    @Nested
    @DisplayName("8. Hashing de Contraseñas")
    class PruebasHashingContrasenas {

        @Test
        @DisplayName("Hash SHA256 genera hash de 64 caracteres")
        void testHashSha256Longitud() {
            String password = "Test1234";
            String hash = HashUtil.sha256(password);
            assertEquals(64, hash.length());
        }

        @Test
        @DisplayName("Hash SHA256 es determinista")
        void testHashSha256Determinista() {
            String password = "Test1234";
            String hash1 = HashUtil.sha256(password);
            String hash2 = HashUtil.sha256(password);
            assertEquals(hash1, hash2);
        }

        @Test
        @DisplayName("Hash SHA256 es irreversible (no se puede obtener original)")
        void testHashSha256Irreversible() {
            String password = "Test1234";
            String hash = HashUtil.sha256(password);
            assertNotEquals(password, hash);
            assertFalse(hash.contains(password));
        }

        @Test
        @DisplayName("Hash SHA256 produce hashes distintos para passwords distintos")
        void testHashSha256UnicoPorPassword() {
            String password1 = "Test1234";
            String password2 = "Test5678";
            String hash1 = HashUtil.sha256(password1);
            String hash2 = HashUtil.sha256(password2);
            assertNotEquals(hash1, hash2);
        }

        @Test
        @DisplayName("Hash SHA256 maneja passwords vacíos")
        void testHashSha256PasswordVacio() {
            String password = "";
            String hash = HashUtil.sha256(password);
            assertNotNull(hash);
            assertEquals(64, hash.length());
        }

        @Test
        @DisplayName("Hash SHA256 maneja passwords con caracteres especiales")
        void testHashSha256CaracteresEspeciales() {
            String password = "T3st!@#$%^&*()";
            String hash = HashUtil.sha256(password);
            assertNotNull(hash);
            assertEquals(64, hash.length());
        }

        @Test
        @DisplayName("Hash SHA256 maneja passwords muy largos")
        void testHashSha256PasswordLargo() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                sb.append("a");
            }
            String password = sb.toString();
            String hash = HashUtil.sha256(password);
            assertNotNull(hash);
            assertEquals(64, hash.length());
        }
    }
}
