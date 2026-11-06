package sara.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import sara.modelo.Corte;
import sara.util.CalculoNotas;
import sara.util.ConexionBD;
import sara.util.NivelLogro;

/*
 * Clase NotaDAO
 * Trabaja con la tabla nota (la nota total de cada estudiante por asignatura
 * y corte, que es la suma de puntos de sus actividades) y con las consultas
 * que se arman a partir de ella: ranking, alumnos en riesgo, boletin
 * (cortes + nota final) y rendimiento por grado.
 */
public class NotaDAO {

    // Recalcula las notas de un grupo a partir de las calificaciones (nota del
    // corte = suma de puntos) y las guarda en la tabla nota. Se borran las notas anteriores de ese grupo,
    // asignatura y corte y se vuelven a insertar, todo en una transaccion.
    public void recalcularNotas(int idUsuario, int idGrado, int idSeccion, int anio,
                                int idAsignatura, int idCorte) throws SQLException {
        String sqlPromedios = "SELECT c.id_matricula, SUM(c.puntaje) AS total "
                            + "FROM calificacion c JOIN actividad a ON a.id_actividad = c.id_actividad "
                            + "WHERE a.id_usuario = ? AND a.id_grado = ? AND a.id_seccion = ? "
                            + "AND a.anio_lectivo = ? AND a.id_asignatura = ? AND a.id_corte = ? "
                            + "GROUP BY c.id_matricula";
        String sqlBorrar = "DELETE FROM nota WHERE id_asignatura = ? AND id_corte = ? AND id_matricula IN "
                         + "(SELECT id_matricula FROM matricula WHERE id_grado = ? AND id_seccion = ? "
                         + "AND anio_lectivo = ?)";
        String sqlInsertar = "INSERT INTO nota (id_matricula, id_asignatura, id_corte, calif_cuantitativa, "
                           + "calif_cualitativa) VALUES (?, ?, ?, ?, ?)";

        try (Connection cn = ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                // 1) sumar los puntos de cada estudiante
                List<Object[]> totales = new ArrayList<>();
                try (PreparedStatement ps = cn.prepareStatement(sqlPromedios)) {
                    ps.setInt(1, idUsuario);
                    ps.setInt(2, idGrado);
                    ps.setInt(3, idSeccion);
                    ps.setInt(4, anio);
                    ps.setInt(5, idAsignatura);
                    ps.setInt(6, idCorte);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            double total = CalculoNotas.redondear(rs.getDouble("total"));
                            totales.add(new Object[] { rs.getInt("id_matricula"), total });
                        }
                    }
                }

                // 2) borrar las notas viejas del grupo
                try (PreparedStatement ps = cn.prepareStatement(sqlBorrar)) {
                    ps.setInt(1, idAsignatura);
                    ps.setInt(2, idCorte);
                    ps.setInt(3, idGrado);
                    ps.setInt(4, idSeccion);
                    ps.setInt(5, anio);
                    ps.executeUpdate();
                }

                // 3) insertar las nuevas
                try (PreparedStatement ps = cn.prepareStatement(sqlInsertar)) {
                    for (Object[] fila : totales) {
                        double total = (Double) fila[1];
                        ps.setInt(1, (Integer) fila[0]);
                        ps.setInt(2, idAsignatura);
                        ps.setInt(3, idCorte);
                        ps.setDouble(4, total);
                        ps.setString(5, NivelLogro.calcular(total));
                        ps.executeUpdate();
                    }
                }

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            }
        }
    }

    // Ranking del grupo en una asignatura y corte.
    // Cada fila: {posicion, codigo, estudiante, nota del corte, nivel}
    // Si dos estudiantes tienen la misma nota comparten la posicion.
    public List<Object[]> ranking(int idGrado, int idSeccion, int anio, int idAsignatura, int idCorte)
            throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT e.codigo, e.nombres, e.apellidos, n.calif_cuantitativa, n.calif_cualitativa "
                   + "FROM nota n "
                   + "JOIN matricula m ON m.id_matricula = n.id_matricula "
                   + "JOIN estudiante e ON e.id_estudiante = m.id_estudiante "
                   + "WHERE m.id_grado = ? AND m.id_seccion = ? AND m.anio_lectivo = ? AND m.estado = 'ACTIVA' "
                   + "AND n.id_asignatura = ? AND n.id_corte = ? "
                   + "ORDER BY n.calif_cuantitativa DESC, e.apellidos, e.nombres";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idGrado);
            ps.setInt(2, idSeccion);
            ps.setInt(3, anio);
            ps.setInt(4, idAsignatura);
            ps.setInt(5, idCorte);

            try (ResultSet rs = ps.executeQuery()) {
                int contador = 0;
                int posicion = 0;
                double anterior = -1;

                while (rs.next()) {
                    contador++;
                    double nota = rs.getDouble("calif_cuantitativa");
                    if (nota != anterior) {
                        posicion = contador; // si es igual al anterior se queda la misma posicion
                        anterior = nota;
                    }
                    filas.add(new Object[] {
                        posicion, rs.getString("codigo"),
                        rs.getString("nombres") + " " + rs.getString("apellidos"),
                        nota, rs.getString("calif_cualitativa") });
                }
            }
        }
        return filas;
    }

    // Alumnos en riesgo academico: los que tienen nota de corte menor a 60 (nivel AI)
    // en alguna asignatura del corte indicado.
    // Cada fila: {codigo, estudiante, asignatura, nota del corte, nivel}
    public List<Object[]> alumnosEnRiesgo(int idGrado, int idSeccion, int anio, int idCorte)
            throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT e.codigo, e.nombres, e.apellidos, a.nombre AS asignatura, "
                   + "n.calif_cuantitativa, n.calif_cualitativa "
                   + "FROM nota n "
                   + "JOIN matricula m ON m.id_matricula = n.id_matricula "
                   + "JOIN estudiante e ON e.id_estudiante = m.id_estudiante "
                   + "JOIN asignatura a ON a.id_asignatura = n.id_asignatura "
                   + "WHERE m.id_grado = ? AND m.id_seccion = ? AND m.anio_lectivo = ? AND m.estado = 'ACTIVA' "
                   + "AND n.id_corte = ? AND n.calif_cuantitativa < ? "
                   + "ORDER BY e.apellidos, e.nombres, a.nombre";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idGrado);
            ps.setInt(2, idSeccion);
            ps.setInt(3, anio);
            ps.setInt(4, idCorte);
            ps.setDouble(5, NivelLogro.NOTA_APROBACION);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[] {
                        rs.getString("codigo"),
                        rs.getString("nombres") + " " + rs.getString("apellidos"),
                        rs.getString("asignatura"),
                        rs.getDouble("calif_cuantitativa"), rs.getString("calif_cualitativa") });
                }
            }
        }
        return filas;
    }

    // Boletin del grupo: una fila por estudiante y asignatura con la nota de cada
    // corte, el acumulado, la nota final, su nivel y el resultado (aprobada o reprobada).
    // Cada fila: {codigo, estudiante, asignatura, corte 1, ..., corte N,
    //             acumulado, nota final, nivel, resultado}
    // La nota final solo existe cuando los N cortes tienen nota; mientras tanto
    // dice "En proceso" y el nivel y el resultado quedan vacios.
    public List<Object[]> historial(int idGrado, int idSeccion, int anio, String texto, List<Corte> cortes)
            throws SQLException {
        return boletin(idGrado, idSeccion, anio, texto, 0, cortes);
    }

    // Igual que historial, pero de un solo estudiante (por su matricula)
    public List<Object[]> boletinEstudiante(int idGrado, int idSeccion, int anio, int idMatricula,
                                            List<Corte> cortes) throws SQLException {
        return boletin(idGrado, idSeccion, anio, null, idMatricula, cortes);
    }

    private List<Object[]> boletin(int idGrado, int idSeccion, int anio, String texto, int idMatricula,
                                   List<Corte> cortes) throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        List<Object> parametros = new ArrayList<>();
        int n = cortes.size();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT e.codigo, e.nombres, e.apellidos, a.id_asignatura, a.nombre AS asignatura, ");
        sql.append("n.id_corte, n.calif_cuantitativa ");
        sql.append("FROM nota n ");
        sql.append("JOIN matricula m ON m.id_matricula = n.id_matricula ");
        sql.append("JOIN estudiante e ON e.id_estudiante = m.id_estudiante ");
        sql.append("JOIN asignatura a ON a.id_asignatura = n.id_asignatura ");
        sql.append("WHERE m.id_grado = ? AND m.id_seccion = ? AND m.anio_lectivo = ? AND m.estado = 'ACTIVA' ");
        parametros.add(idGrado);
        parametros.add(idSeccion);
        parametros.add(anio);

        if (idMatricula > 0) {
            sql.append("AND m.id_matricula = ? ");
            parametros.add(idMatricula);
        }
        if (texto != null && !texto.trim().isEmpty()) {
            sql.append("AND (e.codigo LIKE ? OR e.nombres LIKE ? OR e.apellidos LIKE ?) ");
            String patron = "%" + texto.trim() + "%";
            parametros.add(patron);
            parametros.add(patron);
            parametros.add(patron);
        }
        sql.append("ORDER BY e.apellidos, e.nombres, a.nombre, n.id_corte");

        // Se agrupa por estudiante + asignatura (una fila de salida por cada par)
        Map<String, Object[]> porFila = new LinkedHashMap<>();

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String clave = rs.getString("codigo") + "|" + rs.getInt("id_asignatura");
                    Object[] fila = porFila.get(clave);

                    if (fila == null) {
                        fila = new Object[3 + n + 4];
                        fila[0] = rs.getString("codigo");
                        fila[1] = rs.getString("nombres") + " " + rs.getString("apellidos");
                        fila[2] = rs.getString("asignatura");
                        porFila.put(clave, fila);
                    }

                    // se ubica la columna del corte
                    for (int c = 0; c < n; c++) {
                        if (cortes.get(c).getId() == rs.getInt("id_corte")) {
                            fila[3 + c] = rs.getDouble("calif_cuantitativa");
                        }
                    }
                }
            }
        }

        // Acumulado, nota final, nivel y resultado de cada fila
        for (Object[] fila : porFila.values()) {
            Double[] notas = new Double[n];
            for (int c = 0; c < n; c++) {
                notas[c] = (Double) fila[3 + c];
            }
            fila[3 + n] = CalculoNotas.acumulado(notas);

            Double finalAsignatura = CalculoNotas.notaFinal(notas);
            if (finalAsignatura == null) {
                fila[4 + n] = CalculoNotas.EN_PROCESO;
            } else {
                fila[4 + n] = finalAsignatura;
                fila[5 + n] = NivelLogro.calcular(finalAsignatura);
                fila[6 + n] = finalAsignatura >= NivelLogro.NOTA_APROBACION ? "Aprobada" : "Reprobada";
            }
            filas.add(fila);
        }
        return filas;
    }

    // Calcula el rendimiento academico de un grado en un anio (una fila por
    // modalidad) y lo guarda en la tabla rendimiento.
    //   Cada fila: {modalidad, matricula inicial, matricula actual, aprobaron todas,
    //               reprobaron 1 o 2, reprobaron 3 o mas, % de retencion}
    // Una asignatura se considera reprobada si su nota final (promedio de los cortes,
    // cada uno con 25 %) es menor a 60. Solo se cuentan las asignaturas que ya
    // tienen todos los cortes con nota; los estudiantes sin ninguna asignatura
    // completa todavia no aparecen en el resumen.
    public List<Object[]> calcularRendimiento(int idGrado, int anio) throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        int totalCortes = new CorteDAO().listar().size();

        String sqlModalidades = "SELECT DISTINCT modalidad FROM matricula WHERE id_grado = ? AND anio_lectivo = ? "
                              + "ORDER BY modalidad";
        String sqlConteo = "SELECT COUNT(*) FROM matricula WHERE id_grado = ? AND anio_lectivo = ? "
                         + "AND modalidad = ? AND (? = 0 OR estado = 'ACTIVA')";
        String sqlReprobadas = "SELECT m.id_matricula, "
                             + "SUM(CASE WHEN x.nota_final < ? THEN 1 ELSE 0 END) AS reprobadas "
                             + "FROM matricula m "
                             + "JOIN (SELECT id_matricula, id_asignatura, "
                             + "             ROUND(SUM(calif_cuantitativa) / ?, 2) AS nota_final "
                             + "      FROM nota GROUP BY id_matricula, id_asignatura "
                             + "      HAVING COUNT(*) = ?) x "
                             + "  ON x.id_matricula = m.id_matricula "
                             + "WHERE m.id_grado = ? AND m.anio_lectivo = ? AND m.modalidad = ? "
                             + "AND m.estado = 'ACTIVA' GROUP BY m.id_matricula";

        try (Connection cn = ConexionBD.obtenerConexion()) {
            // modalidades que existen en ese grado y anio
            List<String> modalidades = new ArrayList<>();
            try (PreparedStatement ps = cn.prepareStatement(sqlModalidades)) {
                ps.setInt(1, idGrado);
                ps.setInt(2, anio);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        modalidades.add(rs.getString("modalidad"));
                    }
                }
            }

            for (String modalidad : modalidades) {
                int inicial = contarMatriculas(cn, sqlConteo, idGrado, anio, modalidad, 0);
                int actual = contarMatriculas(cn, sqlConteo, idGrado, anio, modalidad, 1);

                int aprobaronTodas = 0;
                int reprobaron1o2 = 0;
                int reprobaron3omas = 0;

                try (PreparedStatement ps = cn.prepareStatement(sqlReprobadas)) {
                    ps.setDouble(1, NivelLogro.NOTA_APROBACION);
                    ps.setInt(2, totalCortes);
                    ps.setInt(3, totalCortes);
                    ps.setInt(4, idGrado);
                    ps.setInt(5, anio);
                    ps.setString(6, modalidad);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int reprobadas = rs.getInt("reprobadas");
                            if (reprobadas == 0) {
                                aprobaronTodas++;
                            } else if (reprobadas <= 2) {
                                reprobaron1o2++;
                            } else {
                                reprobaron3omas++;
                            }
                        }
                    }
                }

                double retencion = (inicial == 0) ? 0 : CalculoNotas.redondear(actual * 100.0 / inicial);

                guardarRendimiento(cn, idGrado, anio, modalidad, inicial, actual,
                        aprobaronTodas, reprobaron1o2, reprobaron3omas, retencion);

                filas.add(new Object[] { modalidad, inicial, actual, aprobaronTodas,
                                         reprobaron1o2, reprobaron3omas, retencion });
            }
        }
        return filas;
    }

    private int contarMatriculas(Connection cn, String sql, int idGrado, int anio, String modalidad,
                                 int soloActivas) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idGrado);
            ps.setInt(2, anio);
            ps.setString(3, modalidad);
            ps.setInt(4, soloActivas);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    // Guarda (o actualiza si ya existia) el resumen en la tabla rendimiento
    private void guardarRendimiento(Connection cn, int idGrado, int anio, String modalidad, int inicial,
                                    int actual, int todas, int r12, int r3, double retencion) throws SQLException {
        String sqlActualizar = "UPDATE rendimiento SET matricula_inicial = ?, matricula_actual = ?, "
                             + "aprobados_finales = ?, aprobados_1_2 = ?, aprobados_3_mas = ?, pct_retencion = ? "
                             + "WHERE id_grado = ? AND anio_lectivo = ? AND modalidad = ?";
        String sqlInsertar = "INSERT INTO rendimiento (matricula_inicial, matricula_actual, aprobados_finales, "
                           + "aprobados_1_2, aprobados_3_mas, pct_retencion, id_grado, anio_lectivo, modalidad) "
                           + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        int actualizadas;
        try (PreparedStatement ps = cn.prepareStatement(sqlActualizar)) {
            asignarRendimiento(ps, inicial, actual, todas, r12, r3, retencion, idGrado, anio, modalidad);
            actualizadas = ps.executeUpdate();
        }

        if (actualizadas == 0) {
            try (PreparedStatement ps = cn.prepareStatement(sqlInsertar)) {
                asignarRendimiento(ps, inicial, actual, todas, r12, r3, retencion, idGrado, anio, modalidad);
                ps.executeUpdate();
            }
        }
    }

    private void asignarRendimiento(PreparedStatement ps, int inicial, int actual, int todas, int r12, int r3,
                                    double retencion, int idGrado, int anio, String modalidad) throws SQLException {
        ps.setInt(1, inicial);
        ps.setInt(2, actual);
        ps.setInt(3, todas);
        ps.setInt(4, r12);
        ps.setInt(5, r3);
        ps.setDouble(6, retencion);
        ps.setInt(7, idGrado);
        ps.setInt(8, anio);
        ps.setString(9, modalidad);
    }
}
