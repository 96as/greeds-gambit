package com.greedsgambit.account;

import com.greedsgambit.storage.PropertiesFile;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

/**
 * Stores accounts in a properties file, two entries per account:
 * <pre>
 *   &lt;lowercase name&gt;.name=&lt;name as typed at sign-up&gt;
 *   &lt;lowercase name&gt;.password=&lt;PasswordHasher hash&gt;
 * </pre>
 * Methods are synchronized because logins run on a background thread.
 */
public final class FileCredentialStore implements CredentialStore {

    private final PropertiesFile file;

    public FileCredentialStore(Path path) {
        this.file = new PropertiesFile(path);
    }

    @Override
    public synchronized Optional<StoredCredential> find(String username) {
        Properties accounts = file.load();
        String key = key(username);
        String name = accounts.getProperty(key + ".name");
        String hash = accounts.getProperty(key + ".password");
        if (name == null || hash == null) {
            return Optional.empty();
        }
        return Optional.of(new StoredCredential(name, hash));
    }

    @Override
    public synchronized boolean addIfAbsent(StoredCredential credential) {
        Properties accounts = file.load();
        String key = key(credential.username());
        if (accounts.containsKey(key + ".name")) {
            return false;
        }
        accounts.setProperty(key + ".name", credential.username());
        accounts.setProperty(key + ".password", credential.passwordHash());
        file.save(accounts);
        return true;
    }

    private static String key(String username) {
        return username.toLowerCase(Locale.ROOT);
    }
}
