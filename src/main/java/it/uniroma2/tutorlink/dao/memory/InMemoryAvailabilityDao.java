package it.uniroma2.tutorlink.dao.memory;

import it.uniroma2.tutorlink.dao.AvailabilityDao;
import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.model.Tutor;
import java.util.List;
import java.util.Optional;

public class InMemoryAvailabilityDao implements AvailabilityDao {
    private final InMemoryStore store;

    public InMemoryAvailabilityDao(InMemoryStore store) {
        this.store = store;
    }

    @Override
    public void save(Availability availability) {
        store.availabilities().put(availability.id(), availability);
    }

    @Override
    public void update(Availability availability) {
        save(availability);
    }

    @Override
    public Optional<Availability> findById(long id) {
        return Optional.ofNullable(store.availabilities().get(id));
    }

    @Override
    public List<Availability> findByTutor(Tutor tutor) {
        return store.availabilities().values().stream()
                .filter(availability -> availability.tutor().equals(tutor))
                .toList();
    }
}
