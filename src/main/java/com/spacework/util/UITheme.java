package com.spacework.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class UITheme {

    // Paleta de colores
    public static final Color PRIMARY        = new Color(37, 99, 235);   // Azul moderno
    public static final Color PRIMARY_DARK   = new Color(29, 78, 216);
    public static final Color PRIMARY_LIGHT  = new Color(219, 234, 254);
    public static final Color SUCCESS        = new Color(22, 163, 74);
    public static final Color DANGER         = new Color(220, 38, 38);
    public static final Color WARNING        = new Color(234, 179, 8);
    public static final Color BG_MAIN        = new Color(248, 250, 252);  // Fondo general
    public static final Color BG_CARD        = Color.WHITE;
    public static final Color BG_TABLE_EVEN  = new Color(249, 250, 251);
    public static final Color TEXT_PRIMARY   = new Color(17, 24, 39);
    public static final Color TEXT_SECONDARY = new Color(107, 114, 128);
    public static final Color BORDER_COLOR   = new Color(229, 231, 235);
    public static final Color HEADER_BG      = new Color(37, 99, 235);
    public static final Color HEADER_FG      = Color.WHITE;
    public static final Color SELECTED_BG    = new Color(219, 234, 254);
    public static final Color SELECTED_FG    = new Color(29, 78, 216);

    // Fuentes
    public static final Font FONT_TITLE      = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_SUBTITLE   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_LABEL      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_LABEL_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_INPUT      = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BUTTON     = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_TABLE      = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_TABLE_HDR  = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_SECTION    = new Font("Segoe UI", Font.BOLD, 14);

    // Borders
    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)
        );
    }

    public static Border inputBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        );
    }

    public static Border inputFocusBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY, 2, true),
            BorderFactory.createEmptyBorder(5, 9, 5, 9)
        );
    }

    // Fábricas de componentes estilizados

    public static JLabel makeTitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(PRIMARY);
        return l;
    }

    public static JLabel makeSubtitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_SUBTITLE);
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    public static JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

    public static JLabel makeSectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_SECONDARY);
        return l;
    }

    public static JTextField makeTextField(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(FONT_INPUT);
        f.setBorder(inputBorder());
        f.setBackground(BG_CARD);
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { f.setBorder(inputFocusBorder()); }
            public void focusLost(java.awt.event.FocusEvent e)   { f.setBorder(inputBorder()); }
        });
        return f;
    }

    public static JPasswordField makePasswordField(int cols) {
        JPasswordField f = new JPasswordField(cols);
        f.setFont(FONT_INPUT);
        f.setBorder(inputBorder());
        f.setBackground(BG_CARD);
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) { f.setBorder(inputFocusBorder()); }
            public void focusLost(java.awt.event.FocusEvent e)   { f.setBorder(inputBorder()); }
        });
        return f;
    }

    public static JComboBox<String> makeComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_INPUT);
        cb.setBackground(BG_CARD);
        return cb;
    }

    /** Botón primario (azul relleno) */
    public static JButton makePrimaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BUTTON);
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(PRIMARY_DARK); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(PRIMARY); }
        });
        return b;
    }

    /** Botón secundario (contorno gris) */
    public static JButton makeSecondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BUTTON);
        b.setBackground(BG_CARD);
        b.setForeground(TEXT_PRIMARY);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(BG_TABLE_EVEN); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(BG_CARD); }
        });
        return b;
    }

    /** Botón de peligro (rojo) */
    public static JButton makeDangerButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BUTTON);
        b.setBackground(new Color(254, 242, 242));
        b.setForeground(DANGER);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(252, 165, 165), 1),
            BorderFactory.createEmptyBorder(8, 18, 8, 18)
        ));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(new Color(254, 226, 226)); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(new Color(254, 242, 242)); }
        });
        return b;
    }

    /** Botón de éxito (verde) */
    public static JButton makeSuccessButton(String text) {
        JButton b = new JButton(text);
        b.setFont(FONT_BUTTON);
        b.setBackground(SUCCESS);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { b.setBackground(new Color(15, 118, 53)); }
            public void mouseExited(java.awt.event.MouseEvent e)  { b.setBackground(SUCCESS); }
        });
        return b;
    }

    /** Aplica estilo moderno a una JTable (compatible con Nimbus y otros L&F) */
    public static void styleTable(JTable table) {
        table.setFont(FONT_TABLE);
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(SELECTED_BG);
        table.setSelectionForeground(SELECTED_FG);
        table.setBackground(BG_CARD);
        table.setFillsViewportHeight(true);
        table.setGridColor(BORDER_COLOR);

        // Header con renderer propio para forzar color bajo Nimbus
        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(header.getWidth(), 36));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            {
                setHorizontalAlignment(SwingConstants.LEFT);
                setOpaque(true);
            }
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setText(value == null ? "" : value.toString());
                setFont(FONT_TABLE_HDR);
                setForeground(HEADER_FG);
                setBackground(HEADER_BG);
                setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 0, PRIMARY_DARK),
                    BorderFactory.createEmptyBorder(0, 10, 0, 10)
                ));
                return this;
            }
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(HEADER_BG);
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        });

        // Renderer con filas alternadas — setOpaque garantiza que Nimbus no pise el color
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                setOpaque(true);
                if (isSelected) {
                    setBackground(SELECTED_BG);
                    setForeground(SELECTED_FG);
                } else {
                    setBackground(row % 2 == 0 ? BG_CARD : BG_TABLE_EVEN);
                    setForeground(TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
    }

    /** Panel tipo "card" con fondo blanco, borde suave y sombra simulada */
    public static JPanel makeCard() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(cardBorder());
        return p;
    }

    /** Panel de fondo principal */
    public static JPanel makeMainPanel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(BG_MAIN);
        return p;
    }
}
