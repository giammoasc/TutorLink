package it.uniroma2.tutorlink.dao.memory;

import it.uniroma2.tutorlink.dao.LessonDao;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Tutor;
import java.util.List;
import java.util.Optional;

public class InMemoryLessonDao implements LessonDao {
    private final InMemoryStore store;

    public InMemoryLessonDao(InMemoryStore store) {
        this.store = store;
    }

    @Override
    public void save(Lesson lesson) {
        store.lessons().put(lesson.id(), lesson);
    }

    @Override
    public void update(Lesson lesson) {
        save(lesson);
    }

    @Override
    public Optional<Lesson> findById(long id) {
        return Optional.ofNullable(store.lessons().get(id));
    }

    @Override
    public List<Lesson> findByStudent(Student student) {
        return store.lessons().values().stream()
                .filter(lesson -> lesson.student().equals(student))
                .toList();
    }

    @Override
    public List<Lesson> findByTutor(Tutor tutor) {
        return store.lessons().values().stream()
                .filter(lesson -> lesson.tutor().equals(tutor))
                .toList();
    }
}
