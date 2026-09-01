package it.uniroma2.tutorlink.observer;

import it.uniroma2.tutorlink.model.Availability;

public interface AvailabilitySubject {
    void attach(AvailabilityObserver observer);

    void detach(AvailabilityObserver observer);

    void notifyPublished(Availability availability);
}
