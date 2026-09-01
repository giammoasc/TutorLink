package it.uniroma2.tutorlink.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.uniroma2.tutorlink.bean.MaterialBean;
import it.uniroma2.tutorlink.dao.memory.InMemoryDaoFactory;
import it.uniroma2.tutorlink.exception.AuthenticationException;
import it.uniroma2.tutorlink.exception.UnsupportedMaterialFormatException;
import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.TimeSlot;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.session.SessionManager;
import it.uniroma2.tutorlink.support.RecordingNotificationSender;
import it.uniroma2.tutorlink.support.TestFixture;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Test sulla condivisione del materiale.
// In carico a: Ascenzi Gianmarco
class LessonMaterialControllerTest {
    private static final long QUOTA = 1_000_000L;

    private InMemoryDaoFactory daos;
    private Student student;
    private Tutor tutor;
    private Lesson lesson;
    private RecordingNotificationSender recorder;
    private LessonMaterialController controller;

    @BeforeEach
    void setUp() throws Exception {
        daos = TestFixture.freshDaos();
        student = TestFixture.student(daos, "m.rossi@students.uniroma2.eu", "Mario Rossi");
        tutor = TestFixture.tutor(daos, "l.verdi@uniroma2.eu", "Luca Verdi", 30,
                Subject.COMPUTER_SCIENCE);

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(11)
                .withMinute(0).withSecond(0).withNano(0);
        Availability availability = tutor.publishAvailability(1L, new TimeSlot(start, 60));
        daos.createAvailabilityDao().save(availability);
        lesson = Lesson.schedule(100L, student, availability, Subject.COMPUTER_SCIENCE);
        lesson.confirmPayment();
        daos.createLessonDao().save(lesson);

        recorder = new RecordingNotificationSender();
        controller = new LessonMaterialController(daos, recorder, QUOTA);
        SessionManager.getInstance().open(tutor);
    }

    private MaterialBean materialBean(String title, String fileName, long size) {
        MaterialBean bean = new MaterialBean();
        bean.setLessonId(lesson.id());
        bean.setTitle(title);
        bean.setFileName(fileName);
        bean.setSizeBytes(size);
        return bean;
    }

    @Test
    @DisplayName("steps 5 and 6: an attached file is a draft and consumes the quota")
    void attachCreatesADraft() throws Exception {
        MaterialBean saved = controller.attach(materialBean("Slides", "lecture.pdf", 250_000L));

        assertEquals("DRAFT", saved.getStatus());
        assertEquals(1, controller.materialsOf(lesson.id()).size());
        assertEquals(750_000L, controller.residualQuota(lesson.id()));
        assertEquals(0, recorder.count(), "a draft must not notify anybody");
    }

    @Test
    @DisplayName("alternative flow 6a: an unsupported format never reaches the data layer")
    void unsupportedFormatIsRejected() {
        assertThrows(UnsupportedMaterialFormatException.class,
                () -> controller.attach(materialBean("Virus", "payload.exe", 10L)));
    }

    @Test
    @DisplayName("steps 7 to 10: publishing makes the files visible and notifies the student")
    void publishNotifiesTheStudent() throws Exception {
        controller.attach(materialBean("Slides", "lecture.pdf", 1_000L));
        controller.attach(materialBean("Exercises", "exercises.pdf", 2_000L));

        List<MaterialBean> published = controller.publish(lesson.id());

        assertEquals(2, published.size());
        assertTrue(published.stream().allMatch(bean -> "PUBLISHED".equals(bean.getStatus())));
        assertEquals(1, recorder.count(), "one notification for the whole publication");
        assertEquals(student, recorder.sent().get(0).recipient());
    }

    @Test
    @DisplayName("the student sees the published files and nothing else")
    void studentSeesOnlyPublishedMaterial() throws Exception {
        controller.attach(materialBean("Slides", "lecture.pdf", 1_000L));
        controller.publish(lesson.id());
        controller.attach(materialBean("Draft notes", "notes.md", 500L));

        SessionManager.getInstance().open(student);
        List<MaterialBean> visible = controller.publishedMaterialsForStudent(lesson.id());

        assertEquals(1, visible.size());
        assertEquals("Slides", visible.get(0).getTitle());
    }

    @Test
    @DisplayName("a student cannot read the material of a lesson that is not his")
    void materialOfSomebodyElseIsRefused() throws Exception {
        controller.attach(materialBean("Slides", "lecture.pdf", 1_000L));
        controller.publish(lesson.id());

        Student intruder = TestFixture.student(daos, "a.neri@students.uniroma2.eu", "Anna Neri");
        SessionManager.getInstance().open(intruder);

        assertThrows(AuthenticationException.class,
                () -> controller.publishedMaterialsForStudent(lesson.id()));
    }

    @Test
    @DisplayName("publishing twice does not send a second notification")
    void publishingTwiceIsSafe() throws Exception {
        controller.attach(materialBean("Slides", "lecture.pdf", 1_000L));
        controller.publish(lesson.id());
        List<MaterialBean> second = controller.publish(lesson.id());

        assertTrue(second.isEmpty());
        assertEquals(1, recorder.count());
    }
}
