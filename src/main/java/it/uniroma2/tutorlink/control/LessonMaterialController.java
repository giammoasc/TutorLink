package it.uniroma2.tutorlink.control;

import it.uniroma2.tutorlink.bean.LessonBean;
import it.uniroma2.tutorlink.bean.MaterialBean;
import it.uniroma2.tutorlink.config.AppConfig;
import it.uniroma2.tutorlink.dao.DaoFactory;
import it.uniroma2.tutorlink.dao.LessonDao;
import it.uniroma2.tutorlink.dao.MaterialDao;
import it.uniroma2.tutorlink.exception.AuthenticationException;
import it.uniroma2.tutorlink.exception.IllegalLessonStateException;
import it.uniroma2.tutorlink.exception.MaterialRejectedException;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.exception.UnsupportedMaterialFormatException;
import it.uniroma2.tutorlink.model.Lesson;
import it.uniroma2.tutorlink.model.Material;
import it.uniroma2.tutorlink.model.Notification;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.notification.CompositeNotificationSender;
import it.uniroma2.tutorlink.notification.EmailNotificationSender;
import it.uniroma2.tutorlink.notification.InAppNotificationSender;
import it.uniroma2.tutorlink.notification.NotificationSender;
import it.uniroma2.tutorlink.util.IdGenerator;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Materiale della lezione: allega, pubblica, scarica.
public class LessonMaterialController extends AbstractApplicationController {
    private static final int RECENT_WINDOW_DAYS = 30;

    private final NotificationSender notificationSender;
    private final long quotaBytes;

    public LessonMaterialController() {
        this(it.uniroma2.tutorlink.dao.DaoFactoryProvider.getInstance().factory(), null,
                AppConfig.getInstance().getMaterialQuotaBytes());
    }

    public LessonMaterialController(DaoFactory daoFactory, NotificationSender sender, long quotaBytes) {
        super(daoFactory);
        this.notificationSender = sender == null
                ? new CompositeNotificationSender(
                        new InAppNotificationSender(daoFactory.createNotificationDao()),
                        new EmailNotificationSender())
                : sender;
        this.quotaBytes = quotaBytes;
    }

    public List<LessonBean> myLessons() throws AuthenticationException, PersistenceException {
        Tutor tutor = session().requireTutor();
        LocalDateTime lowerBound = LocalDateTime.now().minusDays(RECENT_WINDOW_DAYS);
        LessonDao lessonDao = daos().createLessonDao();
        MaterialDao materialDao = daos().createMaterialDao();
        List<LessonBean> beans = new ArrayList<>();
        for (Lesson lesson : lessonDao.findByTutor(tutor)) {
            if (lesson.slot().start().isAfter(lowerBound)) {
                materialDao.findByLesson(lesson);
                beans.add(BeanMapper.toBean(lesson));
            }
        }
        beans.sort(Comparator.comparing(LessonBean::getStart).reversed());
        return beans;
    }

    public List<MaterialBean> materialsOf(long lessonId) throws PersistenceException {
        Lesson lesson = requireLesson(lessonId);
        return daos().createMaterialDao().findByLesson(lesson).stream()
                .map(BeanMapper::toBean)
                .toList();
    }

    public long residualQuota(long lessonId) throws PersistenceException {
        Lesson lesson = requireLesson(lessonId);
        daos().createMaterialDao().findByLesson(lesson);
        return lesson.residualQuotaBytes(quotaBytes);
    }

    public MaterialBean attach(MaterialBean request)
            throws AuthenticationException, PersistenceException, MaterialRejectedException,
                   IllegalLessonStateException {
        session().requireTutor();
        Lesson lesson = requireLesson(request.getLessonId());
        MaterialDao materialDao = daos().createMaterialDao();
        materialDao.findByLesson(lesson);

        Material material = buildMaterial(request, lesson);
        lesson.attach(material, quotaBytes);
        materialDao.save(material);
        storeContentIfAvailable(request, material, materialDao);
        return BeanMapper.toBean(material);
    }

    private Material buildMaterial(MaterialBean request, Lesson lesson)
            throws UnsupportedMaterialFormatException {
        return new Material(IdGenerator.next(), lesson, request.getTitle(),
                request.getFileName(), request.getSizeBytes());
    }

    private void storeContentIfAvailable(MaterialBean request, Material material, MaterialDao materialDao)
            throws PersistenceException {
        if (request.getSourcePath() == null || request.getSourcePath().isBlank()) {
            return;
        }
        try {
            materialDao.storeContent(material.id(), Files.readAllBytes(Path.of(request.getSourcePath())));
        } catch (IOException e) {
            throw new PersistenceException(
                    "the file " + request.getSourcePath() + " cannot be read from the disk", e);
        }
    }

    public List<MaterialBean> publish(long lessonId)
            throws AuthenticationException, PersistenceException, IllegalLessonStateException {
        session().requireTutor();
        Lesson lesson = requireLesson(lessonId);
        MaterialDao materialDao = daos().createMaterialDao();
        materialDao.findByLesson(lesson);

        if (lesson.isTerminal() && !lesson.state().allowsMaterial()) {
            throw new IllegalLessonStateException(
                    "the material of a lesson in state " + lesson.stateName() + " cannot be published");
        }

        List<Material> published = lesson.publishDraftMaterials(LocalDateTime.now());
        if (published.isEmpty()) {
            return List.of();
        }
        materialDao.updateAll(published);
        notifyStudent(lesson, published.size());
        return published.stream().map(BeanMapper::toBean).toList();
    }

    private void notifyStudent(Lesson lesson, int count) {
        Student student = lesson.student();
        String message = lesson.tutor().fullName() + " shared " + count
                + " new file(s) for the " + lesson.subject().displayName() + " lesson of "
                + lesson.slot();
        notificationSender.send(new Notification(IdGenerator.next(), student, message, LocalDateTime.now()));
    }

    public List<MaterialBean> publishedMaterialsForStudent(long lessonId)
            throws AuthenticationException, PersistenceException {
        Student student = session().requireStudent();
        Lesson lesson = requireLesson(lessonId);
        if (!lesson.student().equals(student)) {
            throw new AuthenticationException("this lesson does not belong to you");
        }
        daos().createMaterialDao().findByLesson(lesson);
        return lesson.publishedMaterials().stream().map(BeanMapper::toBean).toList();
    }

    public Path download(long lessonId, long materialId, Path targetDirectory)
            throws AuthenticationException, PersistenceException {
        Student student = session().requireStudent();
        Lesson lesson = requireLesson(lessonId);
        if (!lesson.student().equals(student)) {
            throw new AuthenticationException("this lesson does not belong to you");
        }
        MaterialDao materialDao = daos().createMaterialDao();
        Material material = materialDao.findByLesson(lesson).stream()
                .filter(candidate -> candidate.id() == materialId)
                .filter(Material::isVisibleToStudent)
                .findFirst()
                .orElseThrow(() -> new PersistenceException("the material is not available any more"));

        byte[] content = materialDao.loadContent(material.id());
        Path target = targetDirectory.resolve(material.fileName());
        try {
            Files.createDirectories(targetDirectory);
            Files.write(target, content);
        } catch (IOException e) {
            throw new PersistenceException("the file cannot be written in " + targetDirectory, e);
        }
        return target;
    }

    private Lesson requireLesson(long lessonId) throws PersistenceException {
        return daos().createLessonDao().findById(lessonId)
                .orElseThrow(() -> new PersistenceException("the lesson " + lessonId + " does not exist"));
    }



}
