package it.uniroma2.tutorlink.dao.memory;

import it.uniroma2.tutorlink.dao.MaterialDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Material;
import java.util.List;

public class InMemoryMaterialDao implements MaterialDao {
    private final InMemoryStore store;

    public InMemoryMaterialDao(InMemoryStore store) {
        this.store = store;
    }

    @Override
    public void save(Material material) {
        store.materials().put(material.id(), material);
    }

    @Override
    public void updateAll(List<Material> materials) {
        materials.forEach(this::save);
    }

    @Override
    public List<Material> findByLesson(Lesson lesson) {
        return store.materials().values().stream()
                .filter(material -> material.lesson().equals(lesson))
                .toList();
    }

    @Override
    public void storeContent(long materialId, byte[] content) {
        // copia difensiva: chi chiama non deve poter modificare cio' che ho salvato
        store.contents().put(materialId, content.clone());
    }

    @Override
    public byte[] loadContent(long materialId) throws PersistenceException {
        byte[] content = store.contents().get(materialId);
        if (content == null) {
            throw new PersistenceException("no content stored for the material " + materialId);
        }
        return content.clone();
    }
}
