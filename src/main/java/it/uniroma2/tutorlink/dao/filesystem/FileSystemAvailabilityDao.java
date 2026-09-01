package it.uniroma2.tutorlink.dao.filesystem;

import it.uniroma2.tutorlink.dao.AvailabilityDao;
import it.uniroma2.tutorlink.dao.UserDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.model.TimeSlot;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.util.IdGenerator;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileSystemAvailabilityDao extends AbstractCsvDao<Availability> implements AvailabilityDao {
    private static final String FILE_NAME = "availabilities.csv";

    private final UserDao userDao;

    public FileSystemAvailabilityDao(Path root, UserDao userDao) throws PersistenceException {
        super(root, FILE_NAME);
        this.userDao = userDao;
    }

    @Override
    protected String[] toRecord(Availability availability) {
        return new String[]{
                Long.toString(availability.id()),
                availability.tutor().email(),
                availability.slot().start().toString(),
                Integer.toString(availability.slot().minutes()),
                Boolean.toString(availability.isReserved())};
    }

    @Override
    protected Availability fromRecord(String[] record) throws PersistenceException {
        if (record.length < 5) {
            throw new PersistenceException("malformed record in " + file());
        }
        long id = Long.parseLong(record[0]);
        IdGenerator.observe(id);
        Tutor tutor = userDao.findByEmail(record[1])
                .filter(Tutor.class::isInstance)
                .map(Tutor.class::cast)
                .orElseThrow(() -> new PersistenceException("the tutor " + record[1] + " does not exist any more"));
        TimeSlot slot = new TimeSlot(LocalDateTime.parse(record[2]), Integer.parseInt(record[3]));
        Availability availability = new Availability(id, tutor, slot, Boolean.parseBoolean(record[4]));
        tutor.addAvailability(availability);
        return availability;
    }

    @Override
    protected String keyOf(String[] record) {
        return record[0];
    }

    @Override
    public void save(Availability availability) throws PersistenceException {
        upsert(availability);
    }

    @Override
    public void update(Availability availability) throws PersistenceException {
        upsert(availability);
    }

    @Override
    public Optional<Availability> findById(long id) throws PersistenceException {
        String key = Long.toString(id);
        for (String[] record : readRecords()) {
            if (keyOf(record).equals(key)) {
                return Optional.of(fromRecord(record));
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Availability> findByTutor(Tutor tutor) throws PersistenceException {
        List<Availability> result = new ArrayList<>();
        for (String[] record : readRecords()) {
            if (record[1].equalsIgnoreCase(tutor.email())) {
                result.add(fromRecord(record));
            }
        }
        return result;
    }
}
