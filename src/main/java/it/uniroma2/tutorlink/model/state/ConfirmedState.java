package it.uniroma2.tutorlink.model.state;

import it.uniroma2.tutorlink.exception.IllegalLessonStateException;
import it.uniroma2.tutorlink.model.Lesson;
import java.time.LocalDateTime;

// Lezione pagata: si puo' allegare materiale e avviarla poco prima dell'orario.
public final class ConfirmedState extends AbstractLessonState {
    public static final String NAME = "CONFIRMED";

    private static final long FREE_CANCELLATION_HOURS = 24;

    private static final long JOIN_TOLERANCE_MINUTES = 10;

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public LessonState start(Lesson lesson, LocalDateTime now) throws IllegalLessonStateException {
        LocalDateTime earliest = lesson.slot().start().minusMinutes(JOIN_TOLERANCE_MINUTES);
        if (now.isBefore(earliest)) {
            throw new IllegalLessonStateException(
                    "the lesson can be started at the earliest " + JOIN_TOLERANCE_MINUTES
                            + " minutes before " + lesson.slot().start());
        }
        if (lesson.slot().isInThePast(now)) {
            throw new IllegalLessonStateException("the time slot of the lesson is already over");
        }
        return new InProgressState();
    }

    @Override
    public LessonState cancel(Lesson lesson, String reason, LocalDateTime now) throws IllegalLessonStateException {
        if (lesson.slot().hoursBefore(now) < FREE_CANCELLATION_HOURS) {
            throw new IllegalLessonStateException(
                    "a confirmed lesson can be cancelled up to " + FREE_CANCELLATION_HOURS
                            + " hours before it starts");
        }
        lesson.releaseReservation();
        return new CancelledState(reason);
    }

    @Override
    public boolean allowsMaterial() {
        return true;
    }
}
