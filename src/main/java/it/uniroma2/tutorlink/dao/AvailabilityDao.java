package it.uniroma2.tutorlink.dao;

import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.model.Tutor;
import java.util.List;
import java.util.Optional;

public interface AvailabilityDao {
    void save(Availability availability) throws PersistenceException;

    void update(Availability availability) throws PersistenceException;

    Optional<Availability> findById(long id) throws PersistenceException;

    List<Availability> findByTutor(Tutor tutor) throws PersistenceException;
}
