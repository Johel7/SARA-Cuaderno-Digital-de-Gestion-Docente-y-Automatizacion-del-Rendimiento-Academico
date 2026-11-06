package sara.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * Clase SeguridadUtil
 * Convierte una clave a su hash SHA-256 en hexadecimal. En la base de
 * datos nunca se guarda la clave real, solo este hash (igual que
 * SHA2(clave, 256) en MySQL).
 */
public class SeguridadUtil {

    public static String sha256(String texto) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(texto.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 siempre existe en Java, este caso no deberia ocurrir
            throw new IllegalStateException("SHA-256 no disponible", e);
        }
    }
}
