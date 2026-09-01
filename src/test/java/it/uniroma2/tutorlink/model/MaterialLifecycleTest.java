package it.uniroma2.tutorlink.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.uniroma2.tutorlink.exception.IllegalLessonStateException;
import it.uniroma2.tutorlink.exception.MaterialQuotaExceededException;
import it.uniroma2.tutorlink.exception.SlotUnavailableException;
import it.uniroma2.tutorlink.exception.UnsupportedMaterialFormatException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Test sulle regole del materiale condiviso.
// In carico a: Ascenzi Gianmarco
class MaterialLifecycleTest {
    private static final long QUOTA = 1_000_000L;
    private static final LocalDateTime START =
            LocalDateTime.now().plusDays(2).withHour(15).withMinute(0).withSecond(0).withNano(0);

    private Lesson confirmedLesson() throws SlotUnavailableException, IllegalLessonStateException {
        Student student = new Student("m.rossi@students.uniroma2.eu", "Mario Rossi", "salt:hash");
        Tutor tutor = new Tutor("l.verdi@uniroma2.eu", "Luca Verdi", "salt:hash", Money.of(30));
        tutor.teach(Subject.COMPUTER_SCIENCE);
        Availability availability = new Availability(5L, tutor, new TimeSlot(START, 60));
        Lesson lesson = Lesson.schedule(50L, student, availability, Subject.COMPUTER_SCIENCE);
        lesson.confirmPayment();
        return lesson;
    }

    @Test
    @DisplayName("a file with an unsupported extension is refused by the material itself")
    void unsupportedFormatIsRefused() throws Exception {
        Lesson lesson = confirmedLesson();
        UnsupportedMaterialFormatException thrown = assertThrows(UnsupportedMaterialFormatException.class,
                () -> new Material(1L, lesson, "malware", "payload.exe", 10L));
        assertTrue(thrown.getAcceptedFormats().contains("pdf"));
        assertEquals(0, lesson.materials().size());
    }

    @Test
    @DisplayName("attaching more than the quota is refused and the residual quota is reported")
    void quotaIsEnforced() throws Exception {
        Lesson lesson = confirmedLesson();
        lesson.attach(new Material(1L, lesson, "slides", "slides.pdf", 900_000L), QUOTA);

        MaterialQuotaExceededException thrown = assertThrows(MaterialQuotaExceededException.class,
                () -> lesson.attach(new Material(2L, lesson, "video", "recording.zip", 200_000L), QUOTA));

        assertEquals(100_000L, thrown.getResidualBytes());
        assertEquals(1, lesson.materials().size());
    }

    @Test
    @DisplayName("material cannot be attached before the lesson is paid")
    void materialNeedsAConfirmedLesson() throws Exception {
        Student student = new Student("m.rossi@students.uniroma2.eu", "Mario Rossi", "salt:hash");
        Tutor tutor = new Tutor("l.verdi@uniroma2.eu", "Luca Verdi", "salt:hash", Money.of(30));
        Availability availability = new Availability(6L, tutor, new TimeSlot(START, 60));
        Lesson pending = Lesson.schedule(60L, student, availability, Subject.COMPUTER_SCIENCE);

        Material material = new Material(3L, pending, "notes", "notes.md", 100L);
        assertThrows(IllegalLessonStateException.class, () -> pending.attach(material, QUOTA));
    }

    @Test
    @DisplayName("publishing turns every draft into a visible file exactly once")
    void publicationIsIdempotent() throws Exception {
        Lesson lesson = confirmedLesson();
        lesson.attach(new Material(1L, lesson, "slides", "slides.pdf", 1_000L), QUOTA);
        lesson.attach(new Material(2L, lesson, "exercises", "exercises.pdf", 2_000L), QUOTA);

        LocalDateTime when = LocalDateTime.now();
        List<Material> published = lesson.publishDraftMaterials(when);

        assertEquals(2, published.size());
        assertEquals(2, lesson.publishedMaterials().size());
        assertTrue(lesson.draftMaterials().isEmpty());
        assertEquals(when, published.get(0).publishedAt());

        assertTrue(lesson.publishDraftMaterials(when.plusMinutes(1)).isEmpty(),
                "a second publication must not touch anything");
    }

    @Test
    @DisplayName("a withdrawn file stops being visible and frees its quota")
    void withdrawnMaterialIsHidden() throws Exception {
        Lesson lesson = confirmedLesson();
        Material material = new Material(1L, lesson, "slides", "slides.pdf", 400_000L);
        lesson.attach(material, QUOTA);
        lesson.publishDraftMaterials(LocalDateTime.now());

        assertEquals(600_000L, lesson.residualQuotaBytes(QUOTA));
        material.withdraw();

        assertFalse(material.isVisibleToStudent());
        assertEquals(QUOTA, lesson.residualQuotaBytes(QUOTA));
    }
}
