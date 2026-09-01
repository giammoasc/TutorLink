package it.uniroma2.tutorlink.model.state;

import it.uniroma2.tutorlink.exception.IllegalLessonStateException;
import it.uniroma2.tutorlink.model.Feedback;
import it.uniroma2.tutorlink.model.Lesson;
import java.time.LocalDateTime;

// Stato della lezione. Ogni operazione restituisce lo stato successivo.
public interface LessonState {
    String name();

    LessonState confirmPayment(Lesson lesson) throws IllegalLessonStateException;

    LessonState start(Lesson lesson, LocalDateTime now) throws IllegalLessonStateException;

    LessonState complete(Lesson lesson, Feedback feedback) throws IllegalLessonStateException;

    LessonState cancel(Lesson lesson, String reason, LocalDateTime now) throws IllegalLessonStateException;

    boolean allowsMaterial();

    boolean isTerminal();
}
