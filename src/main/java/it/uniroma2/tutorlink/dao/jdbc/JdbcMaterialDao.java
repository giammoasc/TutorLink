package it.uniroma2.tutorlink.dao.jdbc;

import it.uniroma2.tutorlink.dao.LessonDao;
import it.uniroma2.tutorlink.dao.MaterialDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.exception.UnsupportedMaterialFormatException;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Material;
import it.uniroma2.tutorlink.model.MaterialStatus;
import it.uniroma2.tutorlink.util.IdGenerator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

// Materiale su MySQL, con il contenuto in un BLOB.
public class JdbcMaterialDao implements MaterialDao {
    private static final String UPSERT =
            "INSERT INTO material (id, lesson_id, title, file_name, size_bytes, status, published_at) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE status = VALUES(status), "
                    + "published_at = VALUES(published_at), title = VALUES(title)";
    private static final String SELECT_BY_LESSON =
            "SELECT id, lesson_id, title, file_name, size_bytes, status, published_at FROM material "
                    + "WHERE lesson_id = ? ORDER BY id";
    private static final String UPDATE_CONTENT = "UPDATE material SET content = ? WHERE id = ?";
    private static final String SELECT_CONTENT = "SELECT content FROM material WHERE id = ?";

    private final ConnectionFactory connections;
    private final LessonDao lessonDao;

    public JdbcMaterialDao(ConnectionFactory connections, LessonDao lessonDao) {
        this.connections = connections;
        this.lessonDao = lessonDao;
    }

    @Override
    public void save(Material material) throws PersistenceException {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(UPSERT)) {
            statement.setLong(1, material.id());
            statement.setLong(2, material.lesson().id());
            statement.setString(3, material.title());
            statement.setString(4, material.fileName());
            statement.setLong(5, material.sizeBytes());
            statement.setString(6, material.status().name());
            statement.setTimestamp(7, material.publishedAt() == null
                    ? null : Timestamp.valueOf(material.publishedAt()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("the material " + material.id() + " cannot be stored", e);
        }
    }

    @Override
    public void updateAll(List<Material> materials) throws PersistenceException {
        for (Material material : materials) {
            save(material);
        }
    }

    @Override
    public List<Material> findByLesson(Lesson lesson) throws PersistenceException {
        List<Material> materials = new ArrayList<>();
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_LESSON)) {
            statement.setLong(1, lesson.id());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    materials.add(mapRow(resultSet, lesson));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("the material of the lesson " + lesson.id() + " cannot be read", e);
        }
        return materials;
    }

    @Override
    public void storeContent(long materialId, byte[] content) throws PersistenceException {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(UPDATE_CONTENT)) {
            statement.setBytes(1, content);
            statement.setLong(2, materialId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("the content of the material " + materialId + " cannot be written", e);
        }
    }

    @Override
    public byte[] loadContent(long materialId) throws PersistenceException {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_CONTENT)) {
            statement.setLong(1, materialId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    byte[] content = resultSet.getBytes("content");
                    if (content != null) {
                        return content;
                    }
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("the content of the material " + materialId + " cannot be read", e);
        }
        throw new PersistenceException("no content stored for the material " + materialId);
    }

    private Material mapRow(ResultSet resultSet, Lesson known) throws SQLException, PersistenceException {
        long id = resultSet.getLong("id");
        IdGenerator.observe(id);
        long lessonId = resultSet.getLong("lesson_id");
        Lesson lesson = known != null && known.id() == lessonId
                ? known
                : lessonDao.findById(lessonId).orElseThrow(
                        () -> new PersistenceException("the lesson " + lessonId + " does not exist any more"));
        Timestamp publishedAt = resultSet.getTimestamp("published_at");
        try {
            Material material = new Material(id, lesson, resultSet.getString("title"),
                    resultSet.getString("file_name"), resultSet.getLong("size_bytes"),
                    MaterialStatus.valueOf(resultSet.getString("status")),
                    publishedAt == null ? null : publishedAt.toLocalDateTime());
            lesson.reattach(material);
            return material;
        } catch (UnsupportedMaterialFormatException e) {
            throw new PersistenceException("the stored material " + id + " is not readable any more", e);
        }
    }
}
