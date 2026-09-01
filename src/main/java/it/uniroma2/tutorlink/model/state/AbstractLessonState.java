package it.uniroma2.tutorlink.model.state;

import it.uniroma2.tutorlink.exception.IllegalLessonStateException;
import it.uniroma2.tutorlink.model.Feedback;
import it.uniroma2.tutorlink.model.Lesson;
import java.time.LocalDateTime;

// Base degli stati: vieta tutto, i concreti abilitano solo cio' che serve.
abstract class AbstractLessonState implements LessonState {
    @Override
    public LessonState confirmPayment(Lesson lesson) throws IllegalLessonStateException {
        throw reject("confirm the payment of");
    }

    @Override
    public LessonState start(Lesson lesson, LocalDateTime now) throws IllegalLessonStateException {
        throw reject("start");
    }

    @Override
    public LessonState complete(Lesson lesson, Feedback feedback) throws IllegalLessonStateException {
        throw reject("complete");
    }

    @Override
    public LessonState cancel(Lesson lesson, String reason, LocalDateTime now) throws IllegalLessonStateException {
        throw reject("cancel");
    }

    @Override
    public boolean allowsMaterial() {
        return false;
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    protected IllegalLessonStateException reject(String operation) {
        return new IllegalLessonStateException(
                "it is not possible to " + operation + " a lesson in state " + name());
    }

    @Override
    public String toString() {
        return name();
    }
}
