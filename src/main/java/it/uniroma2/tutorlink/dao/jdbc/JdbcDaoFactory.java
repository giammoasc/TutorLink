package it.uniroma2.tutorlink.dao.jdbc;

import it.uniroma2.tutorlink.dao.AvailabilityDao;
import it.uniroma2.tutorlink.dao.DaoFactory;
import it.uniroma2.tutorlink.dao.FeedbackDao;
import it.uniroma2.tutorlink.dao.LessonDao;
import it.uniroma2.tutorlink.dao.MaterialDao;
import it.uniroma2.tutorlink.dao.NotificationDao;
import it.uniroma2.tutorlink.dao.UserDao;
import it.uniroma2.tutorlink.dao.cache.CachedAvailabilityDao;
import it.uniroma2.tutorlink.dao.cache.CachedLessonDao;
import it.uniroma2.tutorlink.dao.cache.CachedMaterialDao;
import it.uniroma2.tutorlink.dao.cache.CachedUserDao;
import it.uniroma2.tutorlink.dao.cache.Invalidatable;
import java.util.List;

public class JdbcDaoFactory implements DaoFactory {
    private final CachedUserDao userDao;
    private final CachedAvailabilityDao availabilityDao;
    private final FeedbackDao feedbackDao;
    private final CachedLessonDao lessonDao;
    private final CachedMaterialDao materialDao;
    private final NotificationDao notificationDao;

    public JdbcDaoFactory() {
        this(new ConnectionFactory());
    }

    public JdbcDaoFactory(ConnectionFactory connections) {
        this.userDao = new CachedUserDao(new JdbcUserDao(connections));
        this.availabilityDao = new CachedAvailabilityDao(new JdbcAvailabilityDao(connections, userDao));
        this.feedbackDao = new JdbcFeedbackDao(connections);
        this.lessonDao = new CachedLessonDao(
                new JdbcLessonDao(connections, userDao, availabilityDao, feedbackDao));
        this.materialDao = new CachedMaterialDao(new JdbcMaterialDao(connections, lessonDao));
        this.notificationDao = new JdbcNotificationDao(connections, userDao);
    }

    @Override
    public String name() {
        return "MySQL (full-version)";
    }

    @Override
    public UserDao createUserDao() {
        return userDao;
    }

    @Override
    public AvailabilityDao createAvailabilityDao() {
        return availabilityDao;
    }

    @Override
    public LessonDao createLessonDao() {
        return lessonDao;
    }

    @Override
    public MaterialDao createMaterialDao() {
        return materialDao;
    }

    @Override
    public FeedbackDao createFeedbackDao() {
        return feedbackDao;
    }

    @Override
    public NotificationDao createNotificationDao() {
        return notificationDao;
    }

    public List<Invalidatable> caches() {
        return List.of(userDao, availabilityDao, lessonDao, materialDao);
    }
}
