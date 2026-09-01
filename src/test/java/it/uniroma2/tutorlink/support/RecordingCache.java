package it.uniroma2.tutorlink.support;

import it.uniroma2.tutorlink.dao.cache.Invalidatable;

// Finta cache: conta solo quante volte viene svuotata.
public class RecordingCache implements Invalidatable {
    private int invalidations;

    @Override
    public void invalidate() {
        invalidations++;
    }

    @Override
    public String cacheName() {
        return "recording";
    }

    @Override
    public int cachedEntries() {
        return 0;
    }

    public int invalidations() {
        return invalidations;
    }
}
