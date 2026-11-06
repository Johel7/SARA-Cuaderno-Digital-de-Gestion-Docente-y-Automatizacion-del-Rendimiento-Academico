package sara.util;

import java.awt.Component;
import java.sql.SQLException;

import javax.swing.JOptionPane;

/*
 * Clase Mensajes
 * Cuadros de dialogo que se repiten en todo el sistema.
 */
public class Mensajes {

    public static void info(Component padre, String texto) {
        JOptionPane.showMessageDialog(padre, texto, "SARA", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void advertencia(Component padre, String texto) {
        JOptionPane.showMessageDialog(padre, texto, "Atención", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component padre, String texto) {
        JOptionPane.showMessageDialog(padre, texto, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void errorBD(Component padre, SQLException ex) {
        JOptionPane.showMessageDialog(padre,
                "Ocurrió un problema con la base de datos:\n" + ex.getMessage(),
                "Error de base de datos", JOptionPane.ERROR_MESSAGE);
    }

    // Pregunta Si/No. Devuelve true si el usuario eligio Si
    public static boolean confirmar(Component padre, String pregunta) {
        int r = JOptionPane.showConfirmDialog(padre, pregunta, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return r == JOptionPane.YES_OPTION;
    }
}
