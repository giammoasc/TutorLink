package it.uniroma2.tutorlink.boundary.compact;

import it.uniroma2.tutorlink.bean.RegistrationBean;
import it.uniroma2.tutorlink.boundary.common.AbstractGraphicController;
import it.uniroma2.tutorlink.boundary.common.Navigator;
import it.uniroma2.tutorlink.boundary.common.View;
import it.uniroma2.tutorlink.control.SessionController;
import it.uniroma2.tutorlink.exception.TutorLinkException;
import it.uniroma2.tutorlink.model.Subject;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class CompactRegistrationGraphicController extends AbstractGraphicController implements View {
    private final SessionController sessionController = new SessionController();

    private final TextField emailField = new TextField();
    private final TextField nameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final PasswordField confirmField = new PasswordField();
    private final ComboBox<String> roleBox = new ComboBox<>();
    private final TextField rateField = new TextField();
    private final ListView<String> subjectList = new ListView<>();

    public CompactRegistrationGraphicController(Navigator navigator) {
        super(navigator);
    }

    @Override
    public String title() {
        return "Create account";
    }

    @Override
    public Parent root() {
        emailField.setPromptText("e-mail");
        nameField.setPromptText("full name");
        passwordField.setPromptText("password");
        confirmField.setPromptText("repeat the password");
        rateField.setPromptText("hourly rate (tutors only)");

        roleBox.getItems().addAll("STUDENT", "TUTOR");
        roleBox.setValue("STUDENT");
        roleBox.setMaxWidth(Double.MAX_VALUE);

        for (Subject subject : Subject.values()) {
            subjectList.getItems().add(subject.displayName());
        }
        subjectList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Button createButton = new Button("Create the account");
        createButton.setMaxWidth(Double.MAX_VALUE);
        createButton.setOnAction(event -> onCreate());
        Button backButton = new Button("Back");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setOnAction(event -> navigator().showLogin());

        VBox layout = new VBox(10, new Label("New account"), emailField, nameField,
                passwordField, confirmField, roleBox, rateField,
                new Label("Subjects (tutors only)"), subjectList, createButton, backButton);
        layout.setPadding(new Insets(24));
        return layout;
    }

    private void onCreate() {
        RegistrationBean registration = new RegistrationBean();
        registration.setEmail(emailField.getText());
        registration.setFullName(nameField.getText());
        registration.setPassword(passwordField.getText().toCharArray());
        registration.setConfirmPassword(confirmField.getText().toCharArray());
        registration.setRole(roleBox.getValue());
        registration.setHourlyRate(rateField.getText());
        registration.setSubjects(selectedSubjects());
        try {
            registration.validateSyntax();
            sessionController.register(registration);
            navigator().info("Account created", "You can now sign in.");
            navigator().showLogin();
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private List<String> selectedSubjects() {
        return new ArrayList<>(subjectList.getSelectionModel().getSelectedItems());
    }
}
