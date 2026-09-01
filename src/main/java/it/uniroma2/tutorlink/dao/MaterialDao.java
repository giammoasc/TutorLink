package it.uniroma2.tutorlink.dao;

import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Material;
import java.util.List;

public interface MaterialDao {
    void save(Material material) throws PersistenceException;

    void updateAll(List<Material> materials) throws PersistenceException;

    List<Material> findByLesson(Lesson lesson) throws PersistenceException;

    void storeContent(long materialId, byte[] content) throws PersistenceException;

    byte[] loadContent(long materialId) throws PersistenceException;
}
