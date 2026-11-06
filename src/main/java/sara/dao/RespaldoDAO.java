package sara.dao;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import sara.util.ConexionBD;

/*
 * Clase RespaldoDAO
 * Crea y restaura respaldos de la base de datos. El respaldo es un archivo
 * de texto (.sql) con una linea INSERT por cada fila de cada tabla.
 * Esta hecho en Java para no depender de que mysqldump este instalado.
 */
public class RespaldoDAO {

    // Orden en que se respaldan las tablas (primero las que no dependen de otras).
    // Al restaurar se borran en orden inverso.
    private static final String[] TABLAS = {
        "usuario", "grado", "seccion", "estudiante", "asignatura", "corte",
        "matricula", "actividad", "calificacion", "nota", "asistencia", "rendimiento"
    };

    // Crea el archivo de respaldo. Devuelve la cantidad de filas respaldadas.
    public int crearRespaldo(File destino) throws SQLException, IOException {
        int totalFilas = 0;

        try (Connection cn = ConexionBD.obtenerConexion();
             Statement st = cn.createStatement();
             BufferedWriter out = Files.newBufferedWriter(destino.toPath(), StandardCharsets.UTF_8)) {

            out.write("-- Respaldo de SARA creado el " + LocalDateTime.now().withNano(0));
            out.newLine();

            for (String tabla : TABLAS) {
                out.write("-- Tabla " + tabla);
                out.newLine();

                try (ResultSet rs = st.executeQuery("SELECT * FROM " + tabla)) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int columnas = meta.getColumnCount();

                    // lista de columnas: (a, b, c)
                    StringBuilder nombresColumnas = new StringBuilder();
                    for (int i = 1; i <= columnas; i++) {
                        nombresColumnas.append(i == 1 ? "" : ", ").append(meta.getColumnName(i));
                    }

                    while (rs.next()) {
                        StringBuilder valores = new StringBuilder();
                        for (int i = 1; i <= columnas; i++) {
                            valores.append(i == 1 ? "" : ", ").append(formatear(rs.getObject(i)));
                        }
                        out.write("INSERT INTO " + tabla + " (" + nombresColumnas + ") VALUES (" + valores + ");");
                        out.newLine();
                        totalFilas++;
                    }
                }
            }
        }
        return totalFilas;
    }

    // Restaura un respaldo: BORRA los datos actuales y carga los del archivo.
    // Todo ocurre en una transaccion: si algo falla no se pierde nada.
    public int restaurarRespaldo(File origen) throws SQLException, IOException {
        int filasCargadas = 0;

        try (Connection cn = ConexionBD.obtenerConexion();
             Statement st = cn.createStatement();
             BufferedReader in = Files.newBufferedReader(origen.toPath(), StandardCharsets.UTF_8)) {

            cn.setAutoCommit(false);
            try {
                st.execute("SET FOREIGN_KEY_CHECKS = 0");

                // se vacian las tablas en orden inverso
                for (int i = TABLAS.length - 1; i >= 0; i--) {
                    st.execute("DELETE FROM " + TABLAS[i]);
                }

                String linea;
                while ((linea = in.readLine()) != null) {
                    linea = linea.trim();
                    // solo se ejecutan los INSERT (los comentarios empiezan con --)
                    if (linea.startsWith("INSERT INTO ")) {
                        st.execute(linea);
                        filasCargadas++;
                    }
                }

                st.execute("SET FOREIGN_KEY_CHECKS = 1");
                cn.commit();
            } catch (SQLException ex) {
                cn.rollback();
                throw ex;
            }
        }
        return filasCargadas;
    }

    // Convierte un valor de la base en texto listo para un INSERT
    private String formatear(Object valor) {
        if (valor == null) {
            return "NULL";
        }
        if (valor instanceof Boolean) {
            return ((Boolean) valor) ? "1" : "0";
        }
        if (valor instanceof BigDecimal) {
            return ((BigDecimal) valor).toPlainString();
        }
        if (valor instanceof Number) {
            return valor.toString();
        }

        // texto, fechas, etc.: van entre comillas y se escapan los caracteres especiales
        String texto = valor.toString();
        texto = texto.replace("\\", "\\\\");
        texto = texto.replace("'", "\\'");
        texto = texto.replace("\n", "\\n");
        texto = texto.replace("\r", "\\r");
        return "'" + texto + "'";
    }
}
