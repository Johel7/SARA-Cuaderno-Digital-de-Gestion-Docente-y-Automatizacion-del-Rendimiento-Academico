package sara.reportes;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sara.dao.ActividadDAO;
import sara.dao.CalificacionDAO;
import sara.dao.CorteDAO;
import sara.dao.EstudianteDAO;
import sara.dao.NotaDAO;
import sara.modelo.Actividad;
import sara.modelo.Asignatura;
import sara.modelo.Corte;
import sara.modelo.Estudiante;
import sara.modelo.Grado;
import sara.modelo.ResultadoTabla;
import sara.modelo.Seccion;
import sara.modelo.Usuario;
import sara.util.CalculoNotas;
import sara.util.NivelLogro;

/*
 * Clase ReporteService
 * Arma cada consulta/reporte del sistema como un ResultadoTabla. Asi la
 * misma tabla se puede ver en pantalla y exportar a Excel o PDF.
 */
public class ReporteService {

    private ActividadDAO actividadDAO = new ActividadDAO();
    private EstudianteDAO estudianteDAO = new EstudianteDAO();
    private CalificacionDAO calificacionDAO = new CalificacionDAO();
    private NotaDAO notaDAO = new NotaDAO();
    private CorteDAO corteDAO = new CorteDAO();

    // Matriz de calificaciones del corte: cada actividad, el total (suma de puntos),
    // lo que aporta a la nota final y el nivel
    public ResultadoTabla calificacionesCorte(Usuario docente, Grado grado, Seccion seccion, int anio,
                                              Asignatura asignatura, Corte corte) throws SQLException {
        List<Actividad> actividades = actividadDAO.listar(docente.getIdUsuario(), asignatura.getId(),
                grado.getId(), seccion.getId(), corte.getId(), anio);
        List<Estudiante> alumnos = estudianteDAO.listar(anio, grado.getId(), seccion.getId(), "", false);
        Double[][] puntajes = calificacionDAO.cargarPuntajes(alumnos, actividades);

        // encabezados
        String[] encabezados = new String[actividades.size() + 5];
        encabezados[0] = "Código";
        encabezados[1] = "Estudiante";
        for (int j = 0; j < actividades.size(); j++) {
            encabezados[2 + j] = actividades.get(j).getNombre() + " (" + puntos(actividades.get(j).getPorcentaje()) + " pts)";
        }
        encabezados[2 + actividades.size()] = "Total (de 100)";
        encabezados[3 + actividades.size()] = "Aporte a la final (de " + (int) CalculoNotas.PESO_CORTE + ")";
        encabezados[4 + actividades.size()] = "Nivel";

        // filas
        List<Object[]> filas = new ArrayList<>();
        for (int i = 0; i < alumnos.size(); i++) {
            Object[] fila = new Object[encabezados.length];
            fila[0] = alumnos.get(i).getCodigo();
            fila[1] = alumnos.get(i).getNombreCompleto();
            for (int j = 0; j < actividades.size(); j++) {
                fila[2 + j] = puntajes[i][j];
            }
            Double total = CalculoNotas.sumaPuntos(puntajes[i]);
            if (total != null) {
                fila[2 + actividades.size()] = total;
                fila[3 + actividades.size()] = CalculoNotas.aporte(total);
                fila[4 + actividades.size()] = NivelLogro.calcular(total);
            }
            filas.add(fila);
        }

        return new ResultadoTabla("Calificaciones del " + corte.getNombre(),
                subtitulo(docente, grado, seccion, anio, asignatura, corte), encabezados, filas);
    }

    public ResultadoTabla ranking(Usuario docente, Grado grado, Seccion seccion, int anio,
                                  Asignatura asignatura, Corte corte) throws SQLException {
        List<Object[]> filas = notaDAO.ranking(grado.getId(), seccion.getId(), anio,
                asignatura.getId(), corte.getId());
        String[] encabezados = { "Posición", "Código", "Estudiante", "Nota del corte", "Nivel" };

        return new ResultadoTabla("Ranking del grupo - " + corte.getNombre(),
                subtitulo(docente, grado, seccion, anio, asignatura, corte), encabezados, filas);
    }

    public ResultadoTabla alumnosEnRiesgo(Usuario docente, Grado grado, Seccion seccion, int anio,
                                          Corte corte) throws SQLException {
        List<Object[]> filas = notaDAO.alumnosEnRiesgo(grado.getId(), seccion.getId(), anio, corte.getId());
        String[] encabezados = { "Código", "Estudiante", "Asignatura", "Nota del corte", "Nivel" };

        return new ResultadoTabla("Alumnos en riesgo académico - " + corte.getNombre(),
                subtitulo(docente, grado, seccion, anio, null, corte)
                        + "  |  Nota del corte menor a " + (int) NivelLogro.NOTA_APROBACION,
                encabezados, filas);
    }

    // Boletin de la seccion: cada estudiante con sus asignaturas, la nota de los
    // cortes, la nota final y su letra (AA, AS, AF o AI)
    public ResultadoTabla historial(Usuario docente, Grado grado, Seccion seccion, int anio,
                                    String texto) throws SQLException {
        List<Corte> cortes = corteDAO.listar();
        List<Object[]> filas = notaDAO.historial(grado.getId(), seccion.getId(), anio, texto, cortes);

        String[] encabezados = new String[cortes.size() + 7];
        encabezados[0] = "Código";
        encabezados[1] = "Estudiante";
        encabezados[2] = "Asignatura";
        completarEncabezados(encabezados, 3, cortes);

        return new ResultadoTabla("Boletín de la sección",
                subtitulo(docente, grado, seccion, anio, null, null) + "  |  " + notaFinalTexto(cortes),
                encabezados, filas);
    }

    // Boletin de un estudiante: sus asignaturas con la nota de cada corte,
    // la nota final y su letra
    public ResultadoTabla boletinEstudiante(Usuario docente, Grado grado, Seccion seccion, int anio,
                                            Estudiante estudiante) throws SQLException {
        List<Corte> cortes = corteDAO.listar();
        List<Object[]> completas = notaDAO.boletinEstudiante(grado.getId(), seccion.getId(), anio,
                estudiante.getIdMatricula(), cortes);

        // como todas las filas son del mismo estudiante, se quitan codigo y nombre de la tabla
        List<Object[]> filas = new ArrayList<>();
        for (Object[] f : completas) {
            filas.add(java.util.Arrays.copyOfRange(f, 2, f.length));
        }

        String[] encabezados = new String[cortes.size() + 5];
        encabezados[0] = "Asignatura";
        completarEncabezados(encabezados, 1, cortes);

        return new ResultadoTabla("Boletín del estudiante",
                estudiante.getNombreCompleto() + " (" + estudiante.getCodigo() + ")  |  "
                        + subtitulo(docente, grado, seccion, anio, null, null) + "  |  " + notaFinalTexto(cortes),
                encabezados, filas);
    }

    // Columnas comunes de los boletines: un corte por columna, acumulado, final, nivel, resultado
    private void completarEncabezados(String[] encabezados, int desde, List<Corte> cortes) {
        for (int c = 0; c < cortes.size(); c++) {
            encabezados[desde + c] = cortes.get(c).getNombre();
        }
        encabezados[desde + cortes.size()] = "Acumulado (de 100)";
        encabezados[desde + cortes.size() + 1] = "Nota final";
        encabezados[desde + cortes.size() + 2] = "Nivel";
        encabezados[desde + cortes.size() + 3] = "Resultado";
    }

    private String notaFinalTexto(List<Corte> cortes) {
        return "Nota final = promedio de los " + cortes.size() + " cortes (" + (int) CalculoNotas.PESO_CORTE
                + "% cada uno)";
    }

    public ResultadoTabla rendimiento(Grado grado, int anio) throws SQLException {
        List<Object[]> filas = notaDAO.calcularRendimiento(grado.getId(), anio);
        String[] encabezados = { "Modalidad", "Matrícula inicial", "Matrícula actual",
                "Aprobaron todas", "Reprobaron 1 o 2", "Reprobaron 3 o más", "% de retención" };

        return new ResultadoTabla("Rendimiento académico por grado",
                grado.getNombre() + "  |  Año lectivo: " + anio, encabezados, filas);
    }

    // 10.0 se escribe 10; 12.5 se queda 12.5
    private String puntos(double valor) {
        return valor == Math.floor(valor) ? String.valueOf((int) valor) : String.valueOf(valor);
    }

    // Texto con los datos del grupo que va debajo del titulo
    private String subtitulo(Usuario docente, Grado grado, Seccion seccion, int anio,
                             Asignatura asignatura, Corte corte) {
        String texto = grado.getNombre() + " " + seccion.getNombre() + "  |  Año lectivo: " + anio;
        if (asignatura != null) {
            texto += "  |  " + asignatura.getNombre();
        }
        if (corte != null) {
            texto += "  |  " + corte.getNombre();
        }
        return texto + "  |  Docente: " + docente.getNombreCompleto();
    }
}
