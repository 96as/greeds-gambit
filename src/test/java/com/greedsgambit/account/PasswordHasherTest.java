package com.greedsgambit.account;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHasherTest {

    private final PasswordHasher hasher = new PasswordHasher(1_000);

    @Test
    void verifiesTheRightPasswordOnly() {
        String hash = hasher.hash("hunter2".toCharArray());

        assertTrue(hasher.verify("hunter2".toCharArray(), hash));
        assertFalse(hasher.verify("hunter3".toCharArray(), hash));
        assertFalse(hasher.verify("hunter".toCharArray(), hash));
    }

    @Test
    void neverContainsThePasswordAndIsSaltedPerAccount() {
        String first = hasher.hash("hunter2".toCharArray());
        String second = hasher.hash("hunter2".toCharArray());

        assertFalse(first.contains("hunter2"));
        assertNotEquals(first, second);
    }

    @Test
    void readsTheIterationCountFromTheStoredHash() {
        String hash = new PasswordHasher(2_000).hash("pw12".toCharArray());

        assertTrue(hasher.verify("pw12".toCharArray(), hash));
    }

    @Test
    void rejectsMalformedHashes() {
        assertFalse(hasher.verify("pw".toCharArray(), ""));
        assertFalse(hasher.verify("pw".toCharArray(), "Password: pw"));
        assertFalse(hasher.verify("pw".toCharArray(), "pbkdf2-sha512$abc$!!$!!"));
    }
}
