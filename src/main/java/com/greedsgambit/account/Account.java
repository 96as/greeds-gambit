package com.greedsgambit.account;

import java.util.Objects;

/** A signed-in player. {@code username} keeps the capitalisation chosen at sign-up. */
public record Account(String username) {

    public Account {
        Objects.requireNonNull(username, "username");
    }
}
