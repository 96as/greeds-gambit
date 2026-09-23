package com.greedsgambit.ui.menu;

import com.greedsgambit.account.Account;
import com.greedsgambit.account.AccountService;
import com.greedsgambit.account.SignUpResult;
import com.greedsgambit.ui.AppRouter;
import com.greedsgambit.ui.Screen;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static com.greedsgambit.ui.menu.MenuLayout.button;
import static com.greedsgambit.ui.menu.MenuLayout.label;

public final class SignUpScreen implements Screen {

    private static final System.Logger LOG = System.getLogger(SignUpScreen.class.getName());

    private final AppRouter router;
    private final AccountService accounts;
    private final Executor background;
    private final CredentialsForm form = new CredentialsForm(true);
    private final Parent root;

    public SignUpScreen(AppRouter router, AccountService accounts, Executor background) {
        this.router = router;
        this.accounts = accounts;
        this.background = background;

        Button submit = button("Create account", this::submit);
        submit.setDefaultButton(true);
        Button back = button("Back", router::showWelcome);
        back.setCancelButton(true);
        HBox actions = new HBox(12, submit, back);
        actions.setAlignment(Pos.CENTER);

        root = MenuLayout.page("Sign up",
                label("3-16 letters, digits or _   |   password "
                        + AccountService.MIN_PASSWORD_LENGTH + "-" + AccountService.MAX_PASSWORD_LENGTH
                        + " characters", "muted"),
                form.fields(), form.message(), actions);
    }

    private void submit() {
        String username = form.username();
        char[] password = form.password();
        char[] confirmation = form.confirmation().orElseThrow();
        form.clearPasswords();
        root.setDisable(true);

        CompletableFuture.supplyAsync(() -> accounts.signUp(username, password, confirmation), background)
                .whenCompleteAsync((result, error) -> {
                    Arrays.fill(password, '\0');
                    Arrays.fill(confirmation, '\0');
                    root.setDisable(false);
                    handleResult(username, result, error);
                }, Platform::runLater);
    }

    private void handleResult(String username, SignUpResult result, Throwable error) {
        if (error != null) {
            LOG.log(System.Logger.Level.WARNING, "Sign-up failed", error);
            form.showError("Could not save your account. Please try again.");
            return;
        }
        switch (result) {
            case CREATED -> {
                router.signedIn(new Account(username));
                return;
            }
            case USERNAME_TAKEN -> form.showError("That username is already taken.");
            case INVALID_USERNAME -> form.showError("Usernames are 3-16 letters, digits or underscores.");
            case INVALID_PASSWORD -> form.showError("Passwords are " + AccountService.MIN_PASSWORD_LENGTH
                    + "-" + AccountService.MAX_PASSWORD_LENGTH + " characters long.");
            case PASSWORDS_DO_NOT_MATCH -> form.showError("The passwords do not match.");
        }
        form.focus();
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
