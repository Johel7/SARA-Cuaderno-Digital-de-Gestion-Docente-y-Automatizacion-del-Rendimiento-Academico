package sara.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import sara.modelo.Estudiante;
import sara.util.ConexionBD;

/*
 * Clase EstudianteDAO
 * Todo lo relacionado con estudiantes y sus matriculas. Al registrar un
 * estudiante se guarda tambien su matricula en el mismo paso.
 */
public class EstudianteDAO {

    // Lista los estudiantes matriculados en un anio, con filtros opcionales.
    //   idGrado / idSeccion = 0  -> no filtra por eso
    //   texto: busca en codigo, nombres o apellidos
    public List<Estudiante> listar(int anio, int idGrado, int idSeccion, String texto,
                                   boolean incluirRetirados) throws SQLException {
        List<Estudiante> lista = new ArrayList<>();
        List<Object> parametros = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT e.id_estudiante, e.codigo, e.nombres, e.apellidos, e.sexo, e.fecha_nacimiento, ");
        sql.append("e.direccion, e.telefono, e.id_usuario, m.id_matricula, m.id_grado, m.id_seccion, ");
        sql.append("g.nombre AS grado, s.nombre AS seccion, m.anio_lectivo, m.modalidad, m.estado ");
        sql.append("FROM matricula m ");
        sql.append("JOIN estudiante e ON e.id_estudiante = m.id_estudiante ");
        sql.append("JOIN grado g ON g.id_grado = m.id_grado ");
        sql.append("JOIN seccion s ON s.id_seccion = m.id_seccion ");
        sql.append("WHERE m.anio_lectivo = ? ");
        parametros.add(anio);

        if (idGrado > 0) {
            sql.append("AND m.id_grado = ? ");
            parametros.add(idGrado);
        }
        if (idSeccion > 0) {
            sql.append("AND m.id_seccion = ? ");
            parametros.add(idSeccion);
        }
        if (!incluirRetirados) {
            sql.append("AND m.estado = 'ACTIVA' ");
        }
        if (texto != null && !texto.trim().isEmpty()) {
            sql.append("AND (e.codigo LIKE ? OR e.nombres LIKE ? OR e.apellidos LIKE ?) ");
            String patron = "%" + texto.trim() + "%";
            parametros.add(patron);
            parametros.add(patron);
            parametros.add(patron);
        }
        sql.append("ORDER BY g.id_grado, s.nombre, e.apellidos, e.nombres");

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(leerEstudiante(rs));
                }
            }
        }
        return lista;
    }

    private Estudiante leerEstudiante(ResultSet rs) throws SQLException {
        Estudiante e = new Estudiante();
        e.setId(rs.getInt("id_estudiante"));
        e.setCodigo(rs.getString("codigo"));
        e.setNombres(rs.getString("nombres"));
        e.setApellidos(rs.getString("apellidos"));
        e.setSexo(rs.getString("sexo"));

        Date fecha = rs.getDate("fecha_nacimiento");
        if (fecha != null) {
            e.setFechaNacimiento(fecha.toLocalDate());
        }

        e.setDireccion(rs.getString("direccion"));
        e.setTelefono(rs.getString("telefono"));
        e.setIdUsuario(rs.getInt("id_usuario"));
        e.setIdMatricula(rs.getInt("id_matricula"));
        e.setIdGrado(rs.getInt("id_grado"));
        e.setIdSeccion(rs.getInt("id_seccion"));
        e.setNombreGrado(rs.getString("grado"));
        e.setNombreSeccion(rs.getString("seccion"));
        e.setAnioLectivo(rs.getInt("anio_lectivo"));
        e.setModalidad(rs.getString("modalidad"));
        e.setEstado(rs.getString("estado"));
        return e;
    }

    // Revisa si ya existe un estudiante con ese codigo (idExcluir = 0 al registrar)
    public boolean existeCodigo(String codigo, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM estudiante WHERE codigo = ? AND id_estudiante <> ?";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, codigo);
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    // Registra al estudiante y su matricula. Si algo falla se deshace todo.
    public void registrar(Estudiante e, int idGrado, int idSeccion, int anio, String modalidad)
            throws SQLException {
        String sqlEstudiante = "INSERT INTO estudiante (codigo, nombres, apellidos, sexo, fecha_nacimiento, "
                + "direccion, telefono, id_usuario) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlMatricula = "INSERT INTO matricula (id_estudiante, id_grado, id_seccion, anio_lectivo, "
                + "modalidad, fecha_matricula) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection cn = ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                int idNuevo;
                try (PreparedStatement ps = cn.prepareStatement(sqlEstudiante, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, e.getCodigo());
                    ps.setString(2, e.getNombres());
                    ps.setString(3, e.getApellidos());
                    ps.setString(4, e.getSexo());
                    asignarFecha(ps, 5, e.getFechaNacimiento());
                    ps.setString(6, e.getDireccion());
                    ps.setString(7, e.getTelefono());
                    ps.setInt(8, e.getIdUsuario());
                    ps.executeUpdate();

                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        claves.next();
                        idNuevo = claves.getInt(1);
                    }
                }

                try (PreparedStatement ps = cn.prepareStatement(sqlMatricula)) {
                    ps.setInt(1, idNuevo);
                    ps.setInt(2, idGrado);
                    ps.setInt(3, idSeccion);
                    ps.setInt(4, anio);
                    ps.setString(5, modalidad);
                    ps.setDate(6, Date.valueOf(LocalDate.now()));
                    ps.executeUpdate();
                }

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            }
        }
    }

    // Actualiza los datos personales y el grupo (grado/seccion) de la matricula
    public void actualizar(Estudiante e, int idGrado, int idSeccion, String modalidad) throws SQLException {
        String sqlEstudiante = "UPDATE estudiante SET codigo = ?, nombres = ?, apellidos = ?, sexo = ?, "
                + "fecha_nacimiento = ?, direccion = ?, telefono = ? WHERE id_estudiante = ?";
        String sqlMatricula = "UPDATE matricula SET id_grado = ?, id_seccion = ?, modalidad = ? "
                + "WHERE id_matricula = ?";

        try (Connection cn = ConexionBD.obtenerConexion()) {
            cn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = cn.prepareStatement(sqlEstudiante)) {
                    ps.setString(1, e.getCodigo());
                    ps.setString(2, e.getNombres());
                    ps.setString(3, e.getApellidos());
                    ps.setString(4, e.getSexo());
                    asignarFecha(ps, 5, e.getFechaNacimiento());
                    ps.setString(6, e.getDireccion());
                    ps.setString(7, e.getTelefono());
                    ps.setInt(8, e.getId());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = cn.prepareStatement(sqlMatricula)) {
                    ps.setInt(1, idGrado);
                    ps.setInt(2, idSeccion);
                    ps.setString(3, modalidad);
                    ps.setInt(4, e.getIdMatricula());
                    ps.executeUpdate();
                }

                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            }
        }
    }

    // Dar de baja = marcar la matricula como RETIRADA (no se borra nada).
    // Reactivar = volver a ponerla ACTIVA.
    public void cambiarEstado(int idMatricula, String estado) throws SQLException {
        String sql = "UPDATE matricula SET estado = ? WHERE id_matricula = ?";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idMatricula);
            ps.executeUpdate();
        }
    }

    private void asignarFecha(PreparedStatement ps, int posicion, LocalDate fecha) throws SQLException {
        if (fecha == null) {
            ps.setNull(posicion, Types.DATE);
        } else {
            ps.setDate(posicion, Date.valueOf(fecha));
        }
    }
}
