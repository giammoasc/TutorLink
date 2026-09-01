package it.uniroma2.tutorlink.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.uniroma2.tutorlink.dao.filesystem.FileSystemDaoFactory;
import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.model.Feedback;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Material;
import it.uniroma2.tutorlink.model.Money;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.TimeSlot;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.model.User;
import it.uniroma2.tutorlink.model.state.LessonStates;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Test di scrittura e rilettura su file.
// In carico a: Cicerchia Nicolas
class FileSystemDaoRoundTripTest {
    private static final LocalDateTime START =
            LocalDateTime.of(2026, 5, 12, 10, 0);

    private Path root;

    @BeforeEach
    void setUp() throws IOException {
        root = java.nio.file.Files.createTempDirectory("tutorlink-test");
    }

    private void writeDataSet() throws Exception {
        FileSystemDaoFactory factory = new FileSystemDaoFactory(root);
        Student student = new Student("m.rossi@students.uniroma2.eu", "Mario Rossi", "salt:hash");
        Tutor tutor = new Tutor("g.bianchi@uniroma2.eu", "Giulia Bianchi", "salt:hash", Money.of(28));
        tutor.teach(Subject.MATHEMATICS);
        tutor.teach(Subject.PHYSICS);
        factory.createUserDao().save(student);
        factory.createUserDao().save(tutor);

        Availability availability = tutor.publishAvailability(11L, new TimeSlot(START, 60));
        availability.reserve();
        factory.createAvailabilityDao().save(availability);

        Lesson lesson = new Lesson(21L, student, tutor, Subject.MATHEMATICS,
                new TimeSlot(START, 60), Money.of(28), availability,
                LessonStates.of("COMPLETED"), "https://meet.google.com/abc-defg-hij");
        factory.createLessonDao().save(lesson);
        factory.createFeedbackDao().save(new Feedback(lesson, 7, "good; work on the limits",
                START.plusHours(1)));

        Material material = new Material(31L, lesson, "Slides", "limits.pdf", 4L);
        material.publish(START.plusHours(2));
        lesson.reattach(material);
        factory.createMaterialDao().save(material);
        factory.createMaterialDao().storeContent(31L, new byte[]{1, 2, 3, 4});
    }

    @Test
    @DisplayName("accounts, calendar, lessons and feedback survive a restart")
    void dataSetIsRebuilt() throws Exception {
        writeDataSet();
        FileSystemDaoFactory reloaded = new FileSystemDaoFactory(root);

        User tutor = reloaded.createUserDao().findByEmail("g.bianchi@uniroma2.eu").orElseThrow();
        assertTrue(tutor instanceof Tutor);
        assertEquals(2, ((Tutor) tutor).subjects().size());
        assertTrue(((Tutor) tutor).teaches(Subject.PHYSICS));

        List<Availability> calendar = reloaded.createAvailabilityDao().findByTutor((Tutor) tutor);
        assertEquals(1, calendar.size());
        assertTrue(calendar.get(0).isReserved());

        Student student = (Student) reloaded.createUserDao()
                .findByEmail("m.rossi@students.uniroma2.eu").orElseThrow();
        List<Lesson> lessons = reloaded.createLessonDao().findByStudent(student);
        assertEquals(1, lessons.size());
        Lesson lesson = lessons.get(0);
        assertEquals("COMPLETED", lesson.stateName());
        assertEquals("https://meet.google.com/abc-defg-hij", lesson.meetingLink().orElseThrow());
        assertEquals(7, lesson.feedback().orElseThrow().score());
        assertEquals(1, student.progressReport().size(), "the chart of US-3 is fed by the stored feedback");
    }

    @Test
    @DisplayName("the cache decorator guarantees the identity of the reloaded objects")
    void identityIsPreserved() throws Exception {
        writeDataSet();
        FileSystemDaoFactory reloaded = new FileSystemDaoFactory(root);

        User first = reloaded.createUserDao().findByEmail("g.bianchi@uniroma2.eu").orElseThrow();
        User second = reloaded.createUserDao().findByEmail("G.Bianchi@uniroma2.eu").orElseThrow();
        assertSame(first, second, "two lookups must return the very same object, not two copies");

        Student student = (Student) reloaded.createUserDao()
                .findByEmail("m.rossi@students.uniroma2.eu").orElseThrow();
        Lesson lesson = reloaded.createLessonDao().findByStudent(student).get(0);
        assertSame(first, lesson.tutor(), "the association is a reference, not a foreign key");
    }

    @Test
    @DisplayName("the material descriptor and its bytes are stored separately and read back")
    void materialRoundTrip() throws Exception {
        writeDataSet();
        FileSystemDaoFactory reloaded = new FileSystemDaoFactory(root);

        Student student = (Student) reloaded.createUserDao()
                .findByEmail("m.rossi@students.uniroma2.eu").orElseThrow();
        Lesson lesson = reloaded.createLessonDao().findByStudent(student).get(0);

        List<Material> materials = reloaded.createMaterialDao().findByLesson(lesson);
        assertEquals(1, materials.size());
        assertTrue(materials.get(0).isPublished());
        assertEquals("limits.pdf", materials.get(0).fileName());
        assertFalse(lesson.publishedMaterials().isEmpty());

        byte[] content = reloaded.createMaterialDao().loadContent(31L);
        assertEquals(4, content.length);
        assertEquals(3, content[2]);
    }
}
