package it.uniroma2.tutorlink.boundary.desktop;

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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

// Area studente a schede: prenota, lezioni, materiale, progressi, notifiche.
public class DesktopStudentGraphicController extends AbstractGraphicController implements View {
    private final SessionController sessionController = new SessionController();
    private final BookingController bookingController = new BookingController();
    private final LessonExecutionController executionController = new LessonExecutionController();
    private final LessonMaterialController materialController = new LessonMaterialController();
    private final ProgressController progressController = new ProgressController();
    private final NotificationController notificationController = new NotificationController();

    private final ComboBox<String> subjectBox = new ComboBox<>();
    private final ListView<String> tutorList = new ListView<>();
    private final ListView<String> slotList = new ListView<>();
    private final ListView<String> lessonList = new ListView<>();
    private final ListView<String> materialList = new ListView<>();
    private final ListView<String> inboxList = new ListView<>();
    private final TextField holderField = new TextField();
    private final TextField cardField = new TextField();
    private final TextField expiryField = new TextField();
    private final Label progressSummary = new Label();
    private final LineChart<String, Number> progressChart =
            new LineChart<>(new CategoryAxis(), new NumberAxis());

    private final List<TutorBean> tutors = new ArrayList<>();
    private final List<AvailabilityBean> slots = new ArrayList<>();
    private final List<LessonBean> lessons = new ArrayList<>();
    private final List<MaterialBean> materials = new ArrayList<>();
    private final List<NotificationBean> notifications = new ArrayList<>();

    public DesktopStudentGraphicController(Navigator navigator) {
        super(navigator);
    }

    @Override
    public String title() {
        return "Student area";
    }

    @Override
    public Parent root() {
        TabPane tabs = new TabPane();
        tabs.getTabs().addAll(
                new Tab("Book a lesson", bookingPane()),
                new Tab("My lessons", lessonsPane()),
                new Tab("Material", materialPane()),
                new Tab("Progress", progressPane()),
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
        refreshSubjects();
        refreshLessons();
        refreshInbox();
        return layout;
    }

    private Parent bookingPane() {
        Button searchButton = new Button("Search tutors");
        searchButton.setOnAction(event -> onSearchTutors());
        Button slotsButton = new Button("Show free slots");
        slotsButton.setOnAction(event -> onShowSlots());
        Button confirmButton = new Button("Confirm and pay");
        confirmButton.setOnAction(event -> onConfirmBooking());

        holderField.setPromptText("card holder");
        cardField.setPromptText("16 digits");
        expiryField.setPromptText("MM/YY");

        VBox left = new VBox(10, new Label("1. Subject"), subjectBox, searchButton,
                new Label("2. Tutor (best match first)"), tutorList, slotsButton);
        VBox right = new VBox(10, new Label("3. Free slots"), slotList,
                new Label("4. Payment"), holderField, cardField, expiryField, confirmButton);
        left.setPadding(new Insets(16));
        right.setPadding(new Insets(16));

        HBox layout = new HBox(20, left, right);
        layout.setPadding(new Insets(12));
        return layout;
    }

    private void refreshSubjects() {
        subjectBox.getItems().setAll(bookingController.listSubjects());
        if (!subjectBox.getItems().isEmpty()) {
            subjectBox.setValue(subjectBox.getItems().get(0));
        }
    }

    private void onSearchTutors() {
        try {
            tutors.clear();
            tutors.addAll(bookingController.rankedTutors(subjectBox.getValue()));
            tutorList.getItems().setAll(tutors.stream().map(TutorBean::toString).toList());
            if (tutors.isEmpty()) {
                navigator().info("No tutor found", "Nobody teaches this subject at the moment.");
            }
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private void onShowSlots() {
        int index = tutorList.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= tutors.size()) {
            navigator().info("Select a tutor", "Pick one tutor from the list first.");
            return;
        }
        try {
            slots.clear();
            slots.addAll(bookingController.freeSlots(tutors.get(index).getEmail()));
            slotList.getItems().setAll(slots.stream().map(AvailabilityBean::toString).toList());
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private void onConfirmBooking() {
        int index = slotList.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= slots.size()) {
            navigator().info("Select a slot", "Pick one time slot from the list first.");
            return;
        }
        BookingRequestBean request = new BookingRequestBean();
        request.setAvailabilityId(slots.get(index).getId());
        request.setSubject(subjectBox.getValue());
        request.setCardHolder(holderField.getText());
        request.setCardNumber(cardField.getText());
        request.setCardExpiry(expiryField.getText());
        try {
            request.validateSyntax();
            LessonBean lesson = bookingController.confirmBooking(request);
            navigator().info("Lesson booked",
                    lesson + "\nMeeting link: "
                            + (lesson.getMeetingLink().isEmpty() ? "will be issued before the lesson"
                                                                 : lesson.getMeetingLink()));
            onShowSlots();
            refreshLessons();
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private Parent lessonsPane() {
        Button joinButton = new Button("Join the lesson");
        joinButton.setOnAction(event -> onJoin());
        Button cancelButton = new Button("Cancel the lesson");
        cancelButton.setOnAction(event -> onCancel());
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> refreshLessons());

        VBox layout = new VBox(12, new Label("Your lessons"), lessonList,
                new HBox(12, joinButton, cancelButton, refreshButton));
        layout.setPadding(new Insets(16));
        return layout;
    }

    private void refreshLessons() {
        try {
            lessons.clear();
            lessons.addAll(executionController.myUpcomingLessons());
            lessonList.getItems().setAll(lessons.stream().map(LessonBean::toString).toList());
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private void onJoin() {
        selectedLesson().ifPresent(lesson -> {
            try {
                String link = executionController.join(lesson.getId());
                navigator().info("The lesson is starting", "Open this link in your browser:\n" + link);
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
                navigator().info("Lesson cancelled", "The slot is available again for other students.");
                refreshLessons();
            } catch (TutorLinkException e) {
                report(e);
            }
        });
    }

    private java.util.Optional<LessonBean> selectedLesson() {
        int index = lessonList.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= lessons.size()) {
            navigator().info("Select a lesson", "Pick one lesson from the list first.");
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(lessons.get(index));
    }

    private Parent materialPane() {
        Button listButton = new Button("Show shared material");
        listButton.setOnAction(event -> onListMaterial());
        Button downloadButton = new Button("Download the selected file");
        downloadButton.setOnAction(event -> onDownload());

        VBox layout = new VBox(12, new Label("Select a lesson in the 'My lessons' tab, then:"),
                listButton, materialList, downloadButton);
        layout.setPadding(new Insets(16));
        return layout;
    }

    private void onListMaterial() {
        selectedLesson().ifPresent(lesson -> {
            try {
                materials.clear();
                materials.addAll(materialController.publishedMaterialsForStudent(lesson.getId()));
                materialList.getItems().setAll(materials.stream().map(MaterialBean::toString).toList());
                if (materials.isEmpty()) {
                    navigator().info("Nothing shared yet",
                            "The tutor has not published any material for this lesson.");
                }
            } catch (TutorLinkException e) {
                report(e);
            }
        });
    }

    private void onDownload() {
        int index = materialList.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= materials.size()) {
            navigator().info("Select a file", "Pick one file from the list first.");
            return;
        }
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Where do you want to save the file?");
        java.io.File directory = chooser.showDialog(null);
        if (directory == null) {
            return;
        }
        MaterialBean material = materials.get(index);
        try {
            Path saved = materialController.download(material.getLessonId(), material.getId(),
                    directory.toPath());
            navigator().info("File saved", saved.toString());
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private Parent progressPane() {
        progressChart.setTitle("Score of every completed lesson");
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> refreshProgress());

        VBox layout = new VBox(12, refreshButton, progressSummary, progressChart);
        layout.setPadding(new Insets(16));
        refreshProgress();
        return layout;
    }

    private void refreshProgress() {
        try {
            ProgressSummaryBean summary = progressController.myProgress();
            progressSummary.setText(describe(summary));
            progressChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("score");
            for (ProgressPointBean point : summary.getPoints()) {
                series.getData().add(new XYChart.Data<>(point.getDate(), point.getScore()));
            }
            progressChart.getData().add(series);
        } catch (TutorLinkException e) {
            report(e);
        }
    }

    private static String describe(ProgressSummaryBean summary) {
        if (summary.isEmpty()) {
            return "No completed lesson yet: the chart will fill up after your first evaluation.";
        }
        return "Average " + summary.getAverage()
                + "   |   trend: " + summary.getTrend()
                + "   |   variation: " + summary.getImprovement()
                + "\nBest subject: " + summary.getBestSubject()
                + "   |   to be improved: " + summary.getWeakestSubject()
                + "   |   your best time band: " + summary.getSuggestedTimeBand();
    }

    private Parent inboxPane() {
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(event -> refreshInbox());
        Button seenButton = new Button("Mark as read");
        seenButton.setOnAction(event -> onMarkSeen());

        VBox layout = new VBox(12, new Label("Notifications"), inboxList,
                new HBox(12, refreshButton, seenButton));
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

    private void onMarkSeen() {
        int index = inboxList.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= notifications.size()) {
            return;
        }
        try {
            notificationController.markSeen(notifications.get(index).getId());
            refreshInbox();
        } catch (TutorLinkException e) {
            report(e);
        }
    }
}
