package it.uniroma2.tutorlink.dao.filesystem;

import it.uniroma2.tutorlink.dao.UserDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Money;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.model.User;
import it.uniroma2.tutorlink.model.UserRole;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class FileSystemUserDao extends AbstractCsvDao<User> implements UserDao {
    private static final String FILE_NAME = "users.csv";
    private static final String SUBJECT_SEPARATOR = ",";

    public FileSystemUserDao(Path root) throws PersistenceException {
        super(root, FILE_NAME);
    }

    @Override
    protected String[] toRecord(User user) {
        String rate = "";
        String subjects = "";
        if (user instanceof Tutor) {
            Tutor tutor = (Tutor) user;
            rate = tutor.hourlyRate().amount().toPlainString();
            subjects = tutor.subjects().stream().map(Enum::name).reduce((a, b) -> a + SUBJECT_SEPARATOR + b).orElse("");
        }
        return new String[]{user.email(), user.fullName(), user.passwordDigest(),
                user.role().name(), rate, subjects};
    }

    @Override
    protected User fromRecord(String[] record) throws PersistenceException {
        if (record.length < 4) {
            throw new PersistenceException("malformed record in " + file());
        }
        UserRole role = UserRole.valueOf(record[3]);
        if (role == UserRole.STUDENT) {
            return new Student(record[0], record[1], record[2]);
        }
        Money rate = record.length > 4 && !record[4].isBlank()
                ? Money.of(new BigDecimal(record[4]))
                : Money.ZERO;
        Tutor tutor = new Tutor(record[0], record[1], record[2], rate);
        if (record.length > 5 && !record[5].isBlank()) {
            for (String subject : record[5].split(SUBJECT_SEPARATOR)) {
                tutor.teach(Subject.valueOf(subject.trim()));
            }
        }
        return tutor;
    }

    @Override
    protected String keyOf(String[] record) {
        return record[0].toLowerCase(Locale.ROOT);
    }

    @Override
    public Optional<User> findByEmail(String email) throws PersistenceException {
        String key = email == null ? "" : email.toLowerCase(Locale.ROOT);
        for (String[] record : readRecords()) {
            if (keyOf(record).equals(key)) {
                return Optional.of(fromRecord(record));
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean exists(String email) throws PersistenceException {
        return findByEmail(email).isPresent();
    }

    @Override
    public void save(User user) throws PersistenceException {
        upsert(user);
    }

    @Override
    public List<Tutor> findTutorsBySubject(Subject subject) throws PersistenceException {
        return loadAll().stream()
                .filter(Tutor.class::isInstance)
                .map(Tutor.class::cast)
                .filter(tutor -> tutor.teaches(subject))
                .toList();
    }

    @Override
    public List<Student> findAllStudents() throws PersistenceException {
        return loadAll().stream()
                .filter(Student.class::isInstance)
                .map(Student.class::cast)
                .toList();
    }
}
