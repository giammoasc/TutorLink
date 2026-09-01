package it.uniroma2.tutorlink.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.HexFormat;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Calcola e verifica il digest della password. Ogni utente ha il suo sale casuale.
public final class PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH_BITS = 256;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final HexFormat HEX = HexFormat.of();

    private PasswordHasher() {
        // costruttore privato: la classe non va istanziata
    }

    public static String hash(char[] rawPassword) {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        RANDOM.nextBytes(salt);
        byte[] digest = derive(rawPassword, salt);
        return HEX.formatHex(salt) + ':' + HEX.formatHex(digest);
    }

    public static boolean matches(char[] rawPassword, String stored) {
        int separator = stored == null ? -1 : stored.indexOf(':');
        if (separator <= 0) {
            return false;
        }
        byte[] salt = HEX.parseHex(stored.substring(0, separator));
        byte[] expected = HEX.parseHex(stored.substring(separator + 1));
        byte[] actual = derive(rawPassword, salt);
        return java.security.MessageDigest.isEqual(expected, actual);
    }

    private static byte[] derive(char[] rawPassword, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(rawPassword, salt, ITERATIONS, KEY_LENGTH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // PBKDF2 c'e' in tutte le JVM: se manca, il problema non e' del programma
            throw new IllegalStateException("PBKDF2 is not available on this JVM", e);
        } finally {
            spec.clearPassword();
        }
    }

    public static void wipe(char[] rawPassword) {
        if (rawPassword != null) {
            Arrays.fill(rawPassword, '\0');
        }
    }
}
