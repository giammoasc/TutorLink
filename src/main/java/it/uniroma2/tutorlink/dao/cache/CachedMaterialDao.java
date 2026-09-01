package it.uniroma2.tutorlink.dao.cache;

import it.uniroma2.tutorlink.dao.MaterialDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Material;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

// Cache dei dati del materiale. Il contenuto del file non si tiene in memoria.
public class CachedMaterialDao implements MaterialDao, Invalidatable {
    private final MaterialDao delegate;
    private final Map<Long, Material> cache = new HashMap<>();

    public CachedMaterialDao(MaterialDao delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(Material material) throws PersistenceException {
        delegate.save(material);
        cache.put(material.id(), material);
    }

    @Override
    public void updateAll(List<Material> materials) throws PersistenceException {
        delegate.updateAll(materials);
        materials.forEach(material -> cache.put(material.id(), material));
    }

    @Override
    public List<Material> findByLesson(Lesson lesson) throws PersistenceException {
        return delegate.findByLesson(lesson).stream()
                .map(material -> cache.computeIfAbsent(material.id(), missing -> material))
                .toList();
    }

    @Override
    public void storeContent(long materialId, byte[] content) throws PersistenceException {
        delegate.storeContent(materialId, content);
    }

    @Override
    public byte[] loadContent(long materialId) throws PersistenceException {
        return delegate.loadContent(materialId);
    }


    @Override
    public void invalidate() {
        cache.clear();
    }

    @Override
    public String cacheName() {
        return "materials";
    }

    @Override
    public int cachedEntries() {
        return cache.size();
    }
}
