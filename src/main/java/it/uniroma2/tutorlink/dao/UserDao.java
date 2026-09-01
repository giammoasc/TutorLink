package it.uniroma2.tutorlink.dao;

import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    Optional<User> findByEmail(String email) throws PersistenceException;

    boolean exists(String email) throws PersistenceException;

    void save(User user) throws PersistenceException;

    List<Tutor> findTutorsBySubject(Subject subject) throws PersistenceException;

    List<Student> findAllStudents() throws PersistenceException;
}
