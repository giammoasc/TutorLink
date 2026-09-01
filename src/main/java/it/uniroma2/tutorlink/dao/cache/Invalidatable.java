package it.uniroma2.tutorlink.dao.cache;

public interface Invalidatable {
    void invalidate();

    String cacheName();

    int cachedEntries();
}
