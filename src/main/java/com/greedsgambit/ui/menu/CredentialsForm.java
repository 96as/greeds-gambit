package com.greedsgambit.ui.menu;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.Optional;

/** Username and password fields plus a message line, shared by the log-in and sign-up pages. */
final class CredentialsForm {

    private final TextField username = new TextField();
    private final PasswordField password = new PasswordField();
    private final PasswordField confirmation;
    private final Label message = new Label();
    private final GridPane grid = new GridPane();

    CredentialsForm(boolean askForConfirmation) {
        grid.setHgap(12);
        grid.setVgap(12);
        grid.addRow(0, new Label("Username"), username);
        grid.addRow(1, new Label("Password"), password);
        if (askForConfirmation) {
            confirmation = new PasswordField();
            grid.addRow(2, new Label("Confirm"), confirmation);
        } else {
            confirmation = null;
        }
        message.getStyleClass().add("error-label");
        message.setWrapText(true);
        message.setMaxWidth(460);
    }

    Node fields() {
        return grid;
    }

    Label message() {
        return message;
    }

    String username() {
        return username.getText().strip();
    }

    char[] password() {
        return password.getText().toCharArray();
    }

    Optional<char[]> confirmation() {
        return Optional.ofNullable(confirmation).map(field -> field.getText().toCharArray());
    }

    void clearPasswords() {
        password.clear();
        if (confirmation != null) {
            confirmation.clear();
        }
    }

    void showError(String text) {
        message.setText(text);
    }

    void focus() {
        username.requestFocus();
    }
}
