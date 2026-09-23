package com.greedsgambit.account;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Salted PBKDF2 password hashing using only the JDK.
 *
 * <p>Hashes are stored as {@code pbkdf2-sha512$<iterations>$<salt>$<hash>} so the cost can be raised
 * later without breaking existing accounts: {@link #verify} reads the iteration count from the hash.
 */
public final class PasswordHasher {

    /** OWASP's 2023 recommendation for PBKDF2-HMAC-SHA512. */
    public static final int DEFAULT_ITERATIONS = 210_000;

    private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
    private static final String PREFIX = "pbkdf2-sha512";
    private static final int SALT_BYTES = 16;
    private static final int HASH_BITS = 512;

    private final SecureRandom random = new SecureRandom();
    private final int iterations;

    public PasswordHasher() {
        this(DEFAULT_ITERATIONS);
    }

    /** Tests use a low iteration count to stay fast; production code uses the default. */
    public PasswordHasher(int iterations) {
        if (iterations < 1) {
            throw new IllegalArgumentException("iterations must be positive");
        }
        this.iterations = iterations;
    }

    public String hash(char[] password) {
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, iterations);
        Base64.Encoder base64 = Base64.getEncoder();
        return String.join("$", PREFIX, Integer.toString(iterations),
                base64.encodeToString(salt), base64.encodeToString(hash));
    }

    public boolean verify(char[] password, String storedHash) {
        String[] parts = storedHash.split("\\$");
        if (parts.length != 4 || !parts[0].equals(PREFIX)) {
            return false;
        }
        try {
            int storedIterations = Integer.parseInt(parts[1]);
            Base64.Decoder base64 = Base64.getDecoder();
            byte[] salt = base64.decode(parts[2]);
            byte[] expected = base64.decode(parts[3]);
            return MessageDigest.isEqual(expected, pbkdf2(password, salt, storedIterations));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, HASH_BITS);
        try {
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(ALGORITHM + " is not available in this JDK", e);
        } finally {
            spec.clearPassword();
        }
    }
}
