package it.uniroma2.tutorlink.boundary.compact;

import it.uniroma2.tutorlink.bean.CredentialsBean;
import it.uniroma2.tutorlink.boundary.common.AbstractGraphicController;
import it.uniroma2.tutorlink.boundary.common.Navigator;
import it.uniroma2.tutorlink.boundary.common.View;
import it.uniroma2.tutorlink.control.SessionController;
import it.uniroma2.tutorlink.exception.TutorLinkException;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class CompactLoginGraphicController extends AbstractGraphicController implements View {
    private final SessionController sessionController = new SessionController();

    private final TextField emailField = new TextField();
    private final PasswordField passwordField = new PasswordField();

    public CompactLoginGraphicController(Navigator navigator) {
        super(navigator);
    }

    @Override
    public String title() {
        return "Sign in";
    }

    @Override
    public Parent root() {
        Label heading = new Label("TutorLink");
        heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        emailField.setPromptText("e-mail");
        passwordField.setPromptText("password");
        emailField.setMaxWidth(Double.MAX_VALUE);
        passwordField.setMaxWidth(Double.MAX_VALUE);

        Button loginButton = new Button("Sign in");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setOnAction(event -> onLogin());

        Button registerButton = new Button("Create an account");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setOnAction(event -> navigator().showRegistration());

        VBox layout = new VBox(14, heading, emailField, passwordField, loginButton, registerButton);
        layout.setPadding(new Insets(28));
        return layout;
    }

    private void onLogin() {
        CredentialsBean credentials = new CredentialsBean();
        credentials.setEmail(emailField.getText());
        credentials.setPassword(passwordField.getText().toCharArray());
        try {
            credentials.validateSyntax();
            String role = sessionController.login(credentials);
            passwordField.setText("");
            navigator().showHomeFor(role);
        } catch (TutorLinkException e) {
            report(e);
        }
    }
}
