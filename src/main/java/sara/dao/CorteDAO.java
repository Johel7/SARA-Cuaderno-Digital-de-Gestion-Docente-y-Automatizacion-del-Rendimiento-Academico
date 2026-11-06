package sara.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sara.modelo.Corte;
import sara.util.ConexionBD;

/*
 * Clase CorteDAO
 * Consulta de los cortes evaluativos (I, II, III y IV corte).
 */
public class CorteDAO {

    public List<Corte> listar() throws SQLException {
        List<Corte> lista = new ArrayList<>();
        String sql = "SELECT id_corte, nombre FROM corte ORDER BY id_corte";

        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new Corte(rs.getInt("id_corte"), rs.getString("nombre")));
            }
        }
        return lista;
    }
}
