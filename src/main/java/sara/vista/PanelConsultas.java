package sara.vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
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

import sara.dao.EstudianteDAO;
import sara.modelo.Asignatura;
import sara.modelo.Corte;
import sara.modelo.Estudiante;
import sara.modelo.Grado;
import sara.modelo.ResultadoTabla;
import sara.modelo.Seccion;
import sara.modelo.Usuario;
import sara.reportes.ExportadorExcel;
import sara.reportes.ExportadorPDF;
import sara.reportes.ReporteService;
import sara.util.Archivos;
import sara.util.Mensajes;

/*
 * Clase PanelConsultas
 * Calificaciones del corte, ranking, alumnos en riesgo, boletines (cortes y
 * nota final con su letra) y rendimiento por grado. Cada consulta se ve en
 * pantalla y se puede exportar a Excel o PDF.
 */
public class PanelConsultas extends JPanel {

    private static final String[] TIPOS = {
        "Calificaciones del corte",
        "Ranking del grupo",
        "Alumnos en riesgo académico",
        "Boletín de la sección (cortes y nota final)",
        "Boletín de un estudiante",
        "Rendimiento del grado"
    };

    private Usuario usuario;
    private ReporteService reportes = new ReporteService();

    private PanelFiltros filtros = new PanelFiltros(false, true, true);
    private JComboBox<String> cmbTipo = new JComboBox<>(TIPOS);
    private JTextField txtEstudiante = new JTextField(12);
    private JButton btnExcel = new JButton("Exportar a Excel");
    private JButton btnPdf = new JButton("Exportar a PDF");
    private JTable tabla;
    private JLabel lblTitulo = new JLabel("Elija una consulta y presione \"Consultar\".  (Para escribir o corregir notas use el menú Evaluación > Registro de notas)");

    private ResultadoTabla ultimoResultado = null;

    public PanelConsultas(Usuario usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout(8, 8));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel arriba = new JPanel(new BorderLayout());
        arriba.add(filtros, BorderLayout.NORTH);

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        JButton btnConsultar = new JButton("Consultar");
        acciones.add(new JLabel("Consulta:"));
        acciones.add(cmbTipo);
        acciones.add(new JLabel("Estudiante (código o nombre):"));
        acciones.add(txtEstudiante);
        acciones.add(btnConsultar);
        acciones.add(btnExcel);
        acciones.add(btnPdf);
        arriba.add(acciones, BorderLayout.SOUTH);
        add(arriba, BorderLayout.NORTH);

        tabla = Tablas.crear(Tablas.modeloNoEditable(new String[0]));
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        add(lblTitulo, BorderLayout.SOUTH);

        // los botones de exportar se activan cuando ya hay una consulta en pantalla
        btnExcel.setEnabled(false);
        btnPdf.setEnabled(false);

        btnConsultar.addActionListener(e -> consultar());
        btnExcel.addActionListener(e -> exportarExcel());
        btnPdf.addActionListener(e -> exportarPdf());
    }

    private void consultar() {
        Grado grado = filtros.getGrado();
        Seccion seccion = filtros.getSeccion();
        Asignatura asignatura = filtros.getAsignatura();
        Corte corte = filtros.getCorte();
        int anio = filtros.getAnio();

        if (grado == null || seccion == null) {
            Mensajes.advertencia(this, "Elija el grado y la sección.");
            return;
        }

        int tipo = cmbTipo.getSelectedIndex();
        // calificaciones y ranking necesitan asignatura y corte; el riesgo necesita corte
        if ((tipo == 0 || tipo == 1) && (asignatura == null || corte == null)) {
            Mensajes.advertencia(this, "Esta consulta necesita la asignatura y el corte.");
            return;
        }
        if (tipo == 2 && corte == null) {
            Mensajes.advertencia(this, "Esta consulta necesita el corte.");
            return;
        }

        // el boletin de un estudiante necesita saber de cual
        Estudiante elegido = null;
        if (tipo == 4) {
            elegido = elegirEstudiante(grado, seccion, anio);
            if (elegido == null) {
                return;
            }
        }

        try {
            ResultadoTabla resultado;
            switch (tipo) {
                case 0:
                    resultado = reportes.calificacionesCorte(usuario, grado, seccion, anio, asignatura, corte);
                    break;
                case 1:
                    resultado = reportes.ranking(usuario, grado, seccion, anio, asignatura, corte);
                    break;
                case 2:
                    resultado = reportes.alumnosEnRiesgo(usuario, grado, seccion, anio, corte);
                    break;
                case 3:
                    resultado = reportes.historial(usuario, grado, seccion, anio, txtEstudiante.getText());
                    break;
                case 4:
                    resultado = reportes.boletinEstudiante(usuario, grado, seccion, anio, elegido);
                    break;
                default:
                    resultado = reportes.rendimiento(grado, anio);
                    break;
            }
            mostrar(resultado);
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
    }

    // Busca al estudiante escrito en el cuadro de texto dentro del grupo elegido.
    // Si hay varios con ese texto, deja escoger uno. Devuelve null si se cancela o no hay.
    private Estudiante elegirEstudiante(Grado grado, Seccion seccion, int anio) {
        String texto = txtEstudiante.getText().trim();
        if (texto.isEmpty()) {
            Mensajes.advertencia(this, "Escriba el código o el nombre del estudiante en el cuadro "
                    + "\"Estudiante\" para ver su boletín.");
            return null;
        }

        try {
            List<Estudiante> encontrados = new EstudianteDAO().listar(anio, grado.getId(), seccion.getId(), texto, false);
            if (encontrados.isEmpty()) {
                Mensajes.advertencia(this, "No se encontró ningún estudiante activo con ese texto en "
                        + grado.getNombre() + " " + seccion.getNombre() + ".");
                return null;
            }
            if (encontrados.size() == 1) {
                return encontrados.get(0);
            }

            String[] opciones = new String[encontrados.size()];
            for (int i = 0; i < opciones.length; i++) {
                opciones[i] = encontrados.get(i).getCodigo() + " - " + encontrados.get(i).getNombreCompleto();
            }
            Object escogido = JOptionPane.showInputDialog(this, "Hay varios estudiantes con ese texto. Elija uno:",
                    "Boletín de un estudiante", JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
            if (escogido == null) {
                return null;
            }
            for (int i = 0; i < opciones.length; i++) {
                if (opciones[i].equals(escogido)) {
                    return encontrados.get(i);
                }
            }
        } catch (SQLException ex) {
            Mensajes.errorBD(this, ex);
        }
        return null;
    }

    // Pone el resultado en la tabla de la pantalla
    private void mostrar(ResultadoTabla resultado) {
        DefaultTableModel modelo = Tablas.modeloNoEditable(resultado.getEncabezados());
        for (Object[] fila : resultado.getFilas()) {
            modelo.addRow(fila);
        }
        tabla.setModel(modelo);
        // la columna con el nombre del estudiante se hace mas ancha
        for (int c = 0; c < modelo.getColumnCount(); c++) {
            if ("Estudiante".equals(modelo.getColumnName(c))) {
                tabla.getColumnModel().getColumn(c).setPreferredWidth(220);
            }
        }

        ultimoResultado = resultado;
        btnExcel.setEnabled(true);
        btnPdf.setEnabled(true);

        lblTitulo.setText(resultado.getTitulo() + "  |  " + resultado.getSubtitulo()
                + "  |  " + (resultado.getFilas().size() == 1 ? "1 fila" : resultado.getFilas().size() + " filas"));
        if (resultado.getFilas().isEmpty()) {
            Mensajes.info(this, "La consulta no devolvió resultados. Revise que las notas estén guardadas.");
        }
    }

    private void exportarExcel() {
        File archivo = Archivos.elegirDondeGuardar(this, ultimoResultado.getTitulo(), "Libro de Excel (*.xlsx)", "xlsx");
        if (archivo == null) {
            return;
        }
        try {
            ExportadorExcel.exportar(archivo, ultimoResultado);
            Mensajes.info(this, "Reporte guardado en:\n" + archivo.getAbsolutePath());
        } catch (IOException ex) {
            Mensajes.error(this, "No se pudo guardar el archivo de Excel:\n" + ex.getMessage());
        }
    }

    private void exportarPdf() {
        File archivo = Archivos.elegirDondeGuardar(this, ultimoResultado.getTitulo(), "Documento PDF (*.pdf)", "pdf");
        if (archivo == null) {
            return;
        }
        try {
            ExportadorPDF.exportar(archivo, ultimoResultado);
            Mensajes.info(this, "Reporte guardado en:\n" + archivo.getAbsolutePath());
        } catch (IOException ex) {
            Mensajes.error(this, "No se pudo guardar el archivo PDF:\n" + ex.getMessage());
        }
    }
}
