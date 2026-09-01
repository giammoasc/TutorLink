package it.uniroma2.tutorlink.dao;

// Crea una famiglia coerente di DAO.
public interface DaoFactory {
    String name();

    UserDao createUserDao();

    AvailabilityDao createAvailabilityDao();

    LessonDao createLessonDao();

    MaterialDao createMaterialDao();

    FeedbackDao createFeedbackDao();

    NotificationDao createNotificationDao();
}
