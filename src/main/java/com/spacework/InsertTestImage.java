import java.sql.Connection;
import java.sql.PreparedStatement;

public class InsertTestImage {
    public static void main(String[] args) {
        try {
            // Usar la clase Conexion del proyecto
            Connection conn = com.spacework.util.Conexion.getConexion();
            if (conn == null) {
                System.out.println("❌ No se pudo conectar a la BD");
                return;
            }
            
            // Imagen base64 PNG rojo de prueba
            String imagenBase64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFBQIAX8jx0gAAAABJRU5ErkJggg==";
            
            // Actualizar el espacio 46 con la imagen
            String sql = "UPDATE ESPACIOS SET imagen_url = ? WHERE id_espacio = 46";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, imagenBase64);
            int rows = ps.executeUpdate();
            conn.commit();
            
            System.out.println("✅ Imagen de prueba insertada en espacio 46");
            System.out.println("   Filas actualizadas: " + rows);
            
            ps.close();
            com.spacework.util.Conexion.cerrar(conn);
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
