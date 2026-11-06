package sara;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import sara.vista.LoginFrame;

/*
 * Clase Main
 * Punto de entrada de SARA: aplica el aspecto del sistema operativo
 * y abre la ventana de login.
 */
public class Main {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // si falla se usa el aspecto por defecto de Java
        }

        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
