package sara.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/*
 * Clase CalculoNotas
 * Aqui estan las formulas de la nota, en un solo lugar, para que las usen la
 * pantalla de notas, los reportes y el guardado en la base de datos.
 *
 * Como se calcula (evaluacion por cortes del MINED):
 *
 *  1) NOTA DEL CORTE = suma de los puntos de sus actividades.
 *     El porcentaje de cada actividad es su maximo de puntos, y los
 *     porcentajes del corte suman 100. Entonces la nota del corte va de 0 a 100.
 *        Ejemplo: 8 + 13 + 22 + 20 + 15 = 78  ->  nivel AS
 *
 *  2) APORTE A LA FINAL = nota del corte x 25 / 100. Con 4 cortes cada uno
 *     pesa 25 % de la nota final. Un corte con 100 aporta 25; uno con 78 aporta 19.5.
 *
 *  3) NOTA FINAL = suma de los aportes de los 4 cortes (es igual al promedio
 *     de los 4 cortes). Solo existe cuando los 4 cortes ya tienen nota.
 *     Mientras falten cortes se muestra el ACUMULADO (suma de los aportes que
 *     ya hay, sobre 100) y la nota final queda "En proceso".
 */
public class CalculoNotas {

    // Peso de un corte en la nota final: 100 / 4 cortes
    public static final double PESO_CORTE = 25.0;

    // Texto que se muestra cuando todavia no estan los 4 cortes
    public static final String EN_PROCESO = "En proceso";

    // Suma de puntos de un corte. Devuelve null si no hay ninguna nota todavia.
    public static Double sumaPuntos(Double[] puntajes) {
        BigDecimal suma = BigDecimal.ZERO;
        boolean hayNotas = false;

        for (Double p : puntajes) {
            if (p != null) {
                suma = suma.add(BigDecimal.valueOf(p));
                hayNotas = true;
            }
        }
        if (!hayNotas) {
            return null;
        }
        return suma.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // Cuantos puntos de la nota final aporta un corte (de 0 a 25)
    public static Double aporte(Double notaCorte) {
        if (notaCorte == null) {
            return null;
        }
        return BigDecimal.valueOf(notaCorte).multiply(BigDecimal.valueOf(PESO_CORTE))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).doubleValue();
    }

    // Nota final = promedio de todos los cortes. Si falta alguno devuelve null.
    public static Double notaFinal(Double[] notasCortes) {
        if (notasCortes.length == 0) {
            return null;
        }
        BigDecimal suma = BigDecimal.ZERO;
        for (Double n : notasCortes) {
            if (n == null) {
                return null;
            }
            suma = suma.add(BigDecimal.valueOf(n));
        }
        return suma.divide(BigDecimal.valueOf(notasCortes.length), 2, RoundingMode.HALF_UP).doubleValue();
    }

    // Acumulado: puntos de la nota final que el estudiante lleva hasta ahora (sobre 100).
    // Los cortes que faltan no suman. Devuelve null si no tiene ningun corte.
    public static Double acumulado(Double[] notasCortes) {
        if (notasCortes.length == 0) {
            return null;
        }
        BigDecimal suma = BigDecimal.ZERO;
        boolean hayNotas = false;
        for (Double n : notasCortes) {
            if (n != null) {
                suma = suma.add(BigDecimal.valueOf(n));
                hayNotas = true;
            }
        }
        if (!hayNotas) {
            return null;
        }
        return suma.divide(BigDecimal.valueOf(notasCortes.length), 2, RoundingMode.HALF_UP).doubleValue();
    }

    // Redondea a 2 decimales (mitad hacia arriba, igual que ROUND de MySQL)
    public static double redondear(double valor) {
        return BigDecimal.valueOf(valor).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
