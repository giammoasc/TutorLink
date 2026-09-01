package it.uniroma2.tutorlink.session;

import it.uniroma2.tutorlink.exception.AuthenticationException;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.model.User;
import java.util.Optional;

public final class SessionManager {
    private User currentUser;

    private SessionManager() {
        // costruttore privato: la classe non va istanziata
    }

    private static final class Holder {
        private static final SessionManager INSTANCE = new SessionManager();

        private Holder() {
            // costruttore privato: la classe non va istanziata
        }
    }

    public static SessionManager getInstance() {
        return Holder.INSTANCE;
    }

    public void open(User user) {
        this.currentUser = user;
    }

    public void close() {
        this.currentUser = null;
    }


    public Optional<User> currentUser() {
        return Optional.ofNullable(currentUser);
    }

    public Student requireStudent() throws AuthenticationException {
        if (currentUser instanceof Student) {
            return (Student) currentUser;
        }
        throw new AuthenticationException("this operation is reserved to a logged student");
    }

    public Tutor requireTutor() throws AuthenticationException {
        if (currentUser instanceof Tutor) {
            return (Tutor) currentUser;
        }
        throw new AuthenticationException("this operation is reserved to a logged tutor");
    }
}
