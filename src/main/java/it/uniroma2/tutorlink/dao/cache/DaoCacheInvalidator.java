package it.uniroma2.tutorlink.dao.cache;

import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.observer.AvailabilityObserver;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DaoCacheInvalidator implements AvailabilityObserver {
    private static final Logger LOGGER = Logger.getLogger(DaoCacheInvalidator.class.getName());

    private final List<Invalidatable> caches;

    public DaoCacheInvalidator(List<Invalidatable> caches) {
        this.caches = List.copyOf(caches);
    }

    @Override
    public void onAvailabilityPublished(Availability availability) {
        for (Invalidatable cache : caches) {
            int entries = cache.cachedEntries();
            cache.invalidate();
            LOGGER.log(Level.FINE, "cache {0} invalidated ({1} entries dropped)",
                    new Object[]{cache.cacheName(), entries});
        }
    }
}
