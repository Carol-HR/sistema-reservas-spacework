package com.spacework.util;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CSVExporter {

    public static void exportarTabla(JTable tabla, String titulo) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar como CSV");
        fileChooser.setSelectedFile(new File(titulo + ".csv"));
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int resultado = fileChooser.showSaveDialog(null);
        if (resultado == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                // Encabezados
                for (int col = 0; col < tabla.getColumnCount(); col++) {
                    if (col > 0) writer.write(",");
                    writer.write("\"" + tabla.getColumnName(col) + "\"");
                }
                writer.write("\n");

                // Datos
                for (int row = 0; row < tabla.getRowCount(); row++) {
                    for (int col = 0; col < tabla.getColumnCount(); col++) {
                        if (col > 0) writer.write(",");
                        Object value = tabla.getValueAt(row, col);
                        writer.write("\"" + (value == null ? "" : value.toString().replace("\"", "\"\"")) + "\"");
                    }
                    writer.write("\n");
                }
                javax.swing.JOptionPane.showMessageDialog(null, "Archivo guardado en: " + file.getAbsolutePath(), "Éxito", javax.swing.JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                javax.swing.JOptionPane.showMessageDialog(null, "Error al guardar: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
