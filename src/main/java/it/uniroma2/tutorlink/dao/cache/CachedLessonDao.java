package it.uniroma2.tutorlink.dao.cache;

import it.uniroma2.tutorlink.dao.LessonDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Tutor;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Cache delle lezioni: serve perche' il materiale finisca sulla lezione giusta.
public class CachedLessonDao implements LessonDao, Invalidatable {
    private final LessonDao delegate;
    private final Map<Long, Lesson> cache = new HashMap<>();

    public CachedLessonDao(LessonDao delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(Lesson lesson) throws PersistenceException {
        delegate.save(lesson);
        cache.put(lesson.id(), lesson);
    }

    @Override
    public void update(Lesson lesson) throws PersistenceException {
        delegate.update(lesson);
        cache.put(lesson.id(), lesson);
    }

    @Override
    public Optional<Lesson> findById(long id) throws PersistenceException {
        Lesson cached = cache.get(id);
        if (cached != null) {
            return Optional.of(cached);
        }
        return delegate.findById(id).map(this::canonical);
    }

    @Override
    public List<Lesson> findByStudent(Student student) throws PersistenceException {
        return delegate.findByStudent(student).stream().map(this::canonical).toList();
    }

    @Override
    public List<Lesson> findByTutor(Tutor tutor) throws PersistenceException {
        return delegate.findByTutor(tutor).stream().map(this::canonical).toList();
    }

    private Lesson canonical(Lesson lesson) {
        return cache.computeIfAbsent(lesson.id(), missing -> lesson);
    }

    @Override
    public void invalidate() {
        cache.clear();
    }

    @Override
    public String cacheName() {
        return "lessons";
    }

    @Override
    public int cachedEntries() {
        return cache.size();
    }
}
