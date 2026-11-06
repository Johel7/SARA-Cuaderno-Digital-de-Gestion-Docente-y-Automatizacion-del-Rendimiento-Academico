package sara.vista;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import sara.dao.UsuarioDAO;
import sara.modelo.Usuario;

/*
 * Clase LoginFrame
 * Ventana de inicio de sesion de SARA. Valida que los campos no esten
 * vacios, consulta al UsuarioDAO y, si los datos son correctos, abre
 * el menu principal.
 */
public class LoginFrame extends JFrame {

    private JTextField txtUsuario;
    private JPasswordField txtClave;
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    public LoginFrame() {
        setTitle("SARA - Inicio de sesión");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        construirInterfaz();
        pack();
        setResizable(false);
        setLocationRelativeTo(null); // centrar en pantalla
    }

    // Arma los componentes de la ventana
    private void construirInterfaz() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 30, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = new JLabel("SARA", JLabel.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(28f));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(titulo, gbc);

        JLabel subtitulo = new JLabel("Cuaderno Digital de Gestión Docente", JLabel.CENTER);
        gbc.gridy = 1;
        panel.add(subtitulo, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Usuario:"), gbc);
        txtUsuario = new JTextField(15);
        gbc.gridx = 1;
        panel.add(txtUsuario, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(new JLabel("Clave:"), gbc);
        txtClave = new JPasswordField(15);
        gbc.gridx = 1;
        panel.add(txtClave, gbc);

        JButton btnIngresar = new JButton("Ingresar");
        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        panel.add(btnIngresar, gbc);

        btnIngresar.addActionListener(e -> iniciarSesion());
        txtClave.addActionListener(e -> iniciarSesion()); // Enter en la clave tambien ingresa
        getRootPane().setDefaultButton(btnIngresar);

        add(panel);
    }

    // Valida los datos y abre el menu principal si son correctos
    private void iniciarSesion() {
        String nombreUsuario = txtUsuario.getText().trim();
        String clave = new String(txtClave.getPassword());

        if (nombreUsuario.isEmpty() || clave.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese su usuario y su clave.",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Usuario usuario = usuarioDAO.autenticar(nombreUsuario, clave);

            if (usuario == null) {
                JOptionPane.showMessageDialog(this, "Usuario o clave incorrectos.",
                        "Acceso denegado", JOptionPane.ERROR_MESSAGE);
                txtClave.setText("");
            } else {
                new MenuPrincipalFrame(usuario).setVisible(true);
                dispose(); // cierra la ventana de login
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo conectar con la base de datos.\n"
                    + "Verifique que MySQL esté encendido y los datos de ConexionBD.\n\n"
                    + ex.getMessage(),
                    "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }
}
