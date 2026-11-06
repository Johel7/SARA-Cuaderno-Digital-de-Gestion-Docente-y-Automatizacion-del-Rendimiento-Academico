package sara.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import sara.modelo.Actividad;
import sara.util.ConexionBD;

/*
 * Clase ActividadDAO
 * Actividades evaluativas con su porcentaje (la "configuracion de
 * evaluaciones" de cada docente).
 */
public class ActividadDAO {

    // Lista las actividades de un docente para una asignatura, grupo y corte
    public List<Actividad> listar(int idUsuario, int idAsignatura, int idGrado, int idSeccion,
                                  int idCorte, int anio) throws SQLException {
        List<Actividad> lista = new ArrayList<>();
        String sql = "SELECT id_actividad, id_usuario, id_asignatura, id_grado, id_seccion, id_corte, "
                   + "anio_lectivo, nombre, porcentaje, fecha FROM actividad "
                   + "WHERE id_usuario = ? AND id_asignatura = ? AND id_grado = ? AND id_seccion = ? "
                   + "AND id_corte = ? AND anio_lectivo = ? ORDER BY fecha, id_actividad";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idAsignatura);
            ps.setInt(3, idGrado);
            ps.setInt(4, idSeccion);
            ps.setInt(5, idCorte);
            ps.setInt(6, anio);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Actividad a = new Actividad();
                    a.setId(rs.getInt("id_actividad"));
                    a.setIdUsuario(rs.getInt("id_usuario"));
                    a.setIdAsignatura(rs.getInt("id_asignatura"));
                    a.setIdGrado(rs.getInt("id_grado"));
                    a.setIdSeccion(rs.getInt("id_seccion"));
                    a.setIdCorte(rs.getInt("id_corte"));
                    a.setAnioLectivo(rs.getInt("anio_lectivo"));
                    a.setNombre(rs.getString("nombre"));
                    a.setPorcentaje(rs.getDouble("porcentaje"));
                    Date fecha = rs.getDate("fecha");
                    if (fecha != null) {
                        a.setFecha(fecha.toLocalDate());
                    }
                    lista.add(a);
                }
            }
        }
        return lista;
    }

    // Suma de los porcentajes ya configurados (sin contar la actividad que se edita)
    public double sumarPorcentajes(int idUsuario, int idAsignatura, int idGrado, int idSeccion,
                                   int idCorte, int anio, int idExcluir) throws SQLException {
        String sql = "SELECT COALESCE(SUM(porcentaje), 0) FROM actividad "
                   + "WHERE id_usuario = ? AND id_asignatura = ? AND id_grado = ? AND id_seccion = ? "
                   + "AND id_corte = ? AND anio_lectivo = ? AND id_actividad <> ?";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idAsignatura);
            ps.setInt(3, idGrado);
            ps.setInt(4, idSeccion);
            ps.setInt(5, idCorte);
            ps.setInt(6, anio);
            ps.setInt(7, idExcluir);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }

    // Nota mas alta ya escrita en una actividad (0 si todavia no tiene notas).
    // Sirve para no bajar el maximo de puntos por debajo de una nota ya registrada.
    public double maxPuntaje(int idActividad) throws SQLException {
        String sql = "SELECT COALESCE(MAX(puntaje), 0) FROM calificacion WHERE id_actividad = ?";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idActividad);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }

    public void insertar(Actividad a) throws SQLException {
        String sql = "INSERT INTO actividad (id_usuario, id_asignatura, id_grado, id_seccion, id_corte, "
                   + "anio_lectivo, nombre, porcentaje, fecha) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, a.getIdUsuario());
            ps.setInt(2, a.getIdAsignatura());
            ps.setInt(3, a.getIdGrado());
            ps.setInt(4, a.getIdSeccion());
            ps.setInt(5, a.getIdCorte());
            ps.setInt(6, a.getAnioLectivo());
            ps.setString(7, a.getNombre());
            ps.setDouble(8, a.getPorcentaje());
            if (a.getFecha() == null) {
                ps.setNull(9, Types.DATE);
            } else {
                ps.setDate(9, Date.valueOf(a.getFecha()));
            }
            ps.executeUpdate();
        }
    }

    public void actualizar(Actividad a) throws SQLException {
        String sql = "UPDATE actividad SET nombre = ?, porcentaje = ?, fecha = ? WHERE id_actividad = ?";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, a.getNombre());
            ps.setDouble(2, a.getPorcentaje());
            if (a.getFecha() == null) {
                ps.setNull(3, Types.DATE);
            } else {
                ps.setDate(3, Date.valueOf(a.getFecha()));
            }
            ps.setInt(4, a.getId());
            ps.executeUpdate();
        }
    }

    // Al eliminar una actividad se borran tambien sus calificaciones (ON DELETE CASCADE)
    public void eliminar(int idActividad) throws SQLException {
        String sql = "DELETE FROM actividad WHERE id_actividad = ?";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idActividad);
            ps.executeUpdate();
        }
    }
}
