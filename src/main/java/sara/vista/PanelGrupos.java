package sara.vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import sara.dao.GradoDAO;
import sara.modelo.Grado;
import sara.util.Mensajes;

/*
 * Clase PanelGrupos
 * Muestra los grupos (grado + seccion) con la cantidad de estudiantes
 * activos del anio lectivo, y permite agregar o eliminar secciones.
 */
public class PanelGrupos extends JPanel {

    private GradoDAO gradoDAO = new GradoDAO();
    private JSpinner spnAnio;
    private DefaultTableModel modelo;
    private JTable tabla;
    private JLabel lblTotal = new JLabel(" ");
    private List<Object[]> grupos = new ArrayList<>(); // {idSeccion, grado, seccion, cantidad}

    public PanelGrupos() {
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // barra superior
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        spnAnio = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1));
        spnAnio.setEditor(new JSpinner.NumberEditor(spnAnio, "#"));
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnAgregar = new JButton("Agregar sección");
        JButton btnEliminar = new JButton("Eliminar sección");
        barra.add(new JLabel("Año lectivo:"));
        barra.add(spnAnio);
        barra.add(btnActualizar);
        barra.add(btnAgregar);
        barra.add(btnEliminar);
        add(barra, BorderLayout.NORTH);

        // tabla
        modelo = Tablas.modeloNoEditable(new String[] { "Grado", "Sección", "Estudiantes activos" });
        tabla = Tablas.crear(modelo);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(lblTotal, BorderLayout.SOUTH);

        btnActualizar.addActionListener(e -> cargar());
        btnAgregar.addActionListener(e -> agregarSeccion());
        btnEliminar.addActionListener(e -> eliminarSeccion());
        spnAnio.addChangeListener(e -> cargar());

        cargar();
    }

    private int getAnio() {
        return (Integer) spnAnio.getValue();
    }

    private void cargar() {
        try {
            grupos = gradoDAO.listarGrupos(getAnio());
            modelo.setRowCount(0);

            int total = 0;
            for (Object[] fila : grupos) {
                modelo.addRow(new Object[] { fila[1], fila[2], fila[3] });
                total += (Integer) fila[3];
            }
            lblTotal.setText("Total de grupos: " + grupos.size() + "   |   Estudiantes activos en " + getAnio() + ": " + total);
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }

    private void agregarSeccion() {
        try {
            JComboBox<Grado> cmbGrado = new JComboBox<>();
            for (Grado g : gradoDAO.listarGrados()) {
                cmbGrado.addItem(g);
            }
            JTextField txtNombre = new JTextField(8);

            JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
            formulario.add(new JLabel("Grado:"));
            formulario.add(cmbGrado);
            formulario.add(new JLabel("Nombre de la sección:"));
            formulario.add(txtNombre);

            int r = JOptionPane.showConfirmDialog(this, formulario, "Agregar sección",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) {
                return;
            }

            String nombre = txtNombre.getText().trim().toUpperCase();
            Grado grado = (Grado) cmbGrado.getSelectedItem();

            if (nombre.isEmpty() || nombre.length() > 10) {
                Mensajes.advertencia(this, "Escriba el nombre de la sección (máximo 10 caracteres). Ejemplo: C");
                return;
            }
            if (gradoDAO.existeSeccion(grado.getId(), nombre)) {
                Mensajes.advertencia(this, "Ya existe la sección " + nombre + " en " + grado.getNombre() + ".");
                return;
            }

            gradoDAO.agregarSeccion(grado.getId(), nombre);
            cargar();
            Mensajes.info(this, "Sección " + nombre + " agregada a " + grado.getNombre() + ".");
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }

    private void eliminarSeccion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.advertencia(this, "Seleccione la sección que desea eliminar.");
            return;
        }

        Object[] grupo = grupos.get(fila);
        if (!Mensajes.confirmar(this, "¿Eliminar la sección " + grupo[2] + " de " + grupo[1] + "?")) {
            return;
        }

        try {
            boolean eliminada = gradoDAO.eliminarSeccion((Integer) grupo[0]);
            if (eliminada) {
                cargar();
            } else {
                Mensajes.advertencia(this, "No se puede eliminar: la sección tiene estudiantes matriculados o actividades.");
            }
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }
}
