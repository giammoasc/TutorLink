package it.uniroma2.tutorlink.boundary.compact;

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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

// Area tutor a colonna singola.
public class CompactTutorGraphicController extends AbstractGraphicController implements View {
    private static final String SECTION_CALENDAR = "My calendar";
    private static final String SECTION_LESSONS = "My lessons";
    private static final String SECTION_MATERIAL = "Material";
    private static final String SECTION_INBOX = "Inbox";
    private static final String SELECT_LESSON = "Select a lesson";

    private final SessionController sessionController = new SessionController();
    private final AvailabilityController availabilityController = new AvailabilityController();
    private final LessonExecutionController executionController = new LessonExecutionController();
    private final LessonMaterialController materialController = new LessonMaterialController();
    private final NotificationController notificationController = new NotificationController();

    private final VBox content = new VBox(10);
    private final ComboBox<String> sectionBox = new ComboBox<>();
    private final ListView<String> resultList = new ListView<>();
    private final TextField dateField = new TextField();
    private final TextField timeField = new TextField();
    private final TextField minutesField = new TextField();
    private final TextField titleField = new TextField();
    private final TextField scoreField = new TextField();

    private final List<LessonBean> lessons = new ArrayList<>();
    private final List<MaterialBean> materials = new ArrayList<>();
    private final List<NotificationBean> notifications = new ArrayList<>();
    private File chosenFile;

    public CompactTutorGraphicController(Navigator navigator) {
        super(navigator);
    }

    @Override
    public String title() {
        return "Tutor";
    }

    @Override
    public Parent root() {
        sectionBox.getItems().addAll(SECTION_CALENDAR, SECTION_LESSONS, SECTION_MATERIAL, SECTION_INBOX);
        sectionBox.setValue(SECTION_CALENDAR);
        sectionBox.setMaxWidth(Double.MAX_VALUE);
        sectionBox.setOnAction(event -> showSection(sectionBox.getValue()));

        Button logout = new Button("Sign out");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setOnAction(event -> {
            sessionController.logout();
            navigator().showLogin();
        });

        VBox layout = new VBox(12, new Label("Hello " + sessionController.currentUserName()),
                sectionBox, content, logout);
        layout.setPadding(new Insets(20));
        showSection(SECTION_CALENDAR);
        return layout;
    }

    private void render(Node... nodes) {
        content.getChildren().clear();
        content.getChildren().addAll(nodes);
    }

    private void showSection(String section) {
        switch (section) {
            case SECTION_LESSONS -> renderLessons();
            case SECTION_MATERIAL -> renderMaterial();
            case SECTION_INBOX -> renderInbox();
            default -> renderCalendar();
        }
    }

    private void renderCalendar() {
        dateField.setPromptText("dd/MM/yyyy");
        timeField.setPromptText("HH:mm");
        minutesField.setPromptText("minutes, multiple of 30");
        Button publishButton = new Button("Publish the slot");
        publishButton.setMaxWidth(Double.MAX_VALUE);
        publishButton.setOnAction(event -> onPublish());
        refreshAvailabilities();
        render(new Label("Add a time availability"), dateField, timeField, minutesField,
                publishButton, resultList);
    }

    private void onPublish() {
        AvailabilityBean request = new AvailabilityBean();
        request.setDate(dateField.getText());
        request.setTime(timeField.getText());
        request.setMinutes(minutesField.getText());
        try {
            request.validateSyntax();
            availabilityController.publish(request);
            navigator().info("Slot published", "Your students have been notified.");
            refreshAvailabilities();
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private void refreshAvailabilities() {
        try {
            resultList.getItems().setAll(availabilityController.myAvailabilities().stream()
                    .map(AvailabilityBean::toString)
                    .toList());
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private void renderLessons() {
        scoreField.setPromptText("score 1-10");
        Button closeButton = new Button("Close the lesson with this score");
        closeButton.setMaxWidth(Double.MAX_VALUE);
        closeButton.setOnAction(event -> onCloseLesson());
        refreshLessons();
        render(new Label("Lessons you deliver"), resultList, scoreField, closeButton);
    }

    private void refreshLessons() {
        try {
            lessons.clear();
            lessons.addAll(executionController.myDeliveredLessons());
            resultList.getItems().setAll(lessons.stream().map(LessonBean::toString).toList());
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private Optional<LessonBean> selectedLesson() {
        int index = resultList.getSelectionModel().getSelectedIndex();
        if (lessons.isEmpty() || index < 0 || index >= lessons.size()) {
            navigator().info(SELECT_LESSON, "Pick one lesson from the list first.");
            return Optional.empty();
        }
        return Optional.of(lessons.get(index));
    }

    private void onCloseLesson() {
        selectedLesson().ifPresent(lesson -> {
            FeedbackBean feedback = new FeedbackBean();
            feedback.setLessonId(lesson.getId());
            feedback.setScore(scoreField.getText());
            feedback.setComment("");
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

    private void renderMaterial() {
        titleField.setPromptText("title of the material");
        Button chooseButton = new Button("Choose a file");
        chooseButton.setMaxWidth(Double.MAX_VALUE);
        chooseButton.setOnAction(event -> onChooseFile());
        Button attachButton = new Button("Attach as draft");
        attachButton.setMaxWidth(Double.MAX_VALUE);
        attachButton.setOnAction(event -> onAttach());
        Button publishButton = new Button("Publish and notify");
        publishButton.setMaxWidth(Double.MAX_VALUE);
        publishButton.setOnAction(event -> onPublishMaterial());
        refreshLessons();
        render(new Label("Pick the lesson, then the file"), resultList, titleField,
                chooseButton, attachButton, publishButton);
    }

    private void onChooseFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose the file to share");
        chosenFile = chooser.showOpenDialog(null);
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
                navigator().info("File attached", "Publish it to make it visible to the student.");
            } catch (TutorLinkException e) {
                report(e);
            }
        });
    }

    private void onPublishMaterial() {
        selectedLesson().ifPresent(lesson -> {
            try {
                materials.clear();
                materials.addAll(materialController.publish(lesson.getId()));
                navigator().info("Material published",
                        materials.size() + " file(s) are now visible to the student.");
            } catch (TutorLinkException e) {
                report(e);
            }
        });
    }

    private void renderInbox() {
        Button refreshButton = new Button("Refresh");
        refreshButton.setMaxWidth(Double.MAX_VALUE);
        refreshButton.setOnAction(event -> refreshInbox());
        refreshInbox();
        render(new Label("Notifications"), resultList, refreshButton);
    }

    private void refreshInbox() {
        try {
            notifications.clear();
            notifications.addAll(notificationController.inbox());
            resultList.getItems().setAll(notifications.stream().map(NotificationBean::toString).toList());
        } catch (TutorLinkException e) {
            report(e);
        }
    }
}
