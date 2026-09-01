package it.uniroma2.tutorlink.support;

import it.uniroma2.tutorlink.dao.memory.InMemoryDaoFactory;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Money;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.observer.AvailabilityPublisher;
import it.uniroma2.tutorlink.session.SessionManager;
import it.uniroma2.tutorlink.util.PasswordHasher;

public final class TestFixture {
    public static final char[] PASSWORD = "tutorlink".toCharArray();
    public static final String APPROVED_CARD = "4111111111111111";
    public static final String REFUSED_CARD = "4111111111110000";

    private TestFixture() {
        // costruttore privato: la classe non va istanziata
    }

    public static InMemoryDaoFactory freshDaos() {
        AvailabilityPublisher.getInstance().detachAll();
        SessionManager.getInstance().close();
        return new InMemoryDaoFactory();
    }

    public static Student student(InMemoryDaoFactory daos, String email, String name)
            throws PersistenceException {
        Student student = new Student(email, name, PasswordHasher.hash(PASSWORD.clone()));
        daos.createUserDao().save(student);
        return student;
    }

    public static Tutor tutor(InMemoryDaoFactory daos, String email, String name,
                              double hourlyRate, Subject... subjects) throws PersistenceException {
        Tutor tutor = new Tutor(email, name, PasswordHasher.hash(PASSWORD.clone()), Money.of(hourlyRate));
        for (Subject subject : subjects) {
            tutor.teach(subject);
        }
        daos.createUserDao().save(tutor);
        return tutor;
    }
}
