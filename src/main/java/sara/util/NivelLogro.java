package sara.util;

/*
 * Clase NivelLogro
 * Convierte una nota (0 a 100) en el nivel de logro del MINED
 * (sirve para la nota de un corte y para la nota final):
 *   AA = Aprendizaje Avanzado     (90 a 100)
 *   AS = Aprendizaje Satisfactorio (76 a 89)
 *   AF = Aprendizaje Fundamental   (60 a 75)
 *   AI = Aprendizaje Inicial       (menos de 60)
 */
public class NivelLogro {

    public static final double NOTA_APROBACION = 60;

    public static String calcular(double nota) {
        if (nota >= 90) {
            return "AA";
        } else if (nota >= 76) {
            return "AS";
        } else if (nota >= 60) {
            return "AF";
        } else {
            return "AI";
        }
    }

    public static String descripcion(String nivel) {
        if ("AA".equals(nivel)) {
            return "Aprendizaje Avanzado";
        } else if ("AS".equals(nivel)) {
            return "Aprendizaje Satisfactorio";
        } else if ("AF".equals(nivel)) {
            return "Aprendizaje Fundamental";
        } else {
            return "Aprendizaje Inicial";
        }
    }
}
