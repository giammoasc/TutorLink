package it.uniroma2.tutorlink.model.state;

import it.uniroma2.tutorlink.model.Lesson;
import java.time.LocalDateTime;

public final class PendingPaymentState extends AbstractLessonState {
    public static final String NAME = "PENDING_PAYMENT";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public LessonState confirmPayment(Lesson lesson) {
        return new ConfirmedState();
    }

    @Override
    public LessonState cancel(Lesson lesson, String reason, LocalDateTime now) {
        // prenotazione mai pagata: lo slot torna subito disponibile
        lesson.releaseReservation();
        return new CancelledState(reason);
    }
}
