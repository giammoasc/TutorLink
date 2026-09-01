package it.uniroma2.tutorlink.dao.jdbc;

import it.uniroma2.tutorlink.dao.AvailabilityDao;
import it.uniroma2.tutorlink.dao.UserDao;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.model.TimeSlot;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.util.IdGenerator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcAvailabilityDao implements AvailabilityDao {
    private static final String UPSERT =
            "INSERT INTO availability (id, tutor_email, start_at, minutes, reserved) VALUES (?, ?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE start_at = VALUES(start_at), minutes = VALUES(minutes), "
                    + "reserved = VALUES(reserved)";
    private static final String SELECT_BY_ID =
            "SELECT id, tutor_email, start_at, minutes, reserved FROM availability WHERE id = ?";
    private static final String SELECT_BY_TUTOR =
            "SELECT id, tutor_email, start_at, minutes, reserved FROM availability WHERE tutor_email = ? "
                    + "ORDER BY start_at";

    private final ConnectionFactory connections;
    private final UserDao userDao;

    public JdbcAvailabilityDao(ConnectionFactory connections, UserDao userDao) {
        this.connections = connections;
        this.userDao = userDao;
    }

    @Override
    public void save(Availability availability) throws PersistenceException {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(UPSERT)) {
            statement.setLong(1, availability.id());
            statement.setString(2, availability.tutor().email());
            statement.setTimestamp(3, Timestamp.valueOf(availability.slot().start()));
            statement.setInt(4, availability.slot().minutes());
            statement.setBoolean(5, availability.isReserved());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenceException("the availability " + availability.id() + " cannot be stored", e);
        }
    }

    @Override
    public void update(Availability availability) throws PersistenceException {
        save(availability);
    }

    @Override
    public Optional<Availability> findById(long id) throws PersistenceException {
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_ID)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(mapRow(resultSet)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenceException("the availability " + id + " cannot be read", e);
        }
    }

    @Override
    public List<Availability> findByTutor(Tutor tutor) throws PersistenceException {
        List<Availability> availabilities = new ArrayList<>();
        try (Connection connection = connections.open();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_TUTOR)) {
            statement.setString(1, tutor.email());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    availabilities.add(build(resultSet, tutor));
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException("the calendar of " + tutor.email() + " cannot be read", e);
        }
        return availabilities;
    }

    private Availability mapRow(ResultSet resultSet) throws SQLException, PersistenceException {
        String email = resultSet.getString("tutor_email");
        Tutor tutor = userDao.findByEmail(email)
                .filter(Tutor.class::isInstance)
                .map(Tutor.class::cast)
                .orElseThrow(() -> new PersistenceException("the tutor " + email + " does not exist any more"));
        return build(resultSet, tutor);
    }

    private static Availability build(ResultSet resultSet, Tutor tutor) throws SQLException {
        long id = resultSet.getLong("id");
        IdGenerator.observe(id);
        TimeSlot slot = new TimeSlot(resultSet.getTimestamp("start_at").toLocalDateTime(),
                resultSet.getInt("minutes"));
        Availability availability = new Availability(id, tutor, slot, resultSet.getBoolean("reserved"));
        tutor.addAvailability(availability);
        return availability;
    }
}
