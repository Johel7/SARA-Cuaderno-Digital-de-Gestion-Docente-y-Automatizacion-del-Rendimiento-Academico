package sara.util;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

/*
 * Clase Archivos
 * Cuadros para elegir donde guardar o abrir un archivo.
 */
public class Archivos {

    // Pide donde guardar un archivo. Devuelve null si el usuario cancela.
    // Agrega la extension si hace falta y pregunta antes de reemplazar un archivo.
    public static File elegirDondeGuardar(Component padre, String nombreSugerido,
                                          String descripcion, String extension) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar archivo");
        selector.setSelectedFile(new File(limpiarNombre(nombreSugerido) + "." + extension));
        selector.setFileFilter(new FileNameExtensionFilter(descripcion, extension));

        if (selector.showSaveDialog(padre) != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        File archivo = selector.getSelectedFile();
        if (!archivo.getName().toLowerCase().endsWith("." + extension)) {
            archivo = new File(archivo.getPath() + "." + extension);
        }

        if (archivo.exists() && !Mensajes.confirmar(padre,
                "El archivo \"" + archivo.getName() + "\" ya existe.\n¿Desea reemplazarlo?")) {
            return null;
        }
        return archivo;
    }

    // Pide cual archivo abrir. Devuelve null si el usuario cancela.
    public static File elegirArchivoAbrir(Component padre, String descripcion, String extension) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Abrir archivo");
        selector.setFileFilter(new FileNameExtensionFilter(descripcion, extension));

        if (selector.showOpenDialog(padre) != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        return selector.getSelectedFile();
    }

    // Quita espacios y caracteres raros para que sirva como nombre de archivo
    private static String limpiarNombre(String nombre) {
        return nombre.trim().replaceAll("[^A-Za-z0-9áéíóúÁÉÍÓÚñÑ_-]+", "_");
    }
}
