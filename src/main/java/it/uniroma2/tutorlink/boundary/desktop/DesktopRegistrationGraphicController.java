package it.uniroma2.tutorlink.boundary.desktop;

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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DesktopRegistrationGraphicController extends AbstractGraphicController implements View {
    private final SessionController sessionController = new SessionController();

    private final TextField emailField = new TextField();
    private final TextField nameField = new TextField();
    private final PasswordField passwordField = new PasswordField();
    private final PasswordField confirmField = new PasswordField();
    private final ComboBox<String> roleBox = new ComboBox<>();
    private final TextField rateField = new TextField();
    private final List<CheckBox> subjectBoxes = new ArrayList<>();

    public DesktopRegistrationGraphicController(Navigator navigator) {
        super(navigator);
    }

    @Override
    public String title() {
        return "Create account";
    }

    @Override
    public Parent root() {
        roleBox.getItems().addAll("STUDENT", "TUTOR");
        roleBox.setValue("STUDENT");
        rateField.setPromptText("hourly rate, tutors only");

        HBox subjects = new HBox(10);
        for (Subject subject : Subject.values()) {
            CheckBox box = new CheckBox(subject.displayName());
            subjectBoxes.add(box);
            subjects.getChildren().add(box);
        }

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.add(new Label("E-mail"), 0, 0);
        form.add(emailField, 1, 0);
        form.add(new Label("Full name"), 0, 1);
        form.add(nameField, 1, 1);
        form.add(new Label("Password"), 0, 2);
        form.add(passwordField, 1, 2);
        form.add(new Label("Repeat password"), 0, 3);
        form.add(confirmField, 1, 3);
        form.add(new Label("Role"), 0, 4);
        form.add(roleBox, 1, 4);
        form.add(new Label("Hourly rate"), 0, 5);
        form.add(rateField, 1, 5);
        form.add(new Label("Subjects"), 0, 6);
        form.add(subjects, 1, 6);

        Button createButton = new Button("Create the account");
        createButton.setOnAction(event -> onCreate());
        Button backButton = new Button("Back to sign in");
        backButton.setOnAction(event -> navigator().showLogin());

        VBox layout = new VBox(16, new Label("New account"), form,
                new HBox(12, createButton, backButton));
        layout.setPadding(new Insets(40));
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
            navigator().info("Account created", "You can now sign in with your credentials.");
            navigator().showLogin();
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private List<String> selectedSubjects() {
        List<String> selected = new ArrayList<>();
        for (CheckBox box : subjectBoxes) {
            if (box.isSelected()) {
                selected.add(box.getText());
            }
        }
        return selected;
    }
}
