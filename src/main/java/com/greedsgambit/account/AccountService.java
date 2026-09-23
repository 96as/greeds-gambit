package com.greedsgambit.account;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Sign-up and login rules.
 *
 * <p>Both operations deliberately take time (password hashing), so call them off the UI thread.
 */
public final class AccountService {

    public static final int MIN_PASSWORD_LENGTH = 4;
    public static final int MAX_PASSWORD_LENGTH = 64;
    private static final Pattern USERNAME = Pattern.compile("[A-Za-z0-9_]{3,16}");

    private final CredentialStore store;
    private final PasswordHasher hasher;

    public AccountService(CredentialStore store, PasswordHasher hasher) {
        this.store = store;
        this.hasher = hasher;
    }

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME.matcher(username).matches();
    }

    public static boolean isValidPassword(char[] password) {
        return password != null
                && password.length >= MIN_PASSWORD_LENGTH
                && password.length <= MAX_PASSWORD_LENGTH;
    }

    public SignUpResult signUp(String username, char[] password, char[] confirmation) {
        if (!isValidUsername(username)) {
            return SignUpResult.INVALID_USERNAME;
        }
        if (!isValidPassword(password)) {
            return SignUpResult.INVALID_PASSWORD;
        }
        if (!Arrays.equals(password, confirmation)) {
            return SignUpResult.PASSWORDS_DO_NOT_MATCH;
        }
        if (store.find(username).isPresent()) {
            return SignUpResult.USERNAME_TAKEN;
        }
        boolean added = store.addIfAbsent(new StoredCredential(username, hasher.hash(password)));
        return added ? SignUpResult.CREATED : SignUpResult.USERNAME_TAKEN;
    }

    /** The account, if the username exists and the password matches; otherwise empty. */
    public Optional<Account> logIn(String username, char[] password) {
        if (!isValidUsername(username) || password == null) {
            return Optional.empty();
        }
        return store.find(username)
                .filter(credential -> hasher.verify(password, credential.passwordHash()))
                .map(credential -> new Account(credential.username()));
    }
}
