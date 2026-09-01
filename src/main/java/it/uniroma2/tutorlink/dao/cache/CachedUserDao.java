package it.uniroma2.tutorlink.dao.cache;

import it.uniroma2.tutorlink.dao.UserDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.model.User;
import java.util.List;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Cache degli account: due ricerche restituiscono lo stesso oggetto.
public class CachedUserDao implements UserDao, Invalidatable {
    private final UserDao delegate;
    private final Map<String, User> cache = new HashMap<>();

    public CachedUserDao(UserDao delegate) {
        this.delegate = delegate;
    }

    @Override
    public Optional<User> findByEmail(String email) throws PersistenceException {
        String key = key(email);
        User cached = cache.get(key);
        if (cached != null) {
            return Optional.of(cached);
        }
        Optional<User> loaded = delegate.findByEmail(email);
        return loaded.map(this::canonical);
    }

    @Override
    public boolean exists(String email) throws PersistenceException {
        return cache.containsKey(key(email)) || delegate.exists(email);
    }

    @Override
    public void save(User user) throws PersistenceException {
        delegate.save(user);
        cache.put(key(user.email()), user);
    }

    @Override
    public List<Tutor> findTutorsBySubject(Subject subject) throws PersistenceException {
        return delegate.findTutorsBySubject(subject).stream()
                .map(this::canonical)
                .filter(Tutor.class::isInstance)
                .map(Tutor.class::cast)
                .toList();
    }

    @Override
    public List<Student> findAllStudents() throws PersistenceException {
        return delegate.findAllStudents().stream()
                .map(this::canonical)
                .filter(Student.class::isInstance)
                .map(Student.class::cast)
                .toList();
    }

    private User canonical(User user) {
        return cache.computeIfAbsent(key(user.email()), missing -> user);
    }

    private static String key(String email) {
        return email == null ? "" : email.toLowerCase(Locale.ROOT);
    }

    @Override
    public void invalidate() {
        cache.clear();
    }

    @Override
    public String cacheName() {
        return "users";
    }

    @Override
    public int cachedEntries() {
        return cache.size();
    }
}
