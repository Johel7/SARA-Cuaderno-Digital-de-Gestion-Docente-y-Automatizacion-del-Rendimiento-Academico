package sara.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import sara.dao.ActividadDAO;
import sara.dao.NotaDAO;
import sara.modelo.Actividad;
import sara.modelo.Asignatura;
import sara.modelo.Corte;
import sara.modelo.Grado;
import sara.modelo.Seccion;
import sara.modelo.Usuario;
import sara.util.Mensajes;

/*
 * Clase PanelEvaluaciones
 * Configuracion de las actividades evaluativas (quiz, tarea, examen...)
 * de un corte, con su porcentaje y fecha. Los porcentajes de un mismo
 * corte no pueden pasar de 100%.
 */
public class PanelEvaluaciones extends JPanel {

    private Usuario usuario;
    private ActividadDAO actividadDAO = new ActividadDAO();
    private NotaDAO notaDAO = new NotaDAO();

    private PanelFiltros filtros = new PanelFiltros(false, true, true);
    private DefaultTableModel modelo;
    private JTable tabla;
    private JLabel lblSuma = new JLabel("Elija el grupo, la asignatura y el corte y presione \"Cargar actividades\".");
    private List<Actividad> actividades = new ArrayList<>();

    // contexto con el que se cargo la tabla (para no depender de los combos si el usuario los cambia)
    private Grado grado;
    private Seccion seccion;
    private Asignatura asignatura;
    private Corte corte;
    private int anio;
    private boolean cargado = false;

    public PanelEvaluaciones(Usuario usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(filtros, BorderLayout.NORTH);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnCargar = new JButton("Cargar actividades");
        JButton btnAgregar = new JButton("Agregar actividad");
        JButton btnEditar = new JButton("Modificar");
        JButton btnEliminar = new JButton("Eliminar");
        botones.add(btnCargar);
        botones.add(btnAgregar);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        arriba.add(botones, BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);

        modelo = Tablas.modeloNoEditable(new String[] { "Actividad", "Porcentaje (= puntos máx.)", "Fecha" });
        tabla = Tablas.crear(modelo);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(lblSuma, BorderLayout.SOUTH);

        btnCargar.addActionListener(e -> cargar());
        btnAgregar.addActionListener(e -> agregar());
        btnEditar.addActionListener(e -> modificar());
        btnEliminar.addActionListener(e -> eliminar());
    }

    // Lee los filtros y muestra las actividades de ese grupo/asignatura/corte
    private void cargar() {
        Grado g = filtros.getGrado();
        Seccion s = filtros.getSeccion();
        Asignatura a = filtros.getAsignatura();
        Corte c = filtros.getCorte();

        if (g == null || s == null || a == null || c == null) {
            Mensajes.advertencia(this, "Elija el grado, la sección, la asignatura y el corte.");
            return;
        }

        grado = g;
        seccion = s;
        asignatura = a;
        corte = c;
        anio = filtros.getAnio();
        cargado = true;
        refrescar();
    }

    private void refrescar() {
        try {
            actividades = actividadDAO.listar(usuario.getIdUsuario(), asignatura.getId(),
                    grado.getId(), seccion.getId(), corte.getId(), anio);

            modelo.setRowCount(0);
            double suma = 0;
            for (Actividad a : actividades) {
                modelo.addRow(new Object[] { a.getNombre(), a.getPorcentaje(),
                        a.getFecha() == null ? "" : a.getFecha().toString() });
                suma += a.getPorcentaje();
            }
            if (actividades.isEmpty()) {
                lblSuma.setForeground(new Color(190, 100, 0));
                lblSuma.setText(grado.getNombre() + " " + seccion.getNombre() + " - " + asignatura.getNombre()
                        + " - " + corte.getNombre() + ":  todavía no hay actividades. Use \"Agregar actividad\".");
            } else {
                mostrarSuma(suma);
            }
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }

    // Muestra cuanto suman los porcentajes y cuanto falta para llegar a 100
    private void mostrarSuma(double suma) {
        String grupo = grado.getNombre() + " " + seccion.getNombre() + " - " + asignatura.getNombre()
                + " - " + corte.getNombre() + ":  ";

        if (Math.abs(suma - 100) < 0.001) {
            lblSuma.setForeground(new Color(0, 120, 0));
            lblSuma.setText(grupo + "los porcentajes suman 100%. Configuración completa.");
        } else {
            lblSuma.setForeground(new Color(190, 100, 0));
            lblSuma.setText(grupo + "los porcentajes suman " + suma + "%. Faltan " + (100 - suma) + "% para completar el 100%.");
        }
    }

    private boolean hayContexto() {
        if (!cargado) {
            Mensajes.advertencia(this, "Primero cargue las actividades de un grupo, asignatura y corte.");
        }
        return cargado;
    }

    private void agregar() {
        if (!hayContexto()) {
            return;
        }
        Actividad nueva = pedirDatos(null);
        if (nueva == null) {
            return;
        }

        nueva.setIdUsuario(usuario.getIdUsuario());
        nueva.setIdAsignatura(asignatura.getId());
        nueva.setIdGrado(grado.getId());
        nueva.setIdSeccion(seccion.getId());
        nueva.setIdCorte(corte.getId());
        nueva.setAnioLectivo(anio);

        try {
            actividadDAO.insertar(nueva);
            refrescar();
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }

    private void modificar() {
        if (!hayContexto()) {
            return;
        }
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.advertencia(this, "Seleccione la actividad que desea modificar.");
            return;
        }

        Actividad actividad = actividades.get(fila);
        Actividad cambios = pedirDatos(actividad);
        if (cambios == null) {
            return;
        }

        try {
            actividadDAO.actualizar(cambios);
            recalcularNotasDelCorte();
            refrescar();
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }

    private void eliminar() {
        if (!hayContexto()) {
            return;
        }
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.advertencia(this, "Seleccione la actividad que desea eliminar.");
            return;
        }

        Actividad actividad = actividades.get(fila);
        if (!Mensajes.confirmar(this, "¿Eliminar la actividad \"" + actividad.getNombre() + "\"?\n"
                + "También se borrarán las notas que ya se hayan registrado en ella.")) {
            return;
        }

        try {
            actividadDAO.eliminar(actividad.getId());
            recalcularNotasDelCorte();
            refrescar();
            Mensajes.info(this, "Actividad eliminada. Los totales del corte se actualizaron.");
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }

    // Si se cambia o se borra una actividad, los totales del corte cambian: se recalculan solos
    private void recalcularNotasDelCorte() throws SQLException {
        notaDAO.recalcularNotas(usuario.getIdUsuario(), grado.getId(), seccion.getId(), anio,
                asignatura.getId(), corte.getId());
    }

    // Muestra el formulario de la actividad (nuevo si existente es null).
    // Devuelve la actividad con los datos escritos, o null si se cancelo.
    private Actividad pedirDatos(Actividad existente) {
        JTextField txtNombre = new JTextField(20);
        JTextField txtPorcentaje = new JTextField(8);
        JTextField txtFecha = new JTextField(10);

        if (existente != null) {
            txtNombre.setText(existente.getNombre());
            txtPorcentaje.setText(String.valueOf(existente.getPorcentaje()));
            if (existente.getFecha() != null) {
                txtFecha.setText(existente.getFecha().toString());
            }
        }

        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
        formulario.add(new JLabel("Nombre de la actividad:"));
        formulario.add(txtNombre);
        formulario.add(new JLabel("Porcentaje del corte (= puntos máximos):"));
        formulario.add(txtPorcentaje);
        formulario.add(new JLabel("Fecha (aaaa-mm-dd, opcional):"));
        formulario.add(txtFecha);

        while (true) {
            int r = JOptionPane.showConfirmDialog(this, formulario,
                    existente == null ? "Nueva actividad" : "Modificar actividad",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) {
                return null;
            }

            String nombre = txtNombre.getText().trim();
            if (nombre.isEmpty() || nombre.length() > 80) {
                Mensajes.advertencia(this, "Escriba el nombre de la actividad (máximo 80 caracteres).");
                continue;
            }

            double porcentaje;
            try {
                porcentaje = Double.parseDouble(txtPorcentaje.getText().trim().replace(',', '.'));
            } catch (NumberFormatException ex) {
                Mensajes.advertencia(this, "El porcentaje debe ser un número. Ejemplo: 20 (la actividad vale 20 puntos)");
                continue;
            }
            if (porcentaje <= 0 || porcentaje > 100) {
                Mensajes.advertencia(this, "El porcentaje debe ser mayor que 0 y no pasar de 100.");
                continue;
            }

            LocalDate fecha = null;
            String textoFecha = txtFecha.getText().trim();
            if (!textoFecha.isEmpty()) {
                try {
                    fecha = LocalDate.parse(textoFecha);
                } catch (DateTimeParseException ex) {
                    Mensajes.advertencia(this, "La fecha debe tener el formato aaaa-mm-dd. Ejemplo: 2026-03-15");
                    continue;
                }
            }

            // no se puede bajar el maximo por debajo de una nota ya escrita
            try {
                if (existente != null) {
                    double notaMasAlta = actividadDAO.maxPuntaje(existente.getId());
                    if (porcentaje < notaMasAlta) {
                        Mensajes.advertencia(this, "Esta actividad ya tiene una nota de " + notaMasAlta
                                + " puntos, así que no puede valer menos que eso.\n"
                                + "Corrija primero esa nota en el registro de notas.");
                        continue;
                    }
                }
            } catch (SQLException ex) {
                Mensajes.errorBD(this, ex);
                return null;
            }

            // los porcentajes del corte no pueden pasar de 100
            try {
                double otras = actividadDAO.sumarPorcentajes(usuario.getIdUsuario(), asignatura.getId(),
                        grado.getId(), seccion.getId(), corte.getId(), anio,
                        existente == null ? 0 : existente.getId());
                if (otras + porcentaje > 100.0001) {
                    Mensajes.advertencia(this, "Con este porcentaje el corte sumaría " + (otras + porcentaje)
                            + "%. Solo quedan " + (100 - otras) + "% disponibles.");
                    continue;
                }
            } catch (SQLException ex) {
                Mensajes.errorBD(this, ex);
                return null;
            }

            Actividad resultado = new Actividad();
            if (existente != null) {
                resultado.setId(existente.getId());
            }
            resultado.setNombre(nombre);
            resultado.setPorcentaje(porcentaje);
            resultado.setFecha(fecha);
            return resultado;
        }
    }
}
