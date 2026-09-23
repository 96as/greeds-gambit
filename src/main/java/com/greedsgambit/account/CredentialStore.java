package com.greedsgambit.account;

import java.util.Optional;

/** Persistence for accounts. Usernames are matched case-insensitively. */
public interface CredentialStore {

    Optional<StoredCredential> find(String username);

    /** Saves a new account. Returns false, changing nothing, if the username is already taken. */
    boolean addIfAbsent(StoredCredential credential);
}
