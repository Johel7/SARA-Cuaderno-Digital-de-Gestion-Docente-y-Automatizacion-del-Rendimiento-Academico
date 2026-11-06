package sara.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;

import sara.dao.ActividadDAO;
import sara.dao.CalificacionDAO;
import sara.dao.EstudianteDAO;
import sara.dao.NotaDAO;
import sara.modelo.Actividad;
import sara.modelo.Asignatura;
import sara.modelo.Corte;
import sara.modelo.Estudiante;
import sara.modelo.Grado;
import sara.modelo.Seccion;
import sara.modelo.Usuario;
import sara.util.CalculoNotas;
import sara.util.Mensajes;
import sara.util.NivelLogro;

/*
 * Clase PanelNotas
 * Registro rapido de calificaciones en una tabla: una fila por estudiante
 * y una columna por actividad. El porcentaje de cada actividad es su maximo
 * de puntos. El Total (suma de puntos, de 0 a 100), lo que aporta a la nota
 * final (25 % por corte) y el nivel de logro se calculan solos.
 */
public class PanelNotas extends JPanel {

    private Usuario usuario;
    private ActividadDAO actividadDAO = new ActividadDAO();
    private EstudianteDAO estudianteDAO = new EstudianteDAO();
    private CalificacionDAO calificacionDAO = new CalificacionDAO();
    private NotaDAO notaDAO = new NotaDAO();

    private PanelFiltros filtros = new PanelFiltros(false, true, true);
    private JTable tabla;
    private ModeloNotas modelo;
    private JLabel lblInfo = new JLabel("Elija el grupo, la asignatura y el corte y presione \"Cargar\".");

    private List<Estudiante> alumnos = new ArrayList<>();
    private List<Actividad> actividades = new ArrayList<>();

    // contexto con el que se cargo la tabla
    private Grado grado;
    private Seccion seccion;
    private Asignatura asignatura;
    private Corte corte;
    private int anio;

    public PanelNotas(Usuario usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(filtros, BorderLayout.NORTH);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnCargar = new JButton("Cargar");
        JButton btnGrupo = new JButton("Nota a todo el grupo");
        JButton btnGuardar = new JButton("Guardar notas");
        botones.add(btnCargar);
        botones.add(btnGrupo);
        botones.add(btnGuardar);
        botones.add(new JLabel("   Escriba los puntos de cada actividad (0 hasta su máximo) y presione Enter. Vacío = todavía sin nota."));
        arriba.add(botones, BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);

        tabla = Tablas.crear(new ModeloNotas(new ArrayList<>(), new ArrayList<>(), new Double[0][0]));
        // si el usuario esta escribiendo una nota y hace clic en otro lado, la nota se guarda en la tabla
        tabla.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(lblInfo, BorderLayout.SOUTH);

        btnCargar.addActionListener(e -> cargar());
        btnGrupo.addActionListener(e -> notaATodoElGrupo());
        btnGuardar.addActionListener(e -> guardar());
    }

    private void cargar() {
        Grado g = filtros.getGrado();
        Seccion s = filtros.getSeccion();
        Asignatura a = filtros.getAsignatura();
        Corte c = filtros.getCorte();

        if (g == null || s == null || a == null || c == null) {
            Mensajes.advertencia(this, "Elija el grado, la sección, la asignatura y el corte.");
            return;
        }
        if (modelo != null && modelo.isModificado()
                && !Mensajes.confirmar(this, "Hay notas sin guardar. ¿Desea descartarlas y cargar de nuevo?")) {
            return;
        }

        try {
            int nuevoAnio = filtros.getAnio();
            List<Actividad> listaActividades = actividadDAO.listar(usuario.getIdUsuario(), a.getId(),
                    g.getId(), s.getId(), c.getId(), nuevoAnio);

            if (listaActividades.isEmpty()) {
                Mensajes.advertencia(this, "Todavía no hay actividades configuradas para este corte.\n"
                        + "Créelas primero en \"Configurar actividades\".");
                return;
            }

            List<Estudiante> listaAlumnos = estudianteDAO.listar(nuevoAnio, g.getId(), s.getId(), "", false);
            if (listaAlumnos.isEmpty()) {
                Mensajes.advertencia(this, "Este grupo no tiene estudiantes activos en " + nuevoAnio + ".");
                return;
            }

            Double[][] puntajes = calificacionDAO.cargarPuntajes(listaAlumnos, listaActividades);

            grado = g;
            seccion = s;
            asignatura = a;
            corte = c;
            anio = nuevoAnio;
            alumnos = listaAlumnos;
            actividades = listaActividades;

            modelo = new ModeloNotas(alumnos, actividades, puntajes);
            tabla.setModel(modelo);
            tabla.getColumnModel().getColumn(0).setPreferredWidth(90);
            tabla.getColumnModel().getColumn(1).setPreferredWidth(220);

            mostrarInfo();
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }


    // Pone la misma nota a todos los estudiantes del grupo en una actividad
    // (por ejemplo: participacion, o una tarea que todos entregaron).
    // Las notas se pueden corregir despues una por una en la tabla.
    private void notaATodoElGrupo() {
        if (modelo == null || alumnos.isEmpty()) {
            Mensajes.advertencia(this, "Primero cargue un grupo para registrar las notas.");
            return;
        }
        if (tabla.isEditing()) {
            tabla.getCellEditor().stopCellEditing();
        }

        JComboBox<String> cmbActividad = new JComboBox<>();
        for (Actividad a : actividades) {
            cmbActividad.addItem(a.getNombre() + " (máx. " + formatear(a.getPorcentaje()) + " pts)");
        }
        JTextField txtNota = new JTextField(6);
        JCheckBox chkSoloVacias = new JCheckBox("Solo a los que todavía no tienen nota", true);

        JPanel formulario = new JPanel(new GridLayout(0, 2, 6, 6));
        formulario.add(new JLabel("Actividad:"));
        formulario.add(cmbActividad);
        formulario.add(new JLabel("Puntos para todo el grupo:"));
        formulario.add(txtNota);
        formulario.add(new JLabel(" "));
        formulario.add(chkSoloVacias);

        while (true) {
            int r = JOptionPane.showConfirmDialog(this, formulario, "Nota a todo el grupo",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION) {
                return;
            }

            double nota;
            try {
                nota = Double.parseDouble(txtNota.getText().trim().replace(',', '.'));
            } catch (NumberFormatException ex) {
                Mensajes.advertencia(this, "Escriba un número válido. Ejemplo: 8 u 8.5");
                continue;
            }
            double maximo = actividades.get(cmbActividad.getSelectedIndex()).getPorcentaje();
            if (nota < 0 || nota > maximo) {
                Mensajes.advertencia(this, "Los puntos de esta actividad deben estar entre 0 y " + formatear(maximo) + ".");
                continue;
            }

            int cambiados = modelo.aplicarAColumna(cmbActividad.getSelectedIndex(),
                    CalculoNotas.redondear(nota), chkSoloVacias.isSelected());
            Mensajes.info(this, "Se pusieron " + formatear(nota) + " puntos a " + cambiados + " estudiante(s).\n"
                    + "Presione \"Guardar notas\" para grabarla en la base de datos.");
            return;
        }
    }

    private double sumaPorcentajes() {
        double suma = 0;
        for (Actividad a : actividades) {
            suma += a.getPorcentaje();
        }
        return suma;
    }

    // 10.0 se escribe 10; 12.5 se queda 12.5
    private String formatear(double valor) {
        return valor == Math.floor(valor) ? String.valueOf((int) valor) : String.valueOf(valor);
    }

    private void mostrarInfo() {
        double suma = sumaPorcentajes();
        String texto = grado.getNombre() + " " + seccion.getNombre() + " - " + asignatura.getNombre()
                + " - " + corte.getNombre() + "  |  " + alumnos.size() + " estudiantes, "
                + actividades.size() + " actividades  |  Total = suma de puntos (máximo 100); cada corte aporta hasta "
                + (int) CalculoNotas.PESO_CORTE + " a la nota final";

        if (Math.abs(suma - 100) < 0.001) {
            lblInfo.setForeground(Color.DARK_GRAY);
        } else {
            lblInfo.setForeground(new Color(190, 100, 0));
            texto += "  |  Atención: los puntos máximos de las actividades suman " + formatear(suma) + " (deberían sumar 100)";
        }
        lblInfo.setText(texto);
    }

    private void guardar() {
        if (modelo == null || alumnos.isEmpty()) {
            Mensajes.advertencia(this, "Primero cargue un grupo para registrar las notas.");
            return;
        }

        // si se esta editando una celda, se termina la edicion para no perder ese dato
        if (tabla.isEditing()) {
            tabla.getCellEditor().stopCellEditing();
        }

        if (Math.abs(sumaPorcentajes() - 100) > 0.001
                && !Mensajes.confirmar(this, "Los puntos máximos de las actividades suman " + formatear(sumaPorcentajes())
                        + " y no 100.\nEl total se calculará igual con lo que hay. ¿Guardar de todas formas?")) {
            return;
        }

        try {
            calificacionDAO.guardarPuntajes(alumnos, actividades, modelo.getPuntajes());
            notaDAO.recalcularNotas(usuario.getIdUsuario(), grado.getId(), seccion.getId(), anio,
                    asignatura.getId(), corte.getId());
            modelo.marcarGuardado();
            Mensajes.info(this, "Notas guardadas y totales actualizados.");
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }

    // ---------------------------------------------------------------
    // Modelo de la tabla de notas
    // Columnas: Codigo | Estudiante | una por actividad | Total | Aporte | Nivel
    // ---------------------------------------------------------------
    private class ModeloNotas extends AbstractTableModel {

        private List<Estudiante> filas;
        private List<Actividad> columnasActividad;
        private Double[][] puntajes;
        private double[] maximos;
        private Double[] totales;
        private boolean modificado = false;

        ModeloNotas(List<Estudiante> filas, List<Actividad> columnasActividad, Double[][] puntajes) {
            this.filas = filas;
            this.columnasActividad = columnasActividad;
            this.puntajes = puntajes;

            maximos = new double[columnasActividad.size()];
            for (int j = 0; j < maximos.length; j++) {
                maximos[j] = columnasActividad.get(j).getPorcentaje();
            }

            totales = new Double[filas.size()];
            for (int i = 0; i < filas.size(); i++) {
                recalcular(i);
            }
        }

        private void recalcular(int fila) {
            totales[fila] = CalculoNotas.sumaPuntos(puntajes[fila]);
        }

        Double[][] getPuntajes() {
            return puntajes;
        }

        boolean isModificado() {
            return modificado;
        }

        // Pone la misma nota a todos en una columna de actividad.
        // Devuelve a cuantos estudiantes se les puso.
        int aplicarAColumna(int actividad, double nota, boolean soloVacias) {
            int cambiados = 0;
            for (int i = 0; i < filas.size(); i++) {
                if (!soloVacias || puntajes[i][actividad] == null) {
                    puntajes[i][actividad] = nota;
                    recalcular(i);
                    cambiados++;
                }
            }
            if (cambiados > 0) {
                modificado = true;
                fireTableDataChanged();
            }
            return cambiados;
        }

        void marcarGuardado() {
            modificado = false;
        }

        @Override
        public int getRowCount() {
            return filas.size();
        }

        @Override
        public int getColumnCount() {
            return columnasActividad.size() + 5;
        }

        @Override
        public String getColumnName(int columna) {
            int n = columnasActividad.size();
            if (columna == 0) {
                return "Código";
            } else if (columna == 1) {
                return "Estudiante";
            } else if (columna < 2 + n) {
                Actividad a = columnasActividad.get(columna - 2);
                return a.getNombre() + " (" + formatear(a.getPorcentaje()) + " pts)";
            } else if (columna == 2 + n) {
                return "Total (de 100)";
            } else if (columna == 3 + n) {
                return "Aporte (de " + (int) CalculoNotas.PESO_CORTE + ")";
            }
            return "Nivel";
        }

        @Override
        public Object getValueAt(int fila, int columna) {
            int n = columnasActividad.size();
            if (columna == 0) {
                return filas.get(fila).getCodigo();
            } else if (columna == 1) {
                return filas.get(fila).getNombreCompleto();
            } else if (columna < 2 + n) {
                return puntajes[fila][columna - 2];
            } else if (columna == 2 + n) {
                return totales[fila];
            } else if (columna == 3 + n) {
                return CalculoNotas.aporte(totales[fila]);
            }
            return totales[fila] == null ? "" : NivelLogro.calcular(totales[fila]);
        }

        // Solo se pueden editar las columnas de las actividades
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return columna >= 2 && columna < 2 + columnasActividad.size();
        }

        // Valida los puntos escritos (de 0 hasta el maximo de la actividad) y recalcula el total de esa fila
        @Override
        public void setValueAt(Object valor, int fila, int columna) {
            int j = columna - 2;
            String texto = (valor == null) ? "" : valor.toString().trim().replace(',', '.');

            if (texto.isEmpty()) {
                puntajes[fila][j] = null; // se borra la nota
            } else {
                double nota;
                try {
                    nota = Double.parseDouble(texto);
                } catch (NumberFormatException ex) {
                    Mensajes.advertencia(PanelNotas.this, "Escriba un número válido. Ejemplo: 8 u 8.5");
                    return;
                }
                if (nota < 0 || nota > maximos[j]) {
                    Mensajes.advertencia(PanelNotas.this, "Esta actividad vale como máximo "
                            + formatear(maximos[j]) + " puntos. Escriba un valor entre 0 y " + formatear(maximos[j]) + ".");
                    return;
                }
                puntajes[fila][j] = CalculoNotas.redondear(nota);
            }

            modificado = true;
            recalcular(fila);
            fireTableRowsUpdated(fila, fila);
        }
    }
}
