package it.uniroma2.tutorlink.observer;

import it.uniroma2.tutorlink.model.Availability;

public interface AvailabilityObserver {
    void onAvailabilityPublished(Availability availability);
}
