package it.uniroma2.tutorlink.observer;

import it.uniroma2.tutorlink.model.Availability;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

// Avvisa gli osservatori quando un tutor pubblica uno slot.
public final class AvailabilityPublisher implements AvailabilitySubject {
    private static final Logger LOGGER = Logger.getLogger(AvailabilityPublisher.class.getName());

    private final List<AvailabilityObserver> observers = new ArrayList<>();

    private AvailabilityPublisher() {
        // costruttore privato: la classe non va istanziata
    }

    private static final class Holder {
        private static final AvailabilityPublisher INSTANCE = new AvailabilityPublisher();

        private Holder() {
            // costruttore privato: la classe non va istanziata
        }
    }

    public static AvailabilityPublisher getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void attach(AvailabilityObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void detach(AvailabilityObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyPublished(Availability availability) {
        // ciclo su una copia: un observer potrebbe registrarne un altro
        for (AvailabilityObserver observer : new ArrayList<>(observers)) {
            try {
                observer.onAvailabilityPublished(availability);
            } catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, e,
                        () -> "the observer " + observer.getClass().getSimpleName()
                                + " failed, publication is not affected");
            }
        }
    }

    public void detachAll() {
        observers.clear();
    }


}
