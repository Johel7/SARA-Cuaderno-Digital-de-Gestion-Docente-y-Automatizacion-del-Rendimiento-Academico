package sara.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sara.modelo.Grado;
import sara.modelo.Seccion;
import sara.util.ConexionBD;

/*
 * Clase GradoDAO
 * Consultas de grados y secciones (los "grupos" del docente).
 */
public class GradoDAO {

    public List<Grado> listarGrados() throws SQLException {
        List<Grado> lista = new ArrayList<>();
        String sql = "SELECT id_grado, nombre FROM grado ORDER BY id_grado";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Grado(rs.getInt("id_grado"), rs.getString("nombre")));
            }
        }
        return lista;
    }

    public List<Seccion> listarSecciones(int idGrado) throws SQLException {
        List<Seccion> lista = new ArrayList<>();
        String sql = "SELECT id_seccion, id_grado, nombre FROM seccion WHERE id_grado = ? ORDER BY nombre";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idGrado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Seccion(rs.getInt("id_seccion"), rs.getInt("id_grado"), rs.getString("nombre")));
                }
            }
        }
        return lista;
    }

    // Lista todos los grupos (grado + seccion) con la cantidad de estudiantes
    // activos matriculados en el anio indicado.
    // Cada fila: {idSeccion, nombre del grado, nombre de la seccion, cantidad}
    public List<Object[]> listarGrupos(int anio) throws SQLException {
        List<Object[]> filas = new ArrayList<>();
        String sql = "SELECT s.id_seccion, g.nombre AS grado, s.nombre AS seccion, COUNT(m.id_matricula) AS cantidad "
                   + "FROM seccion s "
                   + "JOIN grado g ON g.id_grado = s.id_grado "
                   + "LEFT JOIN matricula m ON m.id_seccion = s.id_seccion "
                   + "     AND m.anio_lectivo = ? AND m.estado = 'ACTIVA' "
                   + "GROUP BY s.id_seccion, g.id_grado, g.nombre, s.nombre "
                   + "ORDER BY g.id_grado, s.nombre";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, anio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    filas.add(new Object[] {
                        rs.getInt("id_seccion"), rs.getString("grado"),
                        rs.getString("seccion"), rs.getInt("cantidad") });
                }
            }
        }
        return filas;
    }

    public boolean existeSeccion(int idGrado, String nombre) throws SQLException {
        String sql = "SELECT COUNT(*) FROM seccion WHERE id_grado = ? AND nombre = ?";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idGrado);
            ps.setString(2, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void agregarSeccion(int idGrado, String nombre) throws SQLException {
        String sql = "INSERT INTO seccion (id_grado, nombre) VALUES (?, ?)";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idGrado);
            ps.setString(2, nombre);
            ps.executeUpdate();
        }
    }

    // Solo se puede eliminar una seccion que no tenga matriculas ni actividades.
    // Devuelve false si no se pudo eliminar por eso.
    public boolean eliminarSeccion(int idSeccion) throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            String[] revisiones = {
                "SELECT COUNT(*) FROM matricula WHERE id_seccion = ?",
                "SELECT COUNT(*) FROM actividad WHERE id_seccion = ?"
            };
            for (String sqlRevision : revisiones) {
                try (PreparedStatement ps = cn.prepareStatement(sqlRevision)) {
                    ps.setInt(1, idSeccion);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            return false;
                        }
                    }
                }
            }

            try (PreparedStatement ps = cn.prepareStatement("DELETE FROM seccion WHERE id_seccion = ?")) {
                ps.setInt(1, idSeccion);
                ps.executeUpdate();
            }
        }
        return true;
    }
}
