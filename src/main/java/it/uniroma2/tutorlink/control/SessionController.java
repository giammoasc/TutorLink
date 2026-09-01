package it.uniroma2.tutorlink.control;

import it.uniroma2.tutorlink.bean.CredentialsBean;
import it.uniroma2.tutorlink.bean.RegistrationBean;
import it.uniroma2.tutorlink.dao.DaoFactory;
import it.uniroma2.tutorlink.dao.UserDao;
import it.uniroma2.tutorlink.exception.AccountAlreadyExistsException;
import it.uniroma2.tutorlink.exception.AuthenticationException;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Money;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.model.User;
import it.uniroma2.tutorlink.util.PasswordHasher;
import java.math.BigDecimal;
import java.util.Optional;

public class SessionController extends AbstractApplicationController {
    public SessionController() {
        super();
    }

    public SessionController(DaoFactory daoFactory) {
        super(daoFactory);
    }

    public String login(CredentialsBean credentials) throws AuthenticationException, PersistenceException {
        char[] password = credentials.getPassword();
        try {
            UserDao userDao = daos().createUserDao();
            Optional<User> account = userDao.findByEmail(credentials.getEmail());
            User user = account.orElseThrow(
                    () -> new AuthenticationException("no account is registered with this e-mail"));
            if (!user.authenticate(password)) {
                throw new AuthenticationException("the password does not match");
            }
            session().open(user);
            return user.role().name();
        } finally {
            PasswordHasher.wipe(password);
            credentials.clearPassword();
        }
    }

    public void register(RegistrationBean registration)
            throws AccountAlreadyExistsException, PersistenceException {
        UserDao userDao = daos().createUserDao();
        if (userDao.exists(registration.getEmail())) {
            throw new AccountAlreadyExistsException(
                    "the e-mail " + registration.getEmail() + " is already registered");
        }
        char[] password = registration.getPassword();
        try {
            String digest = PasswordHasher.hash(password);
            User user = registration.isTutor()
                    ? buildTutor(registration, digest)
                    : new Student(registration.getEmail(), registration.getFullName(), digest);
            userDao.save(user);
        } finally {
            PasswordHasher.wipe(password);
            registration.clearPasswords();
        }
    }

    private static Tutor buildTutor(RegistrationBean registration, String digest) {
        Money rate = Money.of(new BigDecimal(registration.getHourlyRate().replace(',', '.')));
        Tutor tutor = new Tutor(registration.getEmail(), registration.getFullName(), digest, rate);
        for (String subject : registration.getSubjects()) {
            tutor.teach(Subject.fromDisplayName(subject));
        }
        return tutor;
    }

    public void logout() {
        session().close();
    }

    public String currentUserName() {
        return session().currentUser().map(User::fullName).orElse("");
    }


}
