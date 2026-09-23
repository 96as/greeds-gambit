package com.greedsgambit.ui.menu;

import com.greedsgambit.account.Account;
import com.greedsgambit.account.AccountService;
import com.greedsgambit.ui.AppRouter;
import com.greedsgambit.ui.Screen;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.greedsgambit.ui.menu.MenuLayout.button;

public final class LoginScreen implements Screen {

    private static final System.Logger LOG = System.getLogger(LoginScreen.class.getName());

    private final AppRouter router;
    private final AccountService accounts;
    private final Executor background;
    private final CredentialsForm form = new CredentialsForm(false);
    private final Parent root;

    public LoginScreen(AppRouter router, AccountService accounts, Executor background) {
        this.router = router;
        this.accounts = accounts;
        this.background = background;

        Button submit = button("Log in", this::submit);
        submit.setDefaultButton(true);
        Button back = button("Back", router::showWelcome);
        back.setCancelButton(true);
        HBox actions = new HBox(12, submit, back);
        actions.setAlignment(Pos.CENTER);

        root = MenuLayout.page("Log in", form.fields(), form.message(), actions);
    }

    /** Password hashing is slow on purpose, so it runs off the UI thread. */
    private void submit() {
        String username = form.username();
        char[] password = form.password();
        form.clearPasswords();
        root.setDisable(true);

        CompletableFuture.supplyAsync(() -> accounts.logIn(username, password), background)
                .whenCompleteAsync((account, error) -> {
                    Arrays.fill(password, '\0');
                    root.setDisable(false);
                    handleResult(account, error);
                }, Platform::runLater);
    }

    private void handleResult(Optional<Account> account, Throwable error) {
        if (error != null) {
            LOG.log(System.Logger.Level.WARNING, "Login failed", error);
            form.showError("Could not read saved accounts. Please try again.");
        } else if (account.isPresent()) {
            router.signedIn(account.get());
        } else {
            form.showError("Incorrect username or password.");
            form.focus();
        }
    }

    @Override
    public Parent root() {
        return root;
    }

    @Override
    public void onShow() {
        form.focus();
    }
}
