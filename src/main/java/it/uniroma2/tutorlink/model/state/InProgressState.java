package it.uniroma2.tutorlink.model.state;

import it.uniroma2.tutorlink.exception.IllegalLessonStateException;
import it.uniroma2.tutorlink.model.Feedback;
import it.uniroma2.tutorlink.model.Lesson;
import java.time.LocalDateTime;

public final class InProgressState extends AbstractLessonState {
    public static final String NAME = "IN_PROGRESS";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public LessonState complete(Lesson lesson, Feedback feedback) throws IllegalLessonStateException {
        if (feedback == null) {
            throw new IllegalLessonStateException("a lesson cannot be closed without the feedback of the tutor");
        }
        return new CompletedState();
    }

    @Override
    public LessonState cancel(Lesson lesson, String reason, LocalDateTime now) {
        return new CancelledState(reason);
    }

    @Override
    public boolean allowsMaterial() {
        return true;
    }
}
