package it.uniroma2.tutorlink.dao;

import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Tutor;
import java.util.List;
import java.util.Optional;

public interface LessonDao {
    void save(Lesson lesson) throws PersistenceException;

    void update(Lesson lesson) throws PersistenceException;

    Optional<Lesson> findById(long id) throws PersistenceException;

    List<Lesson> findByStudent(Student student) throws PersistenceException;

    List<Lesson> findByTutor(Tutor tutor) throws PersistenceException;
}
