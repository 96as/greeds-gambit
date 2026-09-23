package com.greedsgambit.account;

/** What is persisted for an account: never the password itself, only its salted hash. */
public record StoredCredential(String username, String passwordHash) {
}
