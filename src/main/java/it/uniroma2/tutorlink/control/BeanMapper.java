package it.uniroma2.tutorlink.control;

import it.uniroma2.tutorlink.bean.AvailabilityBean;
import it.uniroma2.tutorlink.bean.LessonBean;
import it.uniroma2.tutorlink.bean.MaterialBean;
import it.uniroma2.tutorlink.bean.NotificationBean;
import it.uniroma2.tutorlink.bean.ProgressPointBean;
import it.uniroma2.tutorlink.bean.TutorBean;
import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Material;
import it.uniroma2.tutorlink.model.Notification;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.model.progress.ProgressPoint;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

// Converte gli oggetti del model nei bean che vede la GUI.
public final class BeanMapper {
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private BeanMapper() {
        // costruttore privato: la classe non va istanziata
    }

    public static TutorBean toBean(Tutor tutor, int position, String reason) {
        TutorBean bean = new TutorBean();
        bean.setEmail(tutor.email());
        bean.setFullName(tutor.fullName());
        bean.setHourlyRate(tutor.hourlyRate().toString());
        bean.setSubjects(tutor.subjects().stream()
                .map(subject -> subject.displayName())
                .collect(Collectors.joining(", ")));
        bean.setDeliveredLessons(tutor.deliveredLessons());
        bean.setRankingPosition(position);
        bean.setMatchingReason(reason);
        return bean;
    }

    public static AvailabilityBean toBean(Availability availability) {
        AvailabilityBean bean = new AvailabilityBean();
        bean.setId(availability.id());
        bean.setTutorEmail(availability.tutor().email());
        bean.setTutorName(availability.tutor().fullName());
        bean.setDate(DATE.format(availability.slot().start()));
        bean.setTime(TIME.format(availability.slot().start()));
        bean.setMinutes(Integer.toString(availability.slot().minutes()));
        bean.setReserved(availability.isReserved());
        bean.setPrice(availability.tutor().priceFor(availability.slot()).toString());
        return bean;
    }

    public static LessonBean toBean(Lesson lesson) {
        LessonBean bean = new LessonBean();
        bean.setId(lesson.id());
        bean.setSubject(lesson.subject().displayName());
        bean.setTutorName(lesson.tutor().fullName());
        bean.setStudentName(lesson.student().fullName());
        bean.setStart(DATE_TIME.format(lesson.slot().start()));
        bean.setMinutes(lesson.slot().minutes());
        bean.setPrice(lesson.price().toString());
        bean.setState(lesson.stateName());
        bean.setMeetingLink(lesson.meetingLink().orElse(""));
        bean.setMaterialCount(lesson.publishedMaterials().size());
        lesson.feedback().ifPresent(feedback -> bean.setScore(feedback.score()));
        return bean;
    }

    public static MaterialBean toBean(Material material) {
        MaterialBean bean = new MaterialBean();
        bean.setId(material.id());
        bean.setLessonId(material.lesson().id());
        bean.setTitle(material.title());
        bean.setFileName(material.fileName());
        bean.setSizeBytes(material.sizeBytes());
        bean.setStatus(material.status().name());
        bean.setPublishedAt(material.publishedAt() == null ? "" : DATE_TIME.format(material.publishedAt()));
        return bean;
    }

    public static NotificationBean toBean(Notification notification) {
        NotificationBean bean = new NotificationBean();
        bean.setId(notification.id());
        bean.setMessage(notification.message());
        bean.setCreatedAt(DATE_TIME.format(notification.createdAt()));
        bean.setSeen(notification.isSeen());
        return bean;
    }

    public static ProgressPointBean toBean(ProgressPoint point) {
        ProgressPointBean bean = new ProgressPointBean();
        bean.setDate(DATE.format(point.date()));
        bean.setSubject(point.subject().displayName());
        bean.setScore(point.score());
        bean.setTutorName(point.tutorName());
        return bean;
    }
}
