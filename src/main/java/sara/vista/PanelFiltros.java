package sara.vista;

import java.awt.FlowLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import sara.dao.AsignaturaDAO;
import sara.dao.CorteDAO;
import sara.dao.GradoDAO;
import sara.modelo.Asignatura;
import sara.modelo.Corte;
import sara.modelo.Grado;
import sara.modelo.Seccion;
import sara.util.Mensajes;

/*
 * Clase PanelFiltros
 * Barra con los filtros que se repiten en varias ventanas: anio lectivo,
 * grado, seccion y, si se pide, asignatura y corte. Cuando se cambia el
 * grado, las secciones se actualizan solas.
 */
public class PanelFiltros extends JPanel {

    private JSpinner spnAnio;
    private JComboBox<Grado> cmbGrado = new JComboBox<>();
    private JComboBox<Seccion> cmbSeccion = new JComboBox<>();
    private JComboBox<Asignatura> cmbAsignatura;
    private JComboBox<Corte> cmbCorte;

    private boolean permitirTodos; // si es true, grado y seccion tienen la opcion "Todos"
    private List<Asignatura> todasAsignaturas = new ArrayList<>();
    private GradoDAO gradoDAO = new GradoDAO();

    public PanelFiltros(boolean permitirTodos, boolean conAsignatura, boolean conCorte) {
        this.permitirTodos = permitirTodos;
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));

        cmbGrado.setPrototypeDisplayValue(new Grado(0, "Undécimo grado"));
        cmbSeccion.setPrototypeDisplayValue(new Seccion(0, 0, "Todas"));

        spnAnio = new JSpinner(new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1));
        spnAnio.setEditor(new JSpinner.NumberEditor(spnAnio, "#")); // sin separador de miles

        add(new JLabel("Año lectivo:"));
        add(spnAnio);
        add(new JLabel("Grado:"));
        add(cmbGrado);
        add(new JLabel("Sección:"));
        add(cmbSeccion);

        if (conAsignatura) {
            cmbAsignatura = new JComboBox<>();
            add(new JLabel("Asignatura:"));
            add(cmbAsignatura);
        }
        if (conCorte) {
            cmbCorte = new JComboBox<>();
            add(new JLabel("Corte:"));
            add(cmbCorte);
        }

        cargarCatalogos();
        cmbGrado.addActionListener(e -> {
            recargarSecciones();
            recargarAsignaturas();
        });
    }

    private void cargarCatalogos() {
        try {
            if (permitirTodos) {
                cmbGrado.addItem(new Grado(0, "Todos"));
            }
            for (Grado g : gradoDAO.listarGrados()) {
                cmbGrado.addItem(g);
            }
            recargarSecciones();

            if (cmbAsignatura != null) {
                todasAsignaturas = new AsignaturaDAO().listar();
                recargarAsignaturas();
            }
            if (cmbCorte != null) {
                for (Corte c : new CorteDAO().listar()) {
                    cmbCorte.addItem(c);
                }
            }
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }

    // Vuelve a llenar las secciones segun el grado elegido
    public void recargarSecciones() {
        cmbSeccion.removeAllItems();
        Grado grado = (Grado) cmbGrado.getSelectedItem();
        if (grado == null) {
            return;
        }

        try {
            if (permitirTodos) {
                cmbSeccion.addItem(new Seccion(0, 0, "Todas"));
            }
            if (grado.getId() > 0) {
                for (Seccion s : gradoDAO.listarSecciones(grado.getId())) {
                    cmbSeccion.addItem(s);
                }
            }
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }


    // Muestra solo las asignaturas que corresponden al grado elegido:
    // del primero al sexto grado son de Primaria; del septimo en adelante, de Secundaria.
    // Si el grado es "Todos" se muestran todas.
    public void recargarAsignaturas() {
        if (cmbAsignatura == null) {
            return;
        }

        cmbAsignatura.removeAllItems();
        Grado grado = (Grado) cmbGrado.getSelectedItem();

        for (Asignatura a : todasAsignaturas) {
            boolean mostrar = true;
            if (grado != null && grado.getId() > 0) {
                String regimen = (grado.getId() <= 6) ? "Primaria" : "Secundaria";
                mostrar = regimen.equals(a.getRegimen());
            }
            if (mostrar) {
                cmbAsignatura.addItem(a);
            }
        }
    }

    public int getAnio() {
        return (Integer) spnAnio.getValue();
    }

    public Grado getGrado() {
        return (Grado) cmbGrado.getSelectedItem();
    }

    public Seccion getSeccion() {
        return (Seccion) cmbSeccion.getSelectedItem();
    }

    public Asignatura getAsignatura() {
        return cmbAsignatura == null ? null : (Asignatura) cmbAsignatura.getSelectedItem();
    }

    public Corte getCorte() {
        return cmbCorte == null ? null : (Corte) cmbCorte.getSelectedItem();
    }
}
