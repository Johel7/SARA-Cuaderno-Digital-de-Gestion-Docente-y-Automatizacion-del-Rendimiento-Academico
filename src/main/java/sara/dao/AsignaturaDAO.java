package sara.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sara.modelo.Asignatura;
import sara.util.ConexionBD;

/*
 * Clase AsignaturaDAO
 * Consultas del catalogo de asignaturas.
 */
public class AsignaturaDAO {

    public List<Asignatura> listar() throws SQLException {
        List<Asignatura> lista = new ArrayList<>();
        String sql = "SELECT id_asignatura, nombre, regimen FROM asignatura ORDER BY regimen, nombre";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Asignatura(rs.getInt("id_asignatura"),
                        rs.getString("nombre"), rs.getString("regimen")));
            }
        }
        return lista;
    }

    // Revisa si ya existe otra asignatura con el mismo nombre y regimen
    public boolean existe(String nombre, String regimen, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM asignatura WHERE nombre = ? AND regimen = ? AND id_asignatura <> ?";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, regimen);
            ps.setInt(3, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void insertar(String nombre, String regimen) throws SQLException {
        String sql = "INSERT INTO asignatura (nombre, regimen) VALUES (?, ?)";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, regimen);
            ps.executeUpdate();
        }
    }

    public void actualizar(int id, String nombre, String regimen) throws SQLException {
        String sql = "UPDATE asignatura SET nombre = ?, regimen = ? WHERE id_asignatura = ?";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, regimen);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    // No se puede eliminar una asignatura que ya se uso en actividades o notas
    public boolean eliminar(int id) throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            String[] revisiones = {
                "SELECT COUNT(*) FROM actividad WHERE id_asignatura = ?",
                "SELECT COUNT(*) FROM nota WHERE id_asignatura = ?",
                "SELECT COUNT(*) FROM asistencia WHERE id_asignatura = ?"
            };
            for (String sqlRevision : revisiones) {
                try (PreparedStatement ps = cn.prepareStatement(sqlRevision)) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            return false;
                        }
                    }
                }
            }

            try (PreparedStatement ps = cn.prepareStatement("DELETE FROM asignatura WHERE id_asignatura = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        }
        return true;
    }
}
