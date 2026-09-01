package it.uniroma2.tutorlink.boundary.desktop;

import it.uniroma2.tutorlink.bean.AvailabilityBean;
import it.uniroma2.tutorlink.bean.FeedbackBean;
import it.uniroma2.tutorlink.bean.LessonBean;
import it.uniroma2.tutorlink.bean.MaterialBean;
import it.uniroma2.tutorlink.bean.NotificationBean;
import it.uniroma2.tutorlink.boundary.common.AbstractGraphicController;
import it.uniroma2.tutorlink.boundary.common.Navigator;
import it.uniroma2.tutorlink.boundary.common.View;
import it.uniroma2.tutorlink.control.AvailabilityController;
import it.uniroma2.tutorlink.control.LessonExecutionController;
import it.uniroma2.tutorlink.control.LessonMaterialController;
import it.uniroma2.tutorlink.control.NotificationController;
import it.uniroma2.tutorlink.control.SessionController;
import it.uniroma2.tutorlink.exception.TutorLinkException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

// Area tutor a schede: calendario, lezioni, materiale, notifiche.
public class DesktopTutorGraphicController extends AbstractGraphicController implements View {
    private final SessionController sessionController = new SessionController();
    private final AvailabilityController availabilityController = new AvailabilityController();
    private final LessonExecutionController executionController = new LessonExecutionController();
    private final LessonMaterialController materialController = new LessonMaterialController();
    private final NotificationController notificationController = new NotificationController();

    private final TextField dateField = new TextField();
    private final TextField timeField = new TextField();
    private final TextField minutesField = new TextField();
    private final ListView<String> availabilityList = new ListView<>();
    private final ListView<String> lessonList = new ListView<>();
    private final ListView<String> materialList = new ListView<>();
    private final ListView<String> inboxList = new ListView<>();
    private final TextField titleField = new TextField();
    private final Label selectedFileLabel = new Label("no file selected");
    private final TextField scoreField = new TextField();
    private final TextArea commentArea = new TextArea();
    private final Label quotaLabel = new Label();

    private final List<LessonBean> lessons = new ArrayList<>();
    private final List<MaterialBean> materials = new ArrayList<>();
    private final List<NotificationBean> notifications = new ArrayList<>();
    private File chosenFile;

    public DesktopTutorGraphicController(Navigator navigator) {
        super(navigator);
    }

    @Override
    public String title() {
        return "Tutor area";
    }

    @Override
    public Parent root() {
        TabPane tabs = new TabPane();
        tabs.getTabs().addAll(
                new Tab("My calendar", availabilityPane()),
                new Tab("My lessons", lessonsPane()),
                new Tab("Material", materialPane()),
                new Tab("Inbox", inboxPane()));

        Label header = new Label("Signed in as " + sessionController.currentUserName());
        Button logout = new Button("Sign out");
        logout.setOnAction(event -> {
            sessionController.logout();
            navigator().showLogin();
        });
        HBox top = new HBox(16, header, logout);
        top.setPadding(new Insets(12));

        BorderPane layout = new BorderPane();
        layout.setTop(top);
        layout.setCenter(tabs);
        refreshAvailabilities();
        refreshLessons();
        refreshInbox();
        return layout;
    }

    private Parent availabilityPane() {
        dateField.setPromptText("dd/MM/yyyy");
        timeField.setPromptText("HH:mm");
        minutesField.setPromptText("60");

        Button publishButton = new Button("Publish the slot");
        publishButton.setOnAction(event -> onPublish());

        HBox form = new HBox(10, new Label("Date"), dateField, new Label("Time"), timeField,
                new Label("Minutes"), minutesField, publishButton);
        VBox layout = new VBox(12, new Label("Add a time availability"), form,
                new Label("Published slots"), availabilityList);
        layout.setPadding(new Insets(16));
        return layout;
    }

    private void onPublish() {
        AvailabilityBean request = new AvailabilityBean();
        request.setDate(dateField.getText());
        request.setTime(timeField.getText());
        request.setMinutes(minutesField.getText());
        try {
            request.validateSyntax();
            availabilityController.publish(request);
            navigator().info("Slot published",
                    "The students who already took a lesson with you have been notified.");
            refreshAvailabilities();
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private void refreshAvailabilities() {
        try {
            availabilityList.getItems().setAll(availabilityController.myAvailabilities().stream()
                    .map(bean -> bean + (bean.isReserved() ? "  [booked]" : "  [free]"))
                    .toList());
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private Parent lessonsPane() {
        scoreField.setPromptText("score 1-10");
        commentArea.setPrefRowCount(3);
        commentArea.setPromptText("comment for the student");

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> refreshLessons());
        Button closeButton = new Button("Close the lesson with this feedback");
        closeButton.setOnAction(event -> onCloseLesson());

        VBox layout = new VBox(12, new Label("Lessons you deliver"), lessonList,
                new HBox(10, scoreField, closeButton, refreshButton), commentArea);
        layout.setPadding(new Insets(16));
        return layout;
    }

    private void refreshLessons() {
        try {
            lessons.clear();
            lessons.addAll(executionController.myDeliveredLessons());
            lessonList.getItems().setAll(lessons.stream().map(LessonBean::toString).toList());
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private void onCloseLesson() {
        selectedLesson().ifPresent(lesson -> {
            FeedbackBean feedback = new FeedbackBean();
            feedback.setLessonId(lesson.getId());
            feedback.setScore(scoreField.getText());
            feedback.setComment(commentArea.getText());
            try {
                feedback.validateSyntax();
                executionController.close(feedback);
                navigator().info("Lesson closed", "The progress of the student has been updated.");
                refreshLessons();
            } catch (TutorLinkException e) {
                report(e);
            }
        });
    }

    private Optional<LessonBean> selectedLesson() {
        int index = lessonList.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= lessons.size()) {
            navigator().info("Select a lesson", "Pick one lesson from the list first.");
            return Optional.empty();
        }
        return Optional.of(lessons.get(index));
    }

    private Parent materialPane() {
        titleField.setPromptText("title of the material");

        Button chooseButton = new Button("Choose a file");
        chooseButton.setOnAction(event -> onChooseFile());
        Button attachButton = new Button("Attach as draft");
        attachButton.setOnAction(event -> onAttach());
        Button publishButton = new Button("Publish and notify the student");
        publishButton.setOnAction(event -> onPublishMaterial());
        Button listButton = new Button("Show the material of the lesson");
        listButton.setOnAction(event -> onListMaterial());

        VBox layout = new VBox(12,
                new Label("Select a lesson in the 'My lessons' tab, then work here"),
                new HBox(10, titleField, chooseButton, selectedFileLabel),
                new HBox(10, attachButton, publishButton, listButton),
                quotaLabel, materialList);
        layout.setPadding(new Insets(16));
        return layout;
    }

    private void onChooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose the file to share");
        chosenFile = chooser.showOpenDialog(null);
        selectedFileLabel.setText(chosenFile == null ? "no file selected" : chosenFile.getName());
    }

    private void onAttach() {
        if (chosenFile == null) {
            navigator().info("Select a file", "Choose the file you want to share first.");
            return;
        }
        selectedLesson().ifPresent(lesson -> {
            MaterialBean request = new MaterialBean();
            request.setLessonId(lesson.getId());
            request.setTitle(titleField.getText());
            request.setFileName(chosenFile.getName());
            request.setSourcePath(chosenFile.getAbsolutePath());
            request.setSizeBytes(chosenFile.length());
            try {
                request.validateSyntax();
                materialController.attach(request);
                navigator().info("File attached", "It is a draft: publish it to make it visible.");
                onListMaterial();
            } catch (TutorLinkException e) {
                report(e);
            }
        });
    }

    private void onPublishMaterial() {
        selectedLesson().ifPresent(lesson -> {
            try {
                List<MaterialBean> published = materialController.publish(lesson.getId());
                if (published.isEmpty()) {
                    navigator().info("Nothing to publish", "There is no draft attached to this lesson.");
                } else {
                    navigator().info("Material published",
                            published.size() + " file(s) are now visible to the student, who has been notified.");
                }
                onListMaterial();
            } catch (TutorLinkException e) {
                report(e);
            }
        });
    }

    private void onListMaterial() {
        selectedLesson().ifPresent(lesson -> {
            try {
                materials.clear();
                materials.addAll(materialController.materialsOf(lesson.getId()));
                materialList.getItems().setAll(materials.stream().map(MaterialBean::toString).toList());
                quotaLabel.setText("residual quota: "
                        + materialController.residualQuota(lesson.getId()) / 1024 + " KB");
            } catch (TutorLinkException e) {
                report(e);
            }
        });
    }

    private Parent inboxPane() {
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> refreshInbox());
        VBox layout = new VBox(12, new Label("Notifications"), inboxList, refreshButton);
        layout.setPadding(new Insets(16));
        return layout;
    }

    private void refreshInbox() {
        try {
            notifications.clear();
            notifications.addAll(notificationController.inbox());
            inboxList.getItems().setAll(notifications.stream().map(NotificationBean::toString).toList());
        } catch (TutorLinkException e) {
            report(e);
        }
    }
}
