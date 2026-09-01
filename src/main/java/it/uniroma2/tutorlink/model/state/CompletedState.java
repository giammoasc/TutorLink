package it.uniroma2.tutorlink.model.state;

public final class CompletedState extends AbstractLessonState {
    public static final String NAME = "COMPLETED";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public boolean allowsMaterial() {
        return true;
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}
