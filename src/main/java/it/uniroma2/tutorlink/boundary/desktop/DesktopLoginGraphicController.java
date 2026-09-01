package it.uniroma2.tutorlink.boundary.desktop;

import it.uniroma2.tutorlink.bean.CredentialsBean;
import it.uniroma2.tutorlink.boundary.common.AbstractGraphicController;
import it.uniroma2.tutorlink.boundary.common.Navigator;
import it.uniroma2.tutorlink.boundary.common.View;
import it.uniroma2.tutorlink.control.SessionController;
import it.uniroma2.tutorlink.exception.TutorLinkException;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class DesktopLoginGraphicController extends AbstractGraphicController implements View {
    private final SessionController sessionController = new SessionController();

    private final TextField emailField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final Label feedbackLabel = new Label();

    public DesktopLoginGraphicController(Navigator navigator) {
        super(navigator);
    }

    @Override
    public String title() {
        return "Sign in";
    }

    @Override
    public Parent root() {
        Label heading = new Label("TutorLink");
        heading.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");
        Label subtitle = new Label("Book a tutor, join the lesson, keep your material in one place.");

        emailField.setPromptText("name.surname@students.uniroma2.eu");
        passwordField.setPromptText("password");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.add(new Label("E-mail"), 0, 0);
        form.add(emailField, 1, 0);
        form.add(new Label("Password"), 0, 1);
        form.add(passwordField, 1, 1);

        Button loginButton = new Button("Sign in");
        loginButton.setOnAction(event -> onLogin());
        Button registerButton = new Button("Create an account");
        registerButton.setOnAction(event -> navigator().showRegistration());

        VBox layout = new VBox(16, heading, subtitle, form, loginButton, registerButton, feedbackLabel);
        layout.setPadding(new Insets(48));
        layout.setAlignment(Pos.CENTER_LEFT);
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
            feedbackLabel.setText(e.getMessage());
            report(e);
        }
    }
}
