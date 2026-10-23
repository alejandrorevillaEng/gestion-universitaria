package es.arevilla.universidad.seguridad;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Hash de contraseñas con PBKDF2-HMAC-SHA256 (incluido en el JDK, sin
 * dependencias). Formato guardado: pbkdf2_sha256$iteraciones$sal$hash, con
 * sal y hash en Base64. Las iteraciones van en el propio hash, así que se
 * pueden subir sin invalidar las contraseñas antiguas.
 */
public final class Contrasenas {

    /** Recomendación de OWASP para PBKDF2-HMAC-SHA256. */
    public static final int ITERACIONES_POR_DEFECTO = 600_000;
    private static final String PREFIJO = "pbkdf2_sha256";
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Contrasenas() {
    }

    public static String hash(String contrasena) {
        int iteraciones = Integer.getInteger("seguridad.pbkdf2.iteraciones", ITERACIONES_POR_DEFECTO);
        byte[] sal = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] hash = pbkdf2(contrasena, sal, iteraciones);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIJO + "$" + iteraciones + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(hash);
    }

    public static boolean verificar(String contrasena, String guardado) {
        if (contrasena == null || guardado == null) return false;
        String[] partes = guardado.split("\\$");
        if (partes.length != 4 || !PREFIJO.equals(partes[0])) return false;
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            /* Comparación en tiempo constante. */
            return MessageDigest.isEqual(esperado, pbkdf2(contrasena, sal, iteraciones));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(String contrasena, byte[] sal, int iteraciones) {
        PBEKeySpec spec = new PBEKeySpec(contrasena.toCharArray(), sal, iteraciones, BITS_HASH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 no disponible", e);
        } finally {
            spec.clearPassword();
        }
    }
}
