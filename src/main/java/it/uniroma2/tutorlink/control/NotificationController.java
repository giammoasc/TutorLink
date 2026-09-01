package it.uniroma2.tutorlink.control;

import it.uniroma2.tutorlink.bean.NotificationBean;
import it.uniroma2.tutorlink.dao.DaoFactory;
import it.uniroma2.tutorlink.dao.NotificationDao;
import it.uniroma2.tutorlink.exception.AuthenticationException;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Notification;
import it.uniroma2.tutorlink.model.User;
import java.util.List;

public class NotificationController extends AbstractApplicationController {
    public NotificationController() {
        super();
    }

    public NotificationController(DaoFactory daoFactory) {
        super(daoFactory);
    }

    public List<NotificationBean> inbox() throws AuthenticationException, PersistenceException {
        User user = currentUser();
        return daos().createNotificationDao().findByRecipient(user).stream()
                .map(BeanMapper::toBean)
                .toList();
    }


    public void markSeen(long notificationId) throws AuthenticationException, PersistenceException {
        User user = currentUser();
        NotificationDao notificationDao = daos().createNotificationDao();
        for (Notification notification : notificationDao.findByRecipient(user)) {
            if (notification.id() == notificationId) {
                notification.markSeen();
                notificationDao.update(notification);
                return;
            }
        }
    }

    private User currentUser() throws AuthenticationException {
        return session().currentUser()
                .orElseThrow(() -> new AuthenticationException("no user is logged in"));
    }
}
