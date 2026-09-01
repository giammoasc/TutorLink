package it.uniroma2.tutorlink.dao;

import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Feedback;
import it.uniroma2.tutorlink.model.Lesson;
import java.util.Optional;

public interface FeedbackDao {
    void save(Feedback feedback) throws PersistenceException;

    Optional<Feedback> findByLesson(Lesson lesson) throws PersistenceException;
}
