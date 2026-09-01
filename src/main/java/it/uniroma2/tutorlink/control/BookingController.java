package it.uniroma2.tutorlink.control;

import it.uniroma2.tutorlink.bean.AvailabilityBean;
import it.uniroma2.tutorlink.bean.BookingRequestBean;
import it.uniroma2.tutorlink.bean.LessonBean;
import it.uniroma2.tutorlink.bean.TutorBean;
import it.uniroma2.tutorlink.dao.AvailabilityDao;
import it.uniroma2.tutorlink.dao.DaoFactory;
import it.uniroma2.tutorlink.dao.LessonDao;
import it.uniroma2.tutorlink.dao.UserDao;
import it.uniroma2.tutorlink.exception.AuthenticationException;
import it.uniroma2.tutorlink.exception.IllegalLessonStateException;
import it.uniroma2.tutorlink.exception.MeetingLinkUnavailableException;
import it.uniroma2.tutorlink.exception.PaymentFailedException;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.exception.SlotUnavailableException;
import it.uniroma2.tutorlink.exception.ValidationException;
import it.uniroma2.tutorlink.external.GoogleMeetAdapter;
import it.uniroma2.tutorlink.external.MeetingService;
import it.uniroma2.tutorlink.external.PaymentCard;
import it.uniroma2.tutorlink.external.PaymentGateway;
import it.uniroma2.tutorlink.external.SimulatedPaymentGateway;
import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Notification;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.model.matching.AdaptiveMatchingStrategy;
import it.uniroma2.tutorlink.model.matching.BestTimeBandSlotStrategy;
import it.uniroma2.tutorlink.model.matching.SlotRankingStrategy;
import it.uniroma2.tutorlink.model.matching.TutorMatchingStrategy;
import it.uniroma2.tutorlink.notification.CompositeNotificationSender;
import it.uniroma2.tutorlink.notification.EmailNotificationSender;
import it.uniroma2.tutorlink.notification.InAppNotificationSender;
import it.uniroma2.tutorlink.notification.NotificationSender;
import it.uniroma2.tutorlink.util.IdGenerator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// Prenotazione di una lezione: materia, tutor, slot, pagamento, link.
public class BookingController extends AbstractApplicationController {
    private static final Logger LOGGER = Logger.getLogger(BookingController.class.getName());

    private static final int CALENDAR_HORIZON_DAYS = 30;

    private final PaymentGateway paymentGateway;
    private final MeetingService meetingService;
    private final NotificationSender notificationSender;
    private final TutorMatchingStrategy matchingStrategy;
    private final SlotRankingStrategy slotStrategy;

    public BookingController() {
        this(it.uniroma2.tutorlink.dao.DaoFactoryProvider.getInstance().factory(),
                new SimulatedPaymentGateway(), new GoogleMeetAdapter(), null,
                new AdaptiveMatchingStrategy(), new BestTimeBandSlotStrategy());
    }

    public BookingController(DaoFactory daoFactory, PaymentGateway paymentGateway,
                             MeetingService meetingService, NotificationSender notificationSender,
                             TutorMatchingStrategy matchingStrategy, SlotRankingStrategy slotStrategy) {
        super(daoFactory);
        this.paymentGateway = paymentGateway;
        this.meetingService = meetingService;
        this.notificationSender = notificationSender == null
                ? new CompositeNotificationSender(
                        new InAppNotificationSender(daoFactory.createNotificationDao()),
                        new EmailNotificationSender())
                : notificationSender;
        this.matchingStrategy = matchingStrategy;
        this.slotStrategy = slotStrategy;
    }

    public List<String> listSubjects() {
        return java.util.Arrays.stream(Subject.values()).map(Subject::displayName).toList();
    }

    public List<TutorBean> rankedTutors(String subjectName)
            throws AuthenticationException, PersistenceException, ValidationException {
        Student student = session().requireStudent();
        Subject subject = parseSubject(subjectName);
        UserDao userDao = daos().createUserDao();
        List<Tutor> candidates = userDao.findTutorsBySubject(subject);
        hydrate(student);
        List<Tutor> ranked = matchingStrategy.rank(student, candidates, subject);
        List<TutorBean> beans = new ArrayList<>(ranked.size());
        for (int i = 0; i < ranked.size(); i++) {
            Tutor tutor = ranked.get(i);
            beans.add(BeanMapper.toBean(tutor, i + 1, matchingStrategy.explain(student, tutor, subject)));
        }
        return beans;
    }

    public List<AvailabilityBean> freeSlots(String tutorEmail)
            throws AuthenticationException, PersistenceException {
        Student student = session().requireStudent();
        hydrate(student);
        UserDao userDao = daos().createUserDao();
        Tutor tutor = userDao.findByEmail(tutorEmail)
                .filter(Tutor.class::isInstance)
                .map(Tutor.class::cast)
                .orElseThrow(() -> new PersistenceException("the tutor " + tutorEmail + " is not available"));

        daos().createAvailabilityDao().findByTutor(tutor);

        LocalDateTime now = LocalDateTime.now();
        LocalDate from = now.toLocalDate();
        LocalDate to = from.plusDays(CALENDAR_HORIZON_DAYS);
        List<Availability> free = tutor.freeAvailabilities(from, to, now).stream()
                .filter(availability -> !student.hasConflict(availability.slot()))
                .toList();
        return slotStrategy.rank(student, free).stream().map(BeanMapper::toBean).toList();
    }

    public LessonBean confirmBooking(BookingRequestBean request)
            throws AuthenticationException, SlotUnavailableException, PaymentFailedException,
                   PersistenceException, IllegalLessonStateException, ValidationException {
        Student student = session().requireStudent();
        hydrate(student);
        Subject subject = parseSubject(request.getSubject());
        LocalDateTime now = LocalDateTime.now();

        AvailabilityDao availabilityDao = daos().createAvailabilityDao();
        LessonDao lessonDao = daos().createLessonDao();

        Availability availability = availabilityDao.findById(request.getAvailabilityId())
                .orElseThrow(() -> new SlotUnavailableException(
                        "the selected slot does not exist any more", request.getAvailabilityId()));

        if (!availability.isBookableAt(now)) {
            throw new SlotUnavailableException(
                    "the slot of " + availability.slot() + " is not available any more", availability.id());
        }
        if (student.hasConflict(availability.slot())) {
            throw new SlotUnavailableException(
                    "you already have a lesson overlapping " + availability.slot(), availability.id());
        }

        Lesson lesson = Lesson.schedule(IdGenerator.next(), student, availability, subject);
        availabilityDao.update(availability);
        lessonDao.save(lesson);

        settlePayment(request, lesson, availability, availabilityDao, lessonDao, now);

        lesson.confirmPayment();
        issueMeetingLink(lesson);
        lessonDao.update(lesson);

        notifyTutor(lesson);
        return BeanMapper.toBean(lesson);
    }

    private void settlePayment(BookingRequestBean request, Lesson lesson, Availability availability,
                               AvailabilityDao availabilityDao, LessonDao lessonDao, LocalDateTime now)
            throws PaymentFailedException, PersistenceException, IllegalLessonStateException {
        PaymentCard card = new PaymentCard(request.getCardHolder(), request.getCardNumber(),
                request.getCardExpiry());
        try {
            paymentGateway.charge(lesson.price(), card);
        } catch (PaymentFailedException e) {
            // pagamento rifiutato: libero lo slot e chiudo la lezione prima di risalire
            lesson.cancel("payment refused (" + e.getReasonCode() + ")", now);
            availabilityDao.update(availability);
            lessonDao.update(lesson);
            throw e;
        }
    }

    private void issueMeetingLink(Lesson lesson) {
        try {
            lesson.attachMeetingLink(meetingService.createMeeting(lesson));
        } catch (MeetingLinkUnavailableException e) {
            // il servizio non risponde: la lezione resta valida, il link si crea al join
            lesson.markLinkAsDeferred();
            LOGGER.log(Level.WARNING, e,
                    () -> "the meeting link of the lesson " + lesson.id() + " has been deferred");
        }
    }

    private void notifyTutor(Lesson lesson) {
        String message = lesson.student().fullName() + " booked a "
                + lesson.subject().displayName() + " lesson on " + lesson.slot();
        notificationSender.send(new Notification(IdGenerator.next(), lesson.tutor(),
                message, LocalDateTime.now()));
    }

    private void hydrate(Student student) throws PersistenceException {
        // carica le lezioni dello studente: da qui in poi si naviga sugli oggetti
        daos().createLessonDao().findByStudent(student);
    }

    private static Subject parseSubject(String subjectName) throws ValidationException {
        try {
            return Subject.fromDisplayName(subjectName);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("subject", "'" + subjectName + "' is not a subject of the platform");
        }
    }
}
