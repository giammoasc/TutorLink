package it.uniroma2.tutorlink.model;

import it.uniroma2.tutorlink.model.progress.ProgressPoint;
import it.uniroma2.tutorlink.model.progress.ProgressReport;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Studente: conosce la propria agenda e il proprio storico di voti.
public class Student extends User {
    private final List<Lesson> lessons = new ArrayList<>();

    public Student(String email, String fullName, String passwordDigest) {
        super(email, fullName, passwordDigest);
    }

    @Override
    public UserRole role() {
        return UserRole.STUDENT;
    }

    public void enrol(Lesson lesson) {
        if (!lessons.contains(lesson)) {
            lessons.add(lesson);
        }
    }

    public List<Lesson> lessons() {
        return Collections.unmodifiableList(lessons);
    }

    public List<Lesson> upcomingLessons(LocalDateTime now) {
        return lessons.stream()
                .filter(lesson -> !lesson.isTerminal())
                .filter(lesson -> !lesson.slot().isInThePast(now))
                .sorted((a, b) -> a.slot().compareTo(b.slot()))
                .toList();
    }

    public List<Lesson> completedLessons() {
        return lessons.stream()
                .filter(lesson -> lesson.feedback().isPresent())
                .toList();
    }

    public boolean hasConflict(TimeSlot candidate) {
        return lessons.stream()
                .filter(lesson -> !lesson.isTerminal())
                .anyMatch(lesson -> lesson.slot().overlaps(candidate));
    }


    public int lessonsWith(Tutor tutor) {
        return (int) lessons.stream().filter(lesson -> lesson.tutor().equals(tutor)).count();
    }

    public ProgressReport progressReport() {
        List<ProgressPoint> points = completedLessons().stream()
                .sorted((a, b) -> a.slot().compareTo(b.slot()))
                .map(lesson -> new ProgressPoint(
                        lesson.slot().date(),
                        lesson.slot().start().getHour(),
                        lesson.subject(),
                        lesson.feedback().orElseThrow().score(),
                        lesson.tutor().fullName()))
                .toList();
        return new ProgressReport(points);
    }
}
