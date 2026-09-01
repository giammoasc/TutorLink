package it.uniroma2.tutorlink.dao.filesystem;

import it.uniroma2.tutorlink.dao.FeedbackDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Feedback;
import it.uniroma2.tutorlink.model.Lesson;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Optional;

public class FileSystemFeedbackDao extends AbstractCsvDao<Feedback> implements FeedbackDao {
    private static final String FILE_NAME = "feedbacks.csv";

    public FileSystemFeedbackDao(Path root) throws PersistenceException {
        super(root, FILE_NAME);
    }

    @Override
    protected String[] toRecord(Feedback feedback) {
        return new String[]{
                Long.toString(feedback.lesson().id()),
                Integer.toString(feedback.score()),
                feedback.comment(),
                feedback.createdAt().toString()};
    }

    @Override
    // un feedback non si ricostruisce da solo: serve la lezione, vedi findByLesson
    protected Feedback fromRecord(String[] record) throws PersistenceException {
        throw new PersistenceException("a feedback can only be rebuilt together with its lesson");
    }

    @Override
    protected String keyOf(String[] record) {
        return record[0];
    }

    @Override
    public void save(Feedback feedback) throws PersistenceException {
        upsert(feedback);
    }

    @Override
    public Optional<Feedback> findByLesson(Lesson lesson) throws PersistenceException {
        String key = Long.toString(lesson.id());
        for (String[] record : readRecords()) {
            if (record.length >= 4 && record[0].equals(key)) {
                return Optional.of(new Feedback(lesson, Integer.parseInt(record[1]),
                        record[2], LocalDateTime.parse(record[3])));
            }
        }
        return Optional.empty();
    }
}
