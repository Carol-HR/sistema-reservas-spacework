package com.spacework.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Claims;

import java.util.Date;

/**
 * JWT Util simplificado sin dependencias de Spring
 * Usa una clave fija para generar y validar tokens
 */
public class SimpleJwtUtil {
    // IMPORTANTE: Esta clave debe ser la misma en generación y validación
    private static final String JWT_SECRET = "SPACEWORK_SECRET_KEY_2026_MIN_256_BITS_SECURE_v1";
    private static final long JWT_EXPIRATION_MS = 3600000L; // 1 hora

    /**
     * Genera un token JWT
     */
    public static String generarToken(String username, String nombre, String email, String rol) {
        try {
            return Jwts.builder()
                    .setSubject(username)
                    .claim("nombre", nombre)
                    .claim("email", email)
                    .claim("rol", rol)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION_MS))
                    .signWith(SignatureAlgorithm.HS512, JWT_SECRET)
                    .compact();
        } catch (Exception e) {
            System.err.println("[JWT Error] Error generando token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Valida un token JWT
     */
    public static boolean validarToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        try {
            Jwts.parser()
                    .setSigningKey(JWT_SECRET)
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            System.err.println("[JWT Error] Token inválido: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("[JWT Error] Error validando token: " + e.getMessage());
            return false;
        }
    }

    /**
     * Extrae el usuario del token
     */
    public static String obtenerUsuario(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(JWT_SECRET)
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extrae los claims del token
     */
    public static Claims obtenerClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(JWT_SECRET)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extrae el rol del token
     */
    public static String obtenerRol(String token) {
        Claims claims = obtenerClaims(token);
        return claims != null ? (String) claims.get("rol") : null;
    }
}
