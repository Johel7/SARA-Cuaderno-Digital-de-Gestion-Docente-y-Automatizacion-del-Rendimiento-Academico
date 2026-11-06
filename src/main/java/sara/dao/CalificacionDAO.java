package sara.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sara.modelo.Actividad;
import sara.modelo.Estudiante;
import sara.util.ConexionBD;

/*
 * Clase CalificacionDAO
 * Lee y guarda los puntajes de cada estudiante en cada actividad.
 * Los puntajes se manejan como una matriz: filas = estudiantes,
 * columnas = actividades. Un null significa "todavia sin nota".
 */
public class CalificacionDAO {

    // Carga los puntajes que ya existen para esos estudiantes y actividades
    public Double[][] cargarPuntajes(List<Estudiante> alumnos, List<Actividad> actividades)
            throws SQLException {
        Double[][] puntajes = new Double[alumnos.size()][actividades.size()];

        if (alumnos.isEmpty() || actividades.isEmpty()) {
            return puntajes;
        }

        // Para ubicar rapido la fila y la columna de cada dato
        Map<Integer, Integer> filaPorMatricula = new HashMap<>();
        for (int i = 0; i < alumnos.size(); i++) {
            filaPorMatricula.put(alumnos.get(i).getIdMatricula(), i);
        }
        Map<Integer, Integer> columnaPorActividad = new HashMap<>();
        StringBuilder interrogantes = new StringBuilder();
        for (int j = 0; j < actividades.size(); j++) {
            columnaPorActividad.put(actividades.get(j).getId(), j);
            interrogantes.append(j == 0 ? "?" : ",?");
        }

        String sql = "SELECT id_matricula, id_actividad, puntaje FROM calificacion "
                   + "WHERE id_actividad IN (" + interrogantes + ")";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            for (int j = 0; j < actividades.size(); j++) {
                ps.setInt(j + 1, actividades.get(j).getId());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Integer fila = filaPorMatricula.get(rs.getInt("id_matricula"));
                    Integer columna = columnaPorActividad.get(rs.getInt("id_actividad"));
                    if (fila != null && columna != null) {
                        puntajes[fila][columna] = rs.getDouble("puntaje");
                    }
                }
            }
        }
        return puntajes;
    }

    // Guarda toda la matriz de puntajes en una sola transaccion.
    // Si el puntaje es null se borra la calificacion; si no, se actualiza
    // o se inserta (segun si ya existia).
    public void guardarPuntajes(List<Estudiante> alumnos, List<Actividad> actividades, Double[][] puntajes)
            throws SQLException {
        String sqlBorrar = "DELETE FROM calificacion WHERE id_matricula = ? AND id_actividad = ?";
        String sqlActualizar = "UPDATE calificacion SET puntaje = ? WHERE id_matricula = ? AND id_actividad = ?";
        String sqlInsertar = "INSERT INTO calificacion (id_matricula, id_actividad, puntaje) VALUES (?, ?, ?)";

        try (Connection cn = ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);
            try (PreparedStatement psBorrar = cn.prepareStatement(sqlBorrar);
                 PreparedStatement psActualizar = cn.prepareStatement(sqlActualizar);
                 PreparedStatement psInsertar = cn.prepareStatement(sqlInsertar)) {

                for (int i = 0; i < alumnos.size(); i++) {
                    for (int j = 0; j < actividades.size(); j++) {
                        int idMatricula = alumnos.get(i).getIdMatricula();
                        int idActividad = actividades.get(j).getId();
                        Double puntaje = puntajes[i][j];

                        if (puntaje == null) {
                            psBorrar.setInt(1, idMatricula);
                            psBorrar.setInt(2, idActividad);
                            psBorrar.executeUpdate();
                        } else {
                            psActualizar.setDouble(1, puntaje);
                            psActualizar.setInt(2, idMatricula);
                            psActualizar.setInt(3, idActividad);
                            int filasActualizadas = psActualizar.executeUpdate();

                            if (filasActualizadas == 0) {
                                psInsertar.setInt(1, idMatricula);
                                psInsertar.setInt(2, idActividad);
                                psInsertar.setDouble(3, puntaje);
                                psInsertar.executeUpdate();
                            }
                        }
                    }
                }
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            }
        }
    }
}
