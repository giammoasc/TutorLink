package it.uniroma2.tutorlink.boundary.common;

import it.uniroma2.tutorlink.config.AppConfig;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

// L'unico che conosce lo Stage: cambia schermata e mostra i dialoghi.
public class Navigator {
    private static final double WIDE_WIDTH = 1024;
    private static final double WIDE_HEIGHT = 680;
    private static final double NARROW_WIDTH = 460;
    private static final double NARROW_HEIGHT = 720;

    private final Stage stage;
    private final ViewFactory viewFactory;

    public Navigator(Stage stage, ViewFactory viewFactory) {
        this.stage = stage;
        this.viewFactory = viewFactory;
    }


    public void show(View view) {
        boolean compact = "compact".equals(viewFactory.layoutName());
        Scene scene = new Scene(view.root(),
                compact ? NARROW_WIDTH : WIDE_WIDTH,
                compact ? NARROW_HEIGHT : WIDE_HEIGHT);
        stage.setTitle("TutorLink - " + view.title() + "  [" + AppConfig.getInstance().describe() + "]");
        stage.setScene(scene);
        stage.show();
    }

    public void showLogin() {
        show(viewFactory.loginView(this));
    }

    public void showRegistration() {
        show(viewFactory.registrationView(this));
    }

    public void showHomeFor(String role) {
        if ("TUTOR".equals(role)) {
            show(viewFactory.tutorHomeView(this));
        } else {
            show(viewFactory.studentHomeView(this));
        }
    }

    public void error(String header, String message) {
        dialog(Alert.AlertType.ERROR, "Operation not possible", header, message);
    }

    public void info(String header, String message) {
        dialog(Alert.AlertType.INFORMATION, "TutorLink", header, message);
    }

    private void dialog(Alert.AlertType type, String title, String header, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
