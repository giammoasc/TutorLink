package it.uniroma2.tutorlink.dao.memory;

import it.uniroma2.tutorlink.dao.UserDao;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.model.User;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class InMemoryUserDao implements UserDao {
    private final InMemoryStore store;

    public InMemoryUserDao(InMemoryStore store) {
        this.store = store;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(store.users().get(key(email)));
    }

    @Override
    public boolean exists(String email) {
        return store.users().containsKey(key(email));
    }

    @Override
    public void save(User user) {
        store.users().put(key(user.email()), user);
    }

    @Override
    public List<Tutor> findTutorsBySubject(Subject subject) {
        return store.users().values().stream()
                .filter(Tutor.class::isInstance)
                .map(Tutor.class::cast)
                .filter(tutor -> tutor.teaches(subject))
                .toList();
    }

    @Override
    public List<Student> findAllStudents() {
        return store.users().values().stream()
                .filter(Student.class::isInstance)
                .map(Student.class::cast)
                .toList();
    }

    private static String key(String email) {
        return email == null ? "" : email.toLowerCase(Locale.ROOT);
    }
}
