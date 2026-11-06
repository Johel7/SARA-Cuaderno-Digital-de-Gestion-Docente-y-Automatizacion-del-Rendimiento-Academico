package sara.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sara.modelo.Usuario;
import sara.util.ConexionBD;
import sara.util.SeguridadUtil;

/*
 * Clase UsuarioDAO
 * DAO = Data Access Object: la unica clase que hace consultas SQL
 * relacionadas con la tabla usuario. Asi las ventanas no tienen SQL.
 */
public class UsuarioDAO {

    // Busca un usuario activo con ese nombre de usuario y clave.
    // Devuelve el Usuario si los datos son correctos, o null si no.
    // Lanza SQLException si no se puede conectar a la base de datos.
    public Usuario autenticar(String nombreUsuario, String clave) throws SQLException {
        String sql = "SELECT id_usuario, nombres, apellidos, usuario, rol "
                   + "FROM usuario WHERE usuario = ? AND clave = ? AND activo = 1";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setString(1, nombreUsuario);
            ps.setString(2, SeguridadUtil.sha256(clave));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getInt("id_usuario"),
                            rs.getString("nombres"),
                            rs.getString("apellidos"),
                            rs.getString("usuario"),
                            rs.getString("rol"));
                }
            }
        }
        return null; // usuario o clave incorrectos
    }

    // Lista todas las cuentas (para la pantalla del administrador)
    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id_usuario, nombres, apellidos, usuario, rol, activo FROM usuario ORDER BY rol, apellidos";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Usuario u = new Usuario(rs.getInt("id_usuario"), rs.getString("nombres"),
                        rs.getString("apellidos"), rs.getString("usuario"), rs.getString("rol"));
                u.setActivo(rs.getInt("activo") == 1);
                lista.add(u);
            }
        }
        return lista;
    }

    public boolean existeUsuario(String nombreUsuario, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM usuario WHERE usuario = ? AND id_usuario <> ?";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nombreUsuario);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public void insertar(Usuario u, String clave) throws SQLException {
        String sql = "INSERT INTO usuario (nombres, apellidos, usuario, clave, rol) VALUES (?, ?, ?, ?, ?)";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getNombres());
            ps.setString(2, u.getApellidos());
            ps.setString(3, u.getUsuario());
            ps.setString(4, SeguridadUtil.sha256(clave));
            ps.setString(5, u.getRol());
            ps.executeUpdate();
        }
    }

    public void actualizar(Usuario u) throws SQLException {
        String sql = "UPDATE usuario SET nombres = ?, apellidos = ?, usuario = ?, rol = ? WHERE id_usuario = ?";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, u.getNombres());
            ps.setString(2, u.getApellidos());
            ps.setString(3, u.getUsuario());
            ps.setString(4, u.getRol());
            ps.setInt(5, u.getIdUsuario());
            ps.executeUpdate();
        }
    }

    public void cambiarClave(int idUsuario, String nuevaClave) throws SQLException {
        String sql = "UPDATE usuario SET clave = ? WHERE id_usuario = ?";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, SeguridadUtil.sha256(nuevaClave));
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    // Activa o desactiva una cuenta (no se borran para no perder el historial)
    public void cambiarActivo(int idUsuario, boolean activo) throws SQLException {
        String sql = "UPDATE usuario SET activo = ? WHERE id_usuario = ?";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, activo ? 1 : 0);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }
}
