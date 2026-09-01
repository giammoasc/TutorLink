package it.uniroma2.tutorlink.bootstrap;

import it.uniroma2.tutorlink.config.AppConfig;
import it.uniroma2.tutorlink.dao.DaoFactory;
import it.uniroma2.tutorlink.dao.DaoFactoryProvider;
import it.uniroma2.tutorlink.notification.CompositeNotificationSender;
import it.uniroma2.tutorlink.notification.EmailNotificationSender;
import it.uniroma2.tutorlink.notification.InAppNotificationSender;
import it.uniroma2.tutorlink.notification.NotificationSender;
import it.uniroma2.tutorlink.observer.AvailabilityPublisher;
import it.uniroma2.tutorlink.observer.StudentNotifier;
import java.util.logging.Level;
import java.util.logging.Logger;

// Collega DAO, observer e dati di esempio all'avvio.
public final class ApplicationBootstrap {
    private static final Logger LOGGER = Logger.getLogger(ApplicationBootstrap.class.getName());

    private ApplicationBootstrap() {
        // costruttore privato: la classe non va istanziata
    }

    public static void start() {
        AppConfig config = AppConfig.getInstance();
        DaoFactory daos = DaoFactoryProvider.getInstance().factory();
        LOGGER.log(Level.INFO, "TutorLink starting: {0} on {1}",
                new Object[]{config.describe(), daos.name()});

        NotificationSender sender = new CompositeNotificationSender(
                new InAppNotificationSender(daos.createNotificationDao()),
                new EmailNotificationSender());
        AvailabilityPublisher.getInstance().attach(new StudentNotifier(daos.createLessonDao(), sender));

        new DemoDataSeeder(daos).seedIfEmpty();
    }
}
