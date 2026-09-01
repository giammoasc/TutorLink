package it.uniroma2.tutorlink.dao.memory;

import it.uniroma2.tutorlink.dao.FeedbackDao;
import it.uniroma2.tutorlink.model.Feedback;
import it.uniroma2.tutorlink.model.Lesson;
import java.util.Optional;

public class InMemoryFeedbackDao implements FeedbackDao {
    private final InMemoryStore store;

    public InMemoryFeedbackDao(InMemoryStore store) {
        this.store = store;
    }

    @Override
    public void save(Feedback feedback) {
        store.feedbacks().put(feedback.lesson().id(), feedback);
    }

    @Override
    public Optional<Feedback> findByLesson(Lesson lesson) {
        return Optional.ofNullable(store.feedbacks().get(lesson.id()));
    }
}
