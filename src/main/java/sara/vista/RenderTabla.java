package sara.vista;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/*
 * Clase RenderTabla
 * Controla como se ven las celdas: los decimales se muestran con 2
 * digitos, los numeros y los niveles van centrados y el nivel AI
 * (riesgo) y las asignaturas reprobadas salen en rojo.
 */
public class RenderTabla extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                   boolean conFoco, int fila, int columna) {
        String texto = "";
        if (valor instanceof Double) {
            texto = String.format("%.2f", (Double) valor);
        } else if (valor != null) {
            texto = valor.toString();
        }

        Component celda = super.getTableCellRendererComponent(tabla, texto, seleccionada, conFoco, fila, columna);

        boolean esNivel = "AA".equals(valor) || "AS".equals(valor) || "AF".equals(valor) || "AI".equals(valor);
        boolean esEstado = "En proceso".equals(valor) || "Aprobada".equals(valor) || "Reprobada".equals(valor);
        if (valor instanceof Number || esNivel || esEstado) {
            setHorizontalAlignment(CENTER);
        } else {
            setHorizontalAlignment(LEFT);
        }

        if (!seleccionada) {
            boolean rojo = "AI".equals(valor) || "Reprobada".equals(valor);
            setForeground(rojo ? new Color(190, 30, 30) : tabla.getForeground());
        }
        return celda;
    }
}
