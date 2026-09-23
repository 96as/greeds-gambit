package com.greedsgambit.account;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountServiceTest {

    @TempDir
    Path dataDir;

    private Path accountsFile;
    private AccountService accounts;

    @BeforeEach
    void setUp() {
        accountsFile = dataDir.resolve("accounts.properties");
        accounts = new AccountService(new FileCredentialStore(accountsFile), new PasswordHasher(1_000));
    }

    private SignUpResult signUp(String username, String password) {
        return accounts.signUp(username, password.toCharArray(), password.toCharArray());
    }

    @Test
    void signedUpPlayersCanLogIn() {
        assertEquals(SignUpResult.CREATED, signUp("Salameh", "1234"));

        assertEquals(Optional.of(new Account("Salameh")), accounts.logIn("Salameh", "1234".toCharArray()));
    }

    @Test
    void wrongPasswordOrUnknownUserCannotLogIn() {
        signUp("Salameh", "1234");

        assertTrue(accounts.logIn("Salameh", "4321".toCharArray()).isEmpty());
        assertTrue(accounts.logIn("Nobody", "1234".toCharArray()).isEmpty());
    }

    /**
     * Regression test: the original game matched "Username: sa" and "Password: 12" as substrings
     * of the saved line, so "sa" / "12" logged in as "Salameh" / "1234".
     */
    @Test
    void partialUsernameAndPasswordDoNotLogIn() {
        signUp("Salameh", "1234");

        assertTrue(accounts.logIn("Sal", "1234".toCharArray()).isEmpty());
        assertTrue(accounts.logIn("Salameh", "12".toCharArray()).isEmpty());
    }

    @Test
    void usernamesAreUniqueIgnoringCaseButKeepTheirSpelling() {
        signUp("Salameh", "1234");

        assertEquals(SignUpResult.USERNAME_TAKEN, signUp("salameh", "abcd"));
        assertEquals(Optional.of(new Account("Salameh")), accounts.logIn("SALAMEH", "1234".toCharArray()));
    }

    @Test
    void rejectsInvalidInput() {
        assertEquals(SignUpResult.INVALID_USERNAME, signUp("ab", "1234"));
        assertEquals(SignUpResult.INVALID_USERNAME, signUp("has space", "1234"));
        assertEquals(SignUpResult.INVALID_USERNAME, signUp("a.b=c", "1234"));
        assertEquals(SignUpResult.INVALID_PASSWORD, signUp("player", "123"));
        assertEquals(SignUpResult.PASSWORDS_DO_NOT_MATCH,
                accounts.signUp("player", "1234".toCharArray(), "1235".toCharArray()));
    }

    @Test
    void passwordsAreNotSavedInPlainText() throws IOException {
        signUp("player", "secret-password");

        String saved = Files.readString(accountsFile);
        assertTrue(saved.contains("player"));
        assertFalse(saved.contains("secret-password"));
    }

    @Test
    void accountsSurviveARestart() {
        signUp("player", "12345");

        AccountService afterRestart =
                new AccountService(new FileCredentialStore(accountsFile), new PasswordHasher(1_000));

        assertTrue(afterRestart.logIn("player", "12345".toCharArray()).isPresent());
    }
}
