package it.uniroma2.tutorlink.model.state;

public final class CancelledState extends AbstractLessonState {
    public static final String NAME = "CANCELLED";

    private final String reason;

    public CancelledState() {
        this("unspecified");
    }

    public CancelledState(String reason) {
        this.reason = reason == null || reason.isBlank() ? "unspecified" : reason.trim();
    }

    @Override
    public String name() {
        return NAME;
    }

    public String reason() {
        return reason;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}
