package sara.modelo;

import java.util.List;

/*
 * Clase ResultadoTabla
 * Es el resultado de una consulta lista para mostrarse en pantalla o
 * exportarse: un titulo, un subtitulo, los encabezados y las filas.
 */
public class ResultadoTabla {

    private String titulo;
    private String subtitulo;
    private String[] encabezados;
    private List<Object[]> filas;

    public ResultadoTabla(String titulo, String subtitulo, String[] encabezados, List<Object[]> filas) {
        this.titulo = titulo;
        this.subtitulo = subtitulo;
        this.encabezados = encabezados;
        this.filas = filas;
    }

    public String getTitulo() { return titulo; }

    public String getSubtitulo() { return subtitulo; }

    public String[] getEncabezados() { return encabezados; }

    public List<Object[]> getFilas() { return filas; }
}
