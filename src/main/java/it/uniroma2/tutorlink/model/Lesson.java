package it.uniroma2.tutorlink.model;

import it.uniroma2.tutorlink.exception.IllegalLessonStateException;
import it.uniroma2.tutorlink.exception.MaterialQuotaExceededException;
import it.uniroma2.tutorlink.model.state.LessonState;
import it.uniroma2.tutorlink.model.state.LessonStates;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

// Lezione: tiene materiale e feedback e passa le transizioni al proprio stato.
public final class Lesson {
    private final long id;
    private final Student student;
    private final Tutor tutor;
    private final Subject subject;
    private final TimeSlot slot;
    private final Money price;
    private final Availability origin;
    private final List<Material> materials = new ArrayList<>();

    private LessonState state;
    private String meetingLink;
    private boolean deferredLink;
    private Feedback feedback;

    public Lesson(long id, Student student, Tutor tutor, Subject subject, TimeSlot slot,
                  Money price, Availability origin, LessonState state, String meetingLink) {
        this.id = id;
        this.student = Objects.requireNonNull(student, "student");
        this.tutor = Objects.requireNonNull(tutor, "tutor");
        this.subject = Objects.requireNonNull(subject, "subject");
        this.slot = Objects.requireNonNull(slot, "slot");
        this.price = Objects.requireNonNull(price, "price");
        this.origin = origin;
        this.state = state == null ? LessonStates.initial() : state;
        this.meetingLink = meetingLink;
        student.enrol(this);
        tutor.assign(this);
    }

    // prenota una lezione su uno slot libero: lo slot viene occupato subito
    public static Lesson schedule(long id, Student student, Availability availability, Subject subject)
            throws it.uniroma2.tutorlink.exception.SlotUnavailableException {
        Objects.requireNonNull(availability, "availability");
        Tutor tutor = availability.tutor();
        Money price = tutor.priceFor(availability.slot());
        availability.reserve();
        return new Lesson(id, student, tutor, subject, availability.slot(), price,
                availability, LessonStates.initial(), null);
    }

    public long id() {
        return id;
    }

    public Student student() {
        return student;
    }

    public Tutor tutor() {
        return tutor;
    }

    public Subject subject() {
        return subject;
    }

    public TimeSlot slot() {
        return slot;
    }

    public Money price() {
        return price;
    }

    public String stateName() {
        return state.name();
    }

    public LessonState state() {
        return state;
    }

    public boolean isTerminal() {
        return state.isTerminal();
    }

    public Optional<String> meetingLink() {
        return Optional.ofNullable(meetingLink);
    }

    public boolean needsDeferredLink() {
        return deferredLink;
    }

    public Optional<Feedback> feedback() {
        return Optional.ofNullable(feedback);
    }

    public void confirmPayment() throws IllegalLessonStateException {
        this.state = state.confirmPayment(this);
    }

    public void start(LocalDateTime now) throws IllegalLessonStateException {
        this.state = state.start(this, now);
    }

    public void complete(Feedback lessonFeedback) throws IllegalLessonStateException {
        this.state = state.complete(this, lessonFeedback);
        this.feedback = lessonFeedback;
    }

    public void cancel(String reason, LocalDateTime now) throws IllegalLessonStateException {
        this.state = state.cancel(this, reason, now);
    }

    public void releaseReservation() {
        if (origin != null) {
            origin.release();
        }
    }

    public Optional<Availability> origin() {
        return Optional.ofNullable(origin);
    }

    public void attachMeetingLink(String link) {
        this.meetingLink = link;
        this.deferredLink = false;
    }

    public void markLinkAsDeferred() {
        this.deferredLink = true;
    }

    public List<Material> materials() {
        return Collections.unmodifiableList(materials);
    }

    public List<Material> publishedMaterials() {
        return materials.stream().filter(Material::isVisibleToStudent).toList();
    }

    public List<Material> draftMaterials() {
        return materials.stream().filter(Material::isDraft).toList();
    }

    public long usedQuotaBytes() {
        return materials.stream()
                .filter(material -> material.status() != MaterialStatus.WITHDRAWN)
                .mapToLong(Material::sizeBytes)
                .sum();
    }

    public long residualQuotaBytes(long totalQuotaBytes) {
        return Math.max(0, totalQuotaBytes - usedQuotaBytes());
    }

    public void attach(Material material, long totalQuotaBytes)
            throws IllegalLessonStateException, MaterialQuotaExceededException {
        Objects.requireNonNull(material, "material");
        if (!state.allowsMaterial()) {
            throw new IllegalLessonStateException(
                    "material cannot be attached to a lesson in state " + state.name());
        }
        long residual = residualQuotaBytes(totalQuotaBytes);
        if (material.sizeBytes() > residual) {
            throw new MaterialQuotaExceededException(
                    "the file exceeds the residual quota of the lesson", residual);
        }
        reattach(material);
    }

    // usato dai DAO: rimette il feedback letto da disco senza cambiare stato
    public void restoreFeedback(Feedback storedFeedback) {
        this.feedback = storedFeedback;
    }

    // usato dai DAO: riattacca il materiale senza rifare i controlli
    public void reattach(Material material) {
        if (!materials.contains(material)) {
            materials.add(material);
        }
    }

    public List<Material> publishDraftMaterials(LocalDateTime when) {
        List<Material> published = new ArrayList<>();
        for (Material material : materials) {
            if (material.isDraft()) {
                material.publish(when);
                published.add(material);
            }
        }
        return published;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Lesson)) {
            return false;
        }
        Lesson lesson = (Lesson) other;
        return id == lesson.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return subject.displayName() + " " + slot + " - " + state.name();
    }
}
