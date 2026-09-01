package it.uniroma2.tutorlink.control;

import it.uniroma2.tutorlink.bean.FeedbackBean;
import it.uniroma2.tutorlink.bean.LessonBean;
import it.uniroma2.tutorlink.dao.DaoFactory;
import it.uniroma2.tutorlink.dao.FeedbackDao;
import it.uniroma2.tutorlink.dao.LessonDao;
import it.uniroma2.tutorlink.exception.AuthenticationException;
import it.uniroma2.tutorlink.exception.IllegalLessonStateException;
import it.uniroma2.tutorlink.exception.MeetingLinkUnavailableException;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.exception.ValidationException;
import it.uniroma2.tutorlink.external.GoogleMeetAdapter;
import it.uniroma2.tutorlink.external.MeetingService;
import it.uniroma2.tutorlink.model.Feedback;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Tutor;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

// Avvio della lezione e chiusura con il voto.
public class LessonExecutionController extends AbstractApplicationController {
    private final MeetingService meetingService;

    public LessonExecutionController() {
        this(it.uniroma2.tutorlink.dao.DaoFactoryProvider.getInstance().factory(), new GoogleMeetAdapter());
    }

    public LessonExecutionController(DaoFactory daoFactory, MeetingService meetingService) {
        super(daoFactory);
        this.meetingService = meetingService;
    }

    public List<LessonBean> myUpcomingLessons() throws AuthenticationException, PersistenceException {
        Student student = session().requireStudent();
        daos().createLessonDao().findByStudent(student);
        return student.upcomingLessons(LocalDateTime.now()).stream()
                .map(BeanMapper::toBean)
                .toList();
    }

    public List<LessonBean> myDeliveredLessons() throws AuthenticationException, PersistenceException {
        Tutor tutor = session().requireTutor();
        return daos().createLessonDao().findByTutor(tutor).stream()
                .sorted(Comparator.comparing((Lesson lesson) -> lesson.slot().start()).reversed())
                .map(BeanMapper::toBean)
                .toList();
    }

    public String join(long lessonId)
            throws PersistenceException, IllegalLessonStateException, MeetingLinkUnavailableException {
        LessonDao lessonDao = daos().createLessonDao();
        Lesson lesson = lessonDao.findById(lessonId)
                .orElseThrow(() -> new PersistenceException("the lesson " + lessonId + " does not exist"));

        if (lesson.meetingLink().isEmpty()) {
            lesson.attachMeetingLink(meetingService.createMeeting(lesson));
        }
        lesson.start(LocalDateTime.now());
        lessonDao.update(lesson);
        return lesson.meetingLink().orElseThrow(
                () -> new MeetingLinkUnavailableException("the meeting link could not be issued"));
    }

    public LessonBean close(FeedbackBean request)
            throws AuthenticationException, PersistenceException, IllegalLessonStateException,
                   ValidationException {
        Tutor tutor = session().requireTutor();
        LessonDao lessonDao = daos().createLessonDao();
        Lesson lesson = lessonDao.findById(request.getLessonId())
                .orElseThrow(() -> new PersistenceException(
                        "the lesson " + request.getLessonId() + " does not exist"));
        if (!lesson.tutor().equals(tutor)) {
            throw new AuthenticationException("this lesson is not yours");
        }

        Feedback feedback = new Feedback(lesson, request.parsedScore(),
                request.getComment(), LocalDateTime.now());
        lesson.complete(feedback);

        FeedbackDao feedbackDao = daos().createFeedbackDao();
        feedbackDao.save(feedback);
        lessonDao.update(lesson);
        return BeanMapper.toBean(lesson);
    }

    public void cancel(long lessonId, String reason)
            throws PersistenceException, IllegalLessonStateException {
        LessonDao lessonDao = daos().createLessonDao();
        Lesson lesson = lessonDao.findById(lessonId)
                .orElseThrow(() -> new PersistenceException("the lesson " + lessonId + " does not exist"));
        lesson.cancel(reason, LocalDateTime.now());
        lessonDao.update(lesson);
        lesson.origin().ifPresent(availability -> updateQuietly(availability));
    }

    private void updateQuietly(it.uniroma2.tutorlink.model.Availability availability) {
        try {
            daos().createAvailabilityDao().update(availability);
        } catch (PersistenceException e) {
            java.util.logging.Logger.getLogger(LessonExecutionController.class.getName())
                    .log(java.util.logging.Level.WARNING, e,
                            () -> "the released slot " + availability.id() + " could not be stored");
        }
    }
}
