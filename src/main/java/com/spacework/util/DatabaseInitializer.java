package com.spacework.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void inicializar() {
        try (Connection conn = Conexion.getConexion()) {
            if (!tableExists(conn, "ROLES")) {
                crearRoles(conn);
            }
            if (!columnExists(conn, "USUARIOS", "ROL")) {
                agregarRolAUsuarios(conn);
            }
            if (!tableExists(conn, "HORARIOS_BLOQUEADOS")) {
                crearHorariosBloqueados(conn);
            }
            System.out.println("✓ Base de datos inicializada correctamente.");
        } catch (SQLException ex) {
            System.err.println("Error al inicializar BD: " + ex.getMessage());
        }
    }

    private static void crearRoles(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE ROLES (" +
                    "id_rol NUMBER PRIMARY KEY, " +
                    "nombre VARCHAR2(50) NOT NULL UNIQUE, " +
                    "descripcion VARCHAR2(255), " +
                    "fecha_creacion DATE DEFAULT SYSDATE)");
            stmt.execute("CREATE SEQUENCE SEQ_ROLES START WITH 1 INCREMENT BY 1 NOCACHE");
            stmt.execute("INSERT INTO ROLES VALUES (1, 'ADMIN', 'Administrador con acceso total', SYSDATE)");
            stmt.execute("INSERT INTO ROLES VALUES (2, 'GERENTE', 'Gerente con acceso a reportes', SYSDATE)");
            stmt.execute("INSERT INTO ROLES VALUES (3, 'CLIENTE', 'Cliente con acceso limitado', SYSDATE)");
            conn.commit();
            System.out.println("✓ Tabla ROLES creada.");
        }
    }

    private static void agregarRolAUsuarios(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE USUARIOS ADD (rol VARCHAR2(20) DEFAULT 'CLIENTE')");
            stmt.execute("UPDATE USUARIOS SET rol = 'ADMIN' WHERE nombre_usuario = 'admin'");
            conn.commit();
            System.out.println("✓ Columna ROL agregada a USUARIOS.");
        }
    }

    private static void crearHorariosBloqueados(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE HORARIOS_BLOQUEADOS (" +
                    "id_bloqueo NUMBER PRIMARY KEY, " +
                    "id_espacio NUMBER NOT NULL, " +
                    "fecha_inicio DATE NOT NULL, " +
                    "fecha_fin DATE NOT NULL, " +
                    "razon VARCHAR2(255), " +
                    "fecha_creacion DATE DEFAULT SYSDATE, " +
                    "usuario_creador VARCHAR2(100), " +
                    "FOREIGN KEY (id_espacio) REFERENCES ESPACIOS(id_espacio))");
            stmt.execute("CREATE SEQUENCE SEQ_BLOQUEOS START WITH 1 INCREMENT BY 1 NOCACHE");
            conn.commit();
            System.out.println("✓ Tabla HORARIOS_BLOQUEADOS creada.");
        }
    }

    private static boolean tableExists(Connection conn, String tableName) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT 1 FROM " + tableName + " WHERE ROWNUM = 1");
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }

    private static boolean columnExists(Connection conn, String tableName, String columnName) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT " + columnName + " FROM " + tableName + " WHERE ROWNUM = 1");
            return true;
        } catch (SQLException ex) {
            return false;
        }
    }
}
