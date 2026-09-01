package it.uniroma2.tutorlink.control;

import it.uniroma2.tutorlink.bean.AvailabilityBean;
import it.uniroma2.tutorlink.dao.AvailabilityDao;
import it.uniroma2.tutorlink.dao.DaoFactory;
import it.uniroma2.tutorlink.exception.AuthenticationException;
import it.uniroma2.tutorlink.exception.OverlappingAvailabilityException;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.exception.ValidationException;
import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.model.TimeSlot;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.observer.AvailabilityPublisher;
import it.uniroma2.tutorlink.observer.AvailabilitySubject;
import it.uniroma2.tutorlink.util.IdGenerator;
import java.util.Comparator;
import java.util.List;

public class AvailabilityController extends AbstractApplicationController {
    private final AvailabilitySubject publisher;

    public AvailabilityController() {
        this(null, AvailabilityPublisher.getInstance());
    }

    public AvailabilityController(DaoFactory daoFactory, AvailabilitySubject publisher) {
        super(daoFactory == null
                ? it.uniroma2.tutorlink.dao.DaoFactoryProvider.getInstance().factory()
                : daoFactory);
        this.publisher = publisher;
    }

    public AvailabilityBean publish(AvailabilityBean request)
            throws AuthenticationException, OverlappingAvailabilityException,
                   PersistenceException, ValidationException {
        Tutor tutor = session().requireTutor();
        TimeSlot slot = request.toTimeSlot();
        Availability availability = tutor.publishAvailability(IdGenerator.next(), slot);

        AvailabilityDao availabilityDao = daos().createAvailabilityDao();
        availabilityDao.save(availability);

        publisher.notifyPublished(availability);
        return BeanMapper.toBean(availability);
    }

    public List<AvailabilityBean> myAvailabilities() throws AuthenticationException, PersistenceException {
        Tutor tutor = session().requireTutor();
        return daos().createAvailabilityDao().findByTutor(tutor).stream()
                .sorted(Comparator.comparing(Availability::slot))
                .map(BeanMapper::toBean)
                .toList();
    }
}
