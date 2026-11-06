package sara.vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import sara.dao.UsuarioDAO;
import sara.modelo.Usuario;
import sara.util.Mensajes;

/*
 * Clase PanelCuentas
 * El administrador crea y modifica cuentas, restablece claves y
 * activa o desactiva usuarios (las cuentas no se borran para no perder
 * el historial de actividades).
 */
public class PanelCuentas extends JPanel {

    private static final int CLAVE_MINIMA = 6;

    private Usuario usuarioActual;
    private UsuarioDAO usuarioDAO = new UsuarioDAO();
    private DefaultTableModel modelo;
    private JTable tabla;
    private List<Usuario> lista = new ArrayList<>();

    public PanelCuentas(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnNuevo = new JButton("Nueva cuenta");
        JButton btnEditar = new JButton("Modificar");
        JButton btnClave = new JButton("Restablecer clave");
        JButton btnEstado = new JButton("Activar / Desactivar");
        barra.add(btnNuevo);
        barra.add(btnEditar);
        barra.add(btnClave);
        barra.add(btnEstado);
        add(barra, BorderLayout.NORTH);

        modelo = Tablas.modeloNoEditable(new String[] { "Usuario", "Nombres", "Apellidos", "Rol", "Estado" });
        tabla = Tablas.crear(modelo);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnNuevo.addActionListener(e -> nuevaCuenta());
        btnEditar.addActionListener(e -> modificarCuenta());
        btnClave.addActionListener(e -> restablecerClave());
        btnEstado.addActionListener(e -> cambiarEstado());

        cargar();
    }

    private void cargar() {
        try {
            lista = usuarioDAO.listar();
            modelo.setRowCount(0);
            for (Usuario u : lista) {
                modelo.addRow(new Object[] { u.getUsuario(), u.getNombres(), u.getApellidos(),
                        u.esAdministrador() ? "Administrador" : "Docente", u.isActivo() ? "Activa" : "Desactivada" });
            }
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }

    private Usuario seleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.advertencia(this, "Seleccione una cuenta de la tabla.");
            return null;
        }
        return lista.get(fila);
    }

    private void nuevaCuenta() {
        JTextField txtUsuario = new JTextField(15);
        JTextField txtNombres = new JTextField(15);
        JTextField txtApellidos = new JTextField(15);
        JComboBox<String> cmbRol = new JComboBox<>(new String[] { "Docente", "Administrador" });
        JPasswordField txtClave = new JPasswordField(15);
        JPasswordField txtConfirmar = new JPasswordField(15);

        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
        formulario.add(new JLabel("Usuario:"));
        formulario.add(txtUsuario);
        formulario.add(new JLabel("Nombres:"));
        formulario.add(txtNombres);
        formulario.add(new JLabel("Apellidos:"));
        formulario.add(txtApellidos);
        formulario.add(new JLabel("Rol:"));
        formulario.add(cmbRol);
        formulario.add(new JLabel("Clave (mínimo " + CLAVE_MINIMA + " caracteres):"));
        formulario.add(txtClave);
        formulario.add(new JLabel("Confirmar clave:"));
        formulario.add(txtConfirmar);

        while (true) {
            int r = JOptionPane.showConfirmDialog(this, formulario, "Nueva cuenta",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) {
                return;
            }

            String nombreUsuario = txtUsuario.getText().trim();
            String clave = new String(txtClave.getPassword());

            String error = validarDatos(nombreUsuario, txtNombres.getText(), txtApellidos.getText(), 0);
            if (error == null && clave.length() < CLAVE_MINIMA) {
                error = "La clave debe tener al menos " + CLAVE_MINIMA + " caracteres.";
            }
            if (error == null && !clave.equals(new String(txtConfirmar.getPassword()))) {
                error = "La clave y su confirmación no coinciden.";
            }
            if (error != null) {
                Mensajes.advertencia(this, error);
                continue;
            }

            String rol = cmbRol.getSelectedIndex() == 0 ? Usuario.ROL_DOCENTE : Usuario.ROL_ADMINISTRADOR;
            Usuario nuevo = new Usuario(0, txtNombres.getText().trim(), txtApellidos.getText().trim(), nombreUsuario, rol);

            try {
                usuarioDAO.insertar(nuevo, clave);
                cargar();
                Mensajes.info(this, "Cuenta creada correctamente.");
            } catch (SQLException ex) {
                Mensajes.errorBD(this, ex);
            }
            return;
        }
    }

    private void modificarCuenta() {
        Usuario u = seleccionado();
        if (u == null) {
            return;
        }

        JTextField txtUsuario = new JTextField(u.getUsuario(), 15);
        JTextField txtNombres = new JTextField(u.getNombres(), 15);
        JTextField txtApellidos = new JTextField(u.getApellidos(), 15);
        JComboBox<String> cmbRol = new JComboBox<>(new String[] { "Docente", "Administrador" });
        cmbRol.setSelectedIndex(u.esAdministrador() ? 1 : 0);

        // para no quedarse sin acceso, nadie puede cambiarse el rol a si mismo
        boolean esLaMismaCuenta = (u.getIdUsuario() == usuarioActual.getIdUsuario());
        cmbRol.setEnabled(!esLaMismaCuenta);

        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
        formulario.add(new JLabel("Usuario:"));
        formulario.add(txtUsuario);
        formulario.add(new JLabel("Nombres:"));
        formulario.add(txtNombres);
        formulario.add(new JLabel("Apellidos:"));
        formulario.add(txtApellidos);
        formulario.add(new JLabel("Rol:"));
        formulario.add(cmbRol);

        while (true) {
            int r = JOptionPane.showConfirmDialog(this, formulario, "Modificar cuenta",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) {
                return;
            }

            String error = validarDatos(txtUsuario.getText().trim(), txtNombres.getText(),
                    txtApellidos.getText(), u.getIdUsuario());
            if (error != null) {
                Mensajes.advertencia(this, error);
                continue;
            }

            String rol = cmbRol.getSelectedIndex() == 0 ? Usuario.ROL_DOCENTE : Usuario.ROL_ADMINISTRADOR;
            Usuario cambios = new Usuario(u.getIdUsuario(), txtNombres.getText().trim(),
                    txtApellidos.getText().trim(), txtUsuario.getText().trim(), rol);

            try {
                usuarioDAO.actualizar(cambios);
                cargar();
            } catch (SQLException ex) {
                Mensajes.errorBD(this, ex);
            }
            return;
        }
    }

    private void restablecerClave() {
        Usuario u = seleccionado();
        if (u == null) {
            return;
        }

        JPasswordField txtClave = new JPasswordField(15);
        JPasswordField txtConfirmar = new JPasswordField(15);
        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
        formulario.add(new JLabel("Nueva clave:"));
        formulario.add(txtClave);
        formulario.add(new JLabel("Confirmar clave:"));
        formulario.add(txtConfirmar);

        while (true) {
            int r = JOptionPane.showConfirmDialog(this, formulario, "Restablecer clave de " + u.getUsuario(),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) {
                return;
            }

            String clave = new String(txtClave.getPassword());
            if (clave.length() < CLAVE_MINIMA) {
                Mensajes.advertencia(this, "La clave debe tener al menos " + CLAVE_MINIMA + " caracteres.");
                continue;
            }
            if (!clave.equals(new String(txtConfirmar.getPassword()))) {
                Mensajes.advertencia(this, "La clave y su confirmación no coinciden.");
                continue;
            }

            try {
                usuarioDAO.cambiarClave(u.getIdUsuario(), clave);
                Mensajes.info(this, "La clave de " + u.getUsuario() + " fue cambiada.");
            } catch (SQLException ex) {
                Mensajes.errorBD(this, ex);
            }
            return;
        }
    }

    private void cambiarEstado() {
        Usuario u = seleccionado();
        if (u == null) {
            return;
        }
        if (u.getIdUsuario() == usuarioActual.getIdUsuario()) {
            Mensajes.advertencia(this, "No puede desactivar su propia cuenta.");
            return;
        }

        try {
            usuarioDAO.cambiarActivo(u.getIdUsuario(), !u.isActivo());
            cargar();
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }

    // Valida los datos comunes de una cuenta. Devuelve el error o null si esta bien.
    private String validarDatos(String nombreUsuario, String nombres, String apellidos, int idExcluir) {
        if (!nombreUsuario.matches("[A-Za-z0-9._-]{3,30}")) {
            return "El usuario debe tener entre 3 y 30 caracteres (letras, números, punto, guion o guion bajo).";
        }
        if (nombres.trim().isEmpty() || apellidos.trim().isEmpty()) {
            return "Los nombres y los apellidos son obligatorios.";
        }
        try {
            if (usuarioDAO.existeUsuario(nombreUsuario, idExcluir)) {
                return "Ya existe una cuenta con el usuario " + nombreUsuario + ".";
            }
        } catch (SQLException ex) {
            return "No se pudo comprobar el usuario: " + ex.getMessage();
        }
        return null;
    }
}
