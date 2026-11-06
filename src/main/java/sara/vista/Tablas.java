package sara.vista;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/*
 * Clase Tablas
 * Metodos para crear tablas con el mismo formato en todas las ventanas.
 */
public class Tablas {

    // Modelo de tabla en el que no se puede editar ninguna celda
    public static DefaultTableModel modeloNoEditable(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    // Tabla con el formato estandar del sistema
    public static JTable crear(javax.swing.table.TableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(24);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setDefaultRenderer(Object.class, new RenderTabla());
        return tabla;
    }
}
