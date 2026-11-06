package sara.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
 * Clase ConexionBD
 * Se encarga de abrir la conexion con la base de datos MySQL "sara".
 
 */
public class ConexionBD {

    private static final String URL =
            "jdbc:mysql://localhost:3306/sara?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Managua";
    private static final String USUARIO = "root";
    private static final String CLAVE = "miBDpersonal001";   // <-- poner aqui la clave de MySQL de cada quien

    // Abre y devuelve una conexion nueva. Quien la pida debe cerrarla
    // (se usa con try-with-resources en los DAO).
    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}
