package it.uniroma2.tutorlink.dao.filesystem;

import it.uniroma2.tutorlink.dao.LessonDao;
import it.uniroma2.tutorlink.dao.MaterialDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.exception.UnsupportedMaterialFormatException;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Material;
import it.uniroma2.tutorlink.model.MaterialStatus;
import it.uniroma2.tutorlink.util.IdGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Materiale: i dati su CSV, il contenuto in file separati.
public class FileSystemMaterialDao extends AbstractCsvDao<Material> implements MaterialDao {
    private static final String FILE_NAME = "materials.csv";
    private static final String CONTENT_DIRECTORY = "materials";

    private final LessonDao lessonDao;
    private final Path contentRoot;

    public FileSystemMaterialDao(Path root, LessonDao lessonDao) throws PersistenceException {
        super(root, FILE_NAME);
        this.lessonDao = lessonDao;
        this.contentRoot = root.resolve(CONTENT_DIRECTORY);
        try {
            Files.createDirectories(contentRoot);
        } catch (IOException e) {
            throw new PersistenceException("the directory " + contentRoot + " cannot be created", e);
        }
    }

    @Override
    protected String[] toRecord(Material material) {
        return new String[]{
                Long.toString(material.id()),
                Long.toString(material.lesson().id()),
                material.title(),
                material.fileName(),
                Long.toString(material.sizeBytes()),
                material.status().name(),
                material.publishedAt() == null ? "" : material.publishedAt().toString()};
    }

    @Override
    protected Material fromRecord(String[] record) throws PersistenceException {
        if (record.length < 6) {
            throw new PersistenceException("malformed record in " + file());
        }
        long id = Long.parseLong(record[0]);
        IdGenerator.observe(id);
        long lessonId = Long.parseLong(record[1]);
        Lesson lesson = lessonDao.findById(lessonId)
                .orElseThrow(() -> new PersistenceException("the lesson " + lessonId + " does not exist any more"));
        LocalDateTime publishedAt = record.length > 6 && !record[6].isBlank()
                ? LocalDateTime.parse(record[6])
                : null;
        try {
            Material material = new Material(id, lesson, record[2], record[3],
                    Long.parseLong(record[4]), MaterialStatus.valueOf(record[5]), publishedAt);
            lesson.reattach(material);
            return material;
        } catch (UnsupportedMaterialFormatException e) {
            throw new PersistenceException("the stored material " + id + " is not readable any more", e);
        }
    }

    @Override
    protected String keyOf(String[] record) {
        return record[0];
    }

    @Override
    public void save(Material material) throws PersistenceException {
        upsert(material);
    }

    @Override
    public void updateAll(List<Material> materials) throws PersistenceException {
        for (Material material : materials) {
            upsert(material);
        }
    }

    @Override
    public List<Material> findByLesson(Lesson lesson) throws PersistenceException {
        String lessonId = Long.toString(lesson.id());
        List<Material> materials = new ArrayList<>();
        for (String[] record : readRecords()) {
            if (record[1].equals(lessonId)) {
                materials.add(fromRecord(record));
            }
        }
        return materials;
    }

    @Override
    public void storeContent(long materialId, byte[] content) throws PersistenceException {
        try {
            Files.write(contentRoot.resolve(materialId + ".bin"), content);
        } catch (IOException e) {
            throw new PersistenceException("the content of the material " + materialId + " cannot be written", e);
        }
    }

    @Override
    public byte[] loadContent(long materialId) throws PersistenceException {
        try {
            return Files.readAllBytes(contentRoot.resolve(materialId + ".bin"));
        } catch (IOException e) {
            throw new PersistenceException("the content of the material " + materialId + " cannot be read", e);
        }
    }
}
