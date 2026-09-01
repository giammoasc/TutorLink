package it.uniroma2.tutorlink.dao.cache;

import it.uniroma2.tutorlink.dao.AvailabilityDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.model.Tutor;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CachedAvailabilityDao implements AvailabilityDao, Invalidatable {
    private final AvailabilityDao delegate;
    private final Map<Long, Availability> cache = new HashMap<>();

    public CachedAvailabilityDao(AvailabilityDao delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(Availability availability) throws PersistenceException {
        delegate.save(availability);
        cache.put(availability.id(), availability);
    }

    @Override
    public void update(Availability availability) throws PersistenceException {
        delegate.update(availability);
        cache.put(availability.id(), availability);
    }

    @Override
    public Optional<Availability> findById(long id) throws PersistenceException {
        Availability cached = cache.get(id);
        if (cached != null) {
            return Optional.of(cached);
        }
        return delegate.findById(id).map(this::canonical);
    }

    @Override
    public List<Availability> findByTutor(Tutor tutor) throws PersistenceException {
        return delegate.findByTutor(tutor).stream().map(this::canonical).toList();
    }

    private Availability canonical(Availability availability) {
        return cache.computeIfAbsent(availability.id(), missing -> availability);
    }

    @Override
    public void invalidate() {
        cache.clear();
    }

    @Override
    public String cacheName() {
        return "availabilities";
    }

    @Override
    public int cachedEntries() {
        return cache.size();
    }
}
