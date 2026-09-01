package it.uniroma2.tutorlink.boundary.compact;

import it.uniroma2.tutorlink.bean.AvailabilityBean;
import it.uniroma2.tutorlink.bean.BookingRequestBean;
import it.uniroma2.tutorlink.bean.LessonBean;
import it.uniroma2.tutorlink.bean.MaterialBean;
import it.uniroma2.tutorlink.bean.NotificationBean;
import it.uniroma2.tutorlink.bean.ProgressPointBean;
import it.uniroma2.tutorlink.bean.ProgressSummaryBean;
import it.uniroma2.tutorlink.bean.TutorBean;
import it.uniroma2.tutorlink.boundary.common.AbstractGraphicController;
import it.uniroma2.tutorlink.boundary.common.Navigator;
import it.uniroma2.tutorlink.boundary.common.View;
import it.uniroma2.tutorlink.control.BookingController;
import it.uniroma2.tutorlink.control.LessonExecutionController;
import it.uniroma2.tutorlink.control.LessonMaterialController;
import it.uniroma2.tutorlink.control.NotificationController;
import it.uniroma2.tutorlink.control.ProgressController;
import it.uniroma2.tutorlink.control.SessionController;
import it.uniroma2.tutorlink.exception.TutorLinkException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

// Area studente a colonna singola, con selettore di sezione.
public class CompactStudentGraphicController extends AbstractGraphicController implements View {
    private static final String SECTION_BOOK = "Book a lesson";
    private static final String SECTION_LESSONS = "My lessons";
    private static final String SECTION_MATERIAL = "Material";
    private static final String SECTION_PROGRESS = "Progress";
    private static final String SECTION_INBOX = "Inbox";
    private static final String PICK_A_LESSON = "Pick one lesson from the list first.";

    private final SessionController sessionController = new SessionController();
    private final BookingController bookingController = new BookingController();
    private final LessonExecutionController executionController = new LessonExecutionController();
    private final LessonMaterialController materialController = new LessonMaterialController();
    private final ProgressController progressController = new ProgressController();
    private final NotificationController notificationController = new NotificationController();

    private final VBox content = new VBox(10);
    private final ComboBox<String> sectionBox = new ComboBox<>();

    private final ComboBox<String> subjectBox = new ComboBox<>();
    private final ListView<String> resultList = new ListView<>();
    private final TextField cardField = new TextField();

    private final List<TutorBean> tutors = new ArrayList<>();
    private final List<AvailabilityBean> slots = new ArrayList<>();
    private final List<LessonBean> lessons = new ArrayList<>();
    private final List<MaterialBean> materials = new ArrayList<>();
    private final List<NotificationBean> notifications = new ArrayList<>();

    public CompactStudentGraphicController(Navigator navigator) {
        super(navigator);
    }

    @Override
    public String title() {
        return "Student";
    }

    @Override
    public Parent root() {
        sectionBox.getItems().addAll(SECTION_BOOK, SECTION_LESSONS, SECTION_MATERIAL,
                SECTION_PROGRESS, SECTION_INBOX);
        sectionBox.setValue(SECTION_BOOK);
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
        showSection(SECTION_BOOK);
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
            case SECTION_PROGRESS -> renderProgress();
            case SECTION_INBOX -> renderInbox();
            default -> renderBooking();
        }
    }

    private void renderBooking() {
        subjectBox.getItems().setAll(bookingController.listSubjects());
        if (subjectBox.getValue() == null && !subjectBox.getItems().isEmpty()) {
            subjectBox.setValue(subjectBox.getItems().get(0));
        }
        subjectBox.setMaxWidth(Double.MAX_VALUE);
        cardField.setPromptText("card number, 16 digits");

        Button tutorsButton = new Button("1. Show the best tutors");
        tutorsButton.setMaxWidth(Double.MAX_VALUE);
        tutorsButton.setOnAction(event -> onSearchTutors());

        Button slotsButton = new Button("2. Show the free slots");
        slotsButton.setMaxWidth(Double.MAX_VALUE);
        slotsButton.setOnAction(event -> onShowSlots());

        Button bookButton = new Button("3. Book and pay");
        bookButton.setMaxWidth(Double.MAX_VALUE);
        bookButton.setOnAction(event -> onConfirmBooking());

        render(subjectBox, tutorsButton, resultList, slotsButton, cardField, bookButton);
    }

    private void onSearchTutors() {
        try {
            tutors.clear();
            slots.clear();
            tutors.addAll(bookingController.rankedTutors(subjectBox.getValue()));
            resultList.getItems().setAll(tutors.stream().map(TutorBean::toString).toList());
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private void onShowSlots() {
        int index = resultList.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= tutors.size()) {
            navigator().info("Select a tutor", "Pick one tutor from the list first.");
            return;
        }
        try {
            slots.clear();
            slots.addAll(bookingController.freeSlots(tutors.get(index).getEmail()));
            resultList.getItems().setAll(slots.stream().map(AvailabilityBean::toString).toList());
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private void onConfirmBooking() {
        int index = resultList.getSelectionModel().getSelectedIndex();
        if (slots.isEmpty() || index < 0 || index >= slots.size()) {
            navigator().info("Select a slot", "Show the free slots and pick one first.");
            return;
        }
        BookingRequestBean request = new BookingRequestBean();
        request.setAvailabilityId(slots.get(index).getId());
        request.setSubject(subjectBox.getValue());
        request.setCardHolder(sessionController.currentUserName());
        request.setCardNumber(cardField.getText());
        request.setCardExpiry("12/30");
        try {
            request.validateSyntax();
            LessonBean lesson = bookingController.confirmBooking(request);
            navigator().info("Booked", lesson.toString());
            onShowSlots();
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private void renderLessons() {
        Button joinButton = new Button("Join the selected lesson");
        joinButton.setMaxWidth(Double.MAX_VALUE);
        joinButton.setOnAction(event -> onJoin());
        Button cancelButton = new Button("Cancel the selected lesson");
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setOnAction(event -> onCancel());
        refreshLessons();
        render(new Label("Your next lessons"), resultList, joinButton, cancelButton);
    }

    private void refreshLessons() {
        try {
            lessons.clear();
            lessons.addAll(executionController.myUpcomingLessons());
            resultList.getItems().setAll(lessons.stream().map(LessonBean::toString).toList());
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private Optional<LessonBean> selectedLesson() {
        int index = resultList.getSelectionModel().getSelectedIndex();
        if (lessons.isEmpty() || index < 0 || index >= lessons.size()) {
            navigator().info("Select a lesson", PICK_A_LESSON);
            return Optional.empty();
        }
        return Optional.of(lessons.get(index));
    }

    private void onJoin() {
        selectedLesson().ifPresent(lesson -> {
            try {
                navigator().info("Meeting link", executionController.join(lesson.getId()));
                refreshLessons();
            } catch (TutorLinkException e) {
                report(e);
            }
        });
    }

    private void onCancel() {
        selectedLesson().ifPresent(lesson -> {
            try {
                executionController.cancel(lesson.getId(), "cancelled by the student");
                refreshLessons();
            } catch (TutorLinkException e) {
                report(e);
            }
        });
    }

    private void renderMaterial() {
        Button loadButton = new Button("Show the material of my first lesson");
        loadButton.setMaxWidth(Double.MAX_VALUE);
        loadButton.setOnAction(event -> onListMaterial());
        refreshLessons();
        render(new Label("Shared material"), resultList, loadButton);
    }

    private void onListMaterial() {
        selectedLesson().ifPresent(lesson -> {
            try {
                materials.clear();
                materials.addAll(materialController.publishedMaterialsForStudent(lesson.getId()));
                resultList.getItems().setAll(materials.isEmpty()
                        ? List.of("no material published yet")
                        : materials.stream().map(MaterialBean::toString).toList());
            } catch (TutorLinkException e) {
                report(e);
            }
        });
    }

    private void renderProgress() {
        LineChart<String, Number> chart = new LineChart<>(new CategoryAxis(), new NumberAxis());
        chart.setTitle("Your scores");
        Label summaryLabel = new Label();
        try {
            ProgressSummaryBean summary = progressController.myProgress();
            summaryLabel.setText("average " + summary.getAverage() + " - " + summary.getTrend());
            summaryLabel.setWrapText(true);
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("score");
            for (ProgressPointBean point : summary.getPoints()) {
                series.getData().add(new XYChart.Data<>(point.getDate(), point.getScore()));
            }
            chart.getData().add(series);
        } catch (TutorLinkException e) {
            report(e);
        }
        render(summaryLabel, chart);
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
