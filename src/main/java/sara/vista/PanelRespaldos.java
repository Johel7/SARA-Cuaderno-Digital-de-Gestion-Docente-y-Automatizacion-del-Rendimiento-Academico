package sara.vista;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import sara.dao.RespaldoDAO;
import sara.util.Archivos;
import sara.util.Mensajes;

/*
 * Clase PanelRespaldos
 * El administrador guarda una copia de seguridad de toda la base de
 * datos en un archivo .sql y puede restaurarla cuando haga falta.
 */
public class PanelRespaldos extends JPanel {

    private RespaldoDAO respaldoDAO = new RespaldoDAO();
    private JTextArea txtRegistro = new JTextArea(8, 50);

    public PanelRespaldos() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnCrear = new JButton("Crear respaldo");
        JButton btnRestaurar = new JButton("Restaurar respaldo");
        barra.add(btnCrear);
        barra.add(btnRestaurar);
        add(barra, BorderLayout.NORTH);

        JLabel explicacion = new JLabel("<html>Guarde respaldos con frecuencia, por ejemplo al terminar cada corte.<br>"
                + "<b>Restaurar</b> reemplaza TODOS los datos actuales por los del archivo elegido.</html>");
        add(explicacion, BorderLayout.CENTER);

        txtRegistro.setEditable(false);
        txtRegistro.setBorder(new EmptyBorder(6, 6, 6, 6));
        add(txtRegistro, BorderLayout.SOUTH);

        btnCrear.addActionListener(e -> crear());
        btnRestaurar.addActionListener(e -> restaurar());
    }

    private void crear() {
        File archivo = Archivos.elegirDondeGuardar(this, "sara_respaldo_" + LocalDate.now(),
                "Respaldo SQL (*.sql)", "sql");
        if (archivo == null) {
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            int filas = respaldoDAO.crearRespaldo(archivo);
            registrar("Respaldo creado: " + archivo.getAbsolutePath() + " (" + filas + " registros)");
            Mensajes.info(this, "Respaldo creado correctamente.\n" + filas + " registros guardados.");
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        } catch (IOException ex) {
            Mensajes.error(this, "No se pudo escribir el archivo:\n" + ex.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void restaurar() {
        File archivo = Archivos.elegirArchivoAbrir(this, "Respaldo SQL (*.sql)", "sql");
        if (archivo == null) {
            return;
        }

        if (!Mensajes.confirmar(this, "Se van a BORRAR todos los datos actuales y se cargarán los del archivo:\n"
                + archivo.getName() + "\n\n¿Está seguro de continuar?")) {
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            int filas = respaldoDAO.restaurarRespaldo(archivo);
            registrar("Respaldo restaurado: " + archivo.getName() + " (" + filas + " registros)");
            Mensajes.info(this, "Respaldo restaurado correctamente.\n" + filas + " registros cargados.");
        } catch (SQLException ex) {
            Mensajes.error(this, "No se pudo restaurar. Los datos actuales no se modificaron.\n\n" + ex.getMessage());
        } catch (IOException ex) {
            Mensajes.error(this, "No se pudo leer el archivo:\n" + ex.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void registrar(String texto) {
        txtRegistro.append(java.time.LocalTime.now().withNano(0) + "  " + texto + "\n");
    }
}
