package it.uniroma2.tutorlink.dao.filesystem;

import it.uniroma2.tutorlink.config.AppConfig;
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
import it.uniroma2.tutorlink.exception.PersistenceException;
import java.nio.file.Path;

public class FileSystemDaoFactory implements DaoFactory {
    private final CachedUserDao userDao;
    private final CachedAvailabilityDao availabilityDao;
    private final FeedbackDao feedbackDao;
    private final CachedLessonDao lessonDao;
    private final CachedMaterialDao materialDao;
    private final NotificationDao notificationDao;

    public FileSystemDaoFactory() throws PersistenceException {
        this(Path.of(AppConfig.getInstance().getFilesystemRoot()));
    }

    public FileSystemDaoFactory(Path root) throws PersistenceException {
        this.userDao = new CachedUserDao(new FileSystemUserDao(root));
        this.availabilityDao = new CachedAvailabilityDao(new FileSystemAvailabilityDao(root, userDao));
        this.feedbackDao = new FileSystemFeedbackDao(root);
        this.lessonDao = new CachedLessonDao(
                new FileSystemLessonDao(root, userDao, availabilityDao, feedbackDao));
        this.materialDao = new CachedMaterialDao(new FileSystemMaterialDao(root, lessonDao));
        this.notificationDao = new FileSystemNotificationDao(root, userDao);
    }

    @Override
    public String name() {
        return "file system (full-version)";
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

    public java.util.List<it.uniroma2.tutorlink.dao.cache.Invalidatable> caches() {
        return java.util.List.of(userDao, availabilityDao, lessonDao, materialDao);
    }
}
