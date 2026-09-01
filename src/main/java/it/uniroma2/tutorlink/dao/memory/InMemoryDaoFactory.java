package it.uniroma2.tutorlink.dao.memory;

import it.uniroma2.tutorlink.dao.AvailabilityDao;
import it.uniroma2.tutorlink.dao.DaoFactory;
import it.uniroma2.tutorlink.dao.FeedbackDao;
import it.uniroma2.tutorlink.dao.LessonDao;
import it.uniroma2.tutorlink.dao.MaterialDao;
import it.uniroma2.tutorlink.dao.NotificationDao;
import it.uniroma2.tutorlink.dao.UserDao;

public class InMemoryDaoFactory implements DaoFactory {
    private final InMemoryStore store;

    public InMemoryDaoFactory() {
        this(new InMemoryStore());
    }

    public InMemoryDaoFactory(InMemoryStore store) {
        this.store = store;
    }


    @Override
    public String name() {
        return "in-memory (demo-version)";
    }

    @Override
    public UserDao createUserDao() {
        return new InMemoryUserDao(store);
    }

    @Override
    public AvailabilityDao createAvailabilityDao() {
        return new InMemoryAvailabilityDao(store);
    }

    @Override
    public LessonDao createLessonDao() {
        return new InMemoryLessonDao(store);
    }

    @Override
    public MaterialDao createMaterialDao() {
        return new InMemoryMaterialDao(store);
    }

    @Override
    public FeedbackDao createFeedbackDao() {
        return new InMemoryFeedbackDao(store);
    }

    @Override
    public NotificationDao createNotificationDao() {
        return new InMemoryNotificationDao(store);
    }
}
