package sara.reportes;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import sara.modelo.ResultadoTabla;

/*
 * Clase ExportadorPDF
 * Convierte cualquier ResultadoTabla en un archivo PDF (hoja horizontal)
 * usando la libreria OpenPDF.
 */
public class ExportadorPDF {

    public static void exportar(File destino, ResultadoTabla tabla) throws IOException {
        Document documento = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(documento, new FileOutputStream(destino));
            documento.open();

            Font fuenteTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font fuenteSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font fuenteEncabezado = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9);
            Font fuenteCelda = FontFactory.getFont(FontFactory.HELVETICA, 9);

            documento.add(new Paragraph(tabla.getTitulo(), fuenteTitulo));
            documento.add(new Paragraph(tabla.getSubtitulo(), fuenteSubtitulo));
            documento.add(new Paragraph("Generado por SARA el " + LocalDate.now(), fuenteSubtitulo));
            documento.add(new Paragraph(" "));

            String[] encabezados = tabla.getEncabezados();
            PdfPTable tablaPdf = new PdfPTable(encabezados.length);
            tablaPdf.setWidthPercentage(100);

            // encabezados con fondo gris
            for (String encabezado : encabezados) {
                PdfPCell celda = new PdfPCell(new Phrase(encabezado, fuenteEncabezado));
                celda.setBackgroundColor(Color.LIGHT_GRAY);
                celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                celda.setPadding(4);
                tablaPdf.addCell(celda);
            }
            tablaPdf.setHeaderRows(1); // se repite en cada pagina

            // filas de datos
            for (Object[] datos : tabla.getFilas()) {
                for (int c = 0; c < encabezados.length; c++) {
                    Object valor = (c < datos.length) ? datos[c] : null;
                    String texto = "";
                    if (valor instanceof Double) {
                        texto = String.format("%.2f", (Double) valor);
                    } else if (valor != null) {
                        texto = valor.toString();
                    }

                    PdfPCell celda = new PdfPCell(new Phrase(texto, fuenteCelda));
                    celda.setPadding(3);
                    if (valor instanceof Number) {
                        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
                    }
                    tablaPdf.addCell(celda);
                }
            }

            documento.add(tablaPdf);
        } catch (DocumentException e) {
            throw new IOException("No se pudo crear el PDF: " + e.getMessage(), e);
        } finally {
            if (documento.isOpen()) {
                documento.close();
            }
        }
    }
}
