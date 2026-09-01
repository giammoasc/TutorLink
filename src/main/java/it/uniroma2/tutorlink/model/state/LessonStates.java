package it.uniroma2.tutorlink.model.state;

// Ricostruisce lo stato dal nome salvato su file o su database.
public final class LessonStates {
    private LessonStates() {
        // costruttore privato: la classe non va istanziata
    }

    public static LessonState initial() {
        return new PendingPaymentState();
    }

    public static LessonState of(String name) {
        if (name == null) {
            return initial();
        }
        return switch (name.trim().toUpperCase(java.util.Locale.ROOT)) {
            case ConfirmedState.NAME -> new ConfirmedState();
            case InProgressState.NAME -> new InProgressState();
            case CompletedState.NAME -> new CompletedState();
            case CancelledState.NAME -> new CancelledState();
            case PendingPaymentState.NAME -> new PendingPaymentState();
            default -> throw new IllegalArgumentException("unknown lesson state: " + name);
        };
    }
}
