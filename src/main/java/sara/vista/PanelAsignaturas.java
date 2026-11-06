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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import sara.dao.AsignaturaDAO;
import sara.modelo.Asignatura;
import sara.util.Mensajes;

/*
 * Clase PanelAsignaturas
 * Catalogo de asignaturas (lo administra el administrador).
 */
public class PanelAsignaturas extends JPanel {

    private AsignaturaDAO asignaturaDAO = new AsignaturaDAO();
    private DefaultTableModel modelo;
    private JTable tabla;
    private List<Asignatura> lista = new ArrayList<>();

    public PanelAsignaturas() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnNueva = new JButton("Nueva asignatura");
        JButton btnEditar = new JButton("Modificar");
        JButton btnEliminar = new JButton("Eliminar");
        barra.add(btnNueva);
        barra.add(btnEditar);
        barra.add(btnEliminar);
        add(barra, BorderLayout.NORTH);

        modelo = Tablas.modeloNoEditable(new String[] { "Asignatura", "Régimen" });
        tabla = Tablas.crear(modelo);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnNueva.addActionListener(e -> guardar(null));
        btnEditar.addActionListener(e -> editarSeleccionada());
        btnEliminar.addActionListener(e -> eliminar());

        cargar();
    }

    private void cargar() {
        try {
            lista = asignaturaDAO.listar();
            modelo.setRowCount(0);
            for (Asignatura a : lista) {
                modelo.addRow(new Object[] { a.getNombre(), a.getRegimen() });
            }
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }

    private Asignatura seleccionada() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.advertencia(this, "Seleccione una asignatura de la tabla.");
            return null;
        }
        return lista.get(fila);
    }

    private void editarSeleccionada() {
        Asignatura a = seleccionada();
        if (a != null) {
            guardar(a);
        }
    }

    // Formulario para crear (existente = null) o modificar una asignatura
    private void guardar(Asignatura existente) {
        JTextField txtNombre = new JTextField(20);
        JComboBox<String> cmbRegimen = new JComboBox<>(new String[] { "Primaria", "Secundaria" });

        if (existente != null) {
            txtNombre.setText(existente.getNombre());
            cmbRegimen.setSelectedItem(existente.getRegimen());
        }

        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
        formulario.add(new JLabel("Nombre:"));
        formulario.add(txtNombre);
        formulario.add(new JLabel("Régimen:"));
        formulario.add(cmbRegimen);

        while (true) {
            int r = JOptionPane.showConfirmDialog(this, formulario,
                    existente == null ? "Nueva asignatura" : "Modificar asignatura",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) {
                return;
            }

            String nombre = txtNombre.getText().trim();
            String regimen = (String) cmbRegimen.getSelectedItem();

            if (nombre.isEmpty() || nombre.length() > 60) {
                Mensajes.advertencia(this, "Escriba el nombre de la asignatura (máximo 60 caracteres).");
                continue;
            }

            try {
                int idExcluir = (existente == null) ? 0 : existente.getId();
                if (asignaturaDAO.existe(nombre, regimen, idExcluir)) {
                    Mensajes.advertencia(this, "Ya existe esa asignatura en el régimen " + regimen + ".");
                    continue;
                }

                if (existente == null) {
                    asignaturaDAO.insertar(nombre, regimen);
                } else {
                    asignaturaDAO.actualizar(existente.getId(), nombre, regimen);
                }
                cargar();
            } catch (SQLException ex) {
                Mensajes.errorBD(this, ex);
            }
            return;
        }
    }

    private void eliminar() {
        Asignatura a = seleccionada();
        if (a == null || !Mensajes.confirmar(this, "¿Eliminar la asignatura " + a + "?")) {
            return;
        }

        try {
            if (asignaturaDAO.eliminar(a.getId())) {
                cargar();
            } else {
                Mensajes.advertencia(this, "No se puede eliminar: la asignatura ya tiene actividades o notas registradas.");
            }
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }
}
