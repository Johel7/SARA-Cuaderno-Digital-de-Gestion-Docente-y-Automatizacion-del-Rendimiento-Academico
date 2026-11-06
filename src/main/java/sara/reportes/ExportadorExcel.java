package sara.reportes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import sara.modelo.ResultadoTabla;

/*
 * Clase ExportadorExcel
 * Convierte cualquier ResultadoTabla en un archivo Excel (.xlsx) usando
 * la libreria Apache POI. Los numeros se guardan como numeros para que
 * se puedan usar en formulas dentro de Excel.
 */
public class ExportadorExcel {

    public static void exportar(File destino, ResultadoTabla tabla) throws IOException {
        try (Workbook libro = new XSSFWorkbook()) {
            Sheet hoja = libro.createSheet("Reporte");

            // estilo del titulo
            Font fuenteTitulo = libro.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);
            CellStyle estiloTitulo = libro.createCellStyle();
            estiloTitulo.setFont(fuenteTitulo);

            // estilo de los encabezados de la tabla
            Font fuenteEncabezado = libro.createFont();
            fuenteEncabezado.setBold(true);
            CellStyle estiloEncabezado = libro.createCellStyle();
            estiloEncabezado.setFont(fuenteEncabezado);
            estiloEncabezado.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            estiloEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estiloEncabezado.setBorderBottom(BorderStyle.THIN);

            // titulo y subtitulo
            hoja.createRow(0).createCell(0).setCellValue(tabla.getTitulo());
            hoja.getRow(0).getCell(0).setCellStyle(estiloTitulo);
            hoja.createRow(1).createCell(0).setCellValue(tabla.getSubtitulo());

            // encabezados (fila 3)
            String[] encabezados = tabla.getEncabezados();
            Row filaEncabezado = hoja.createRow(3);
            for (int c = 0; c < encabezados.length; c++) {
                Cell celda = filaEncabezado.createCell(c);
                celda.setCellValue(encabezados[c]);
                celda.setCellStyle(estiloEncabezado);
            }

            // datos (desde la fila 4)
            int numeroFila = 4;
            List<Object[]> filas = tabla.getFilas();
            for (Object[] datos : filas) {
                Row fila = hoja.createRow(numeroFila++);
                for (int c = 0; c < datos.length; c++) {
                    Object valor = datos[c];
                    if (valor == null) {
                        continue; // celda vacia
                    }
                    Cell celda = fila.createCell(c);
                    if (valor instanceof Number) {
                        celda.setCellValue(((Number) valor).doubleValue());
                    } else {
                        celda.setCellValue(valor.toString());
                    }
                }
            }

            // ancho de cada columna segun el texto mas largo
            for (int c = 0; c < encabezados.length; c++) {
                int maximo = encabezados[c].length();
                for (Object[] datos : filas) {
                    if (c < datos.length && datos[c] != null) {
                        maximo = Math.max(maximo, datos[c].toString().length());
                    }
                }
                hoja.setColumnWidth(c, Math.min(60, maximo + 3) * 256);
            }

            try (FileOutputStream salida = new FileOutputStream(destino)) {
                libro.write(salida);
            }
        }
    }
}
