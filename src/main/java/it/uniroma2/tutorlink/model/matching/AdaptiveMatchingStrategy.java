package it.uniroma2.tutorlink.model.matching;

import it.uniroma2.tutorlink.model.Money;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.Tutor;
import it.uniroma2.tutorlink.model.progress.ProgressReport;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

// Ordina i tutor guardando lo storico dei voti dello studente.
public class AdaptiveMatchingStrategy implements TutorMatchingStrategy {
    // pesi delle quattro dimensioni, sommano a 1
    private static final double WEIGHT_SPECIALISATION = 0.35;
    private static final double WEIGHT_EXPERIENCE = 0.25;
    private static final double WEIGHT_FAMILIARITY = 0.20;
    private static final double WEIGHT_AFFORDABILITY = 0.20;

    private static final int EXPERIENCE_SATURATION = 20;
    private static final int FAMILIARITY_SATURATION = 3;

    @Override
    public String name() {
        return "adaptive";
    }

    @Override
    public List<Tutor> rank(Student student, List<Tutor> candidates, Subject subject) {
        if (candidates.size() < 2) {
            return List.copyOf(candidates);
        }
        ProgressReport report = student.progressReport();
        Money mostExpensive = mostExpensive(candidates);
        List<Tutor> ranked = new ArrayList<>(candidates);
        ranked.sort(Comparator
                .comparingDouble((Tutor tutor) -> -score(student, tutor, subject, report, mostExpensive))
                .thenComparing(Tutor::fullName));
        return List.copyOf(ranked);
    }

    @Override
    public String explain(Student student, Tutor tutor, Subject subject) {
        ProgressReport report = student.progressReport();
        List<String> reasons = new ArrayList<>();
        if (isWeakSubject(report, subject)) {
            reasons.add("covers a subject you are struggling with");
        }
        int previous = student.lessonsWith(tutor);
        if (previous > 0) {
            reasons.add(previous + " lesson(s) already taken together");
        }
        if (tutor.deliveredLessons() >= EXPERIENCE_SATURATION) {
            reasons.add("very experienced");
        }
        report.bestTimeBand().ifPresent(band ->
                reasons.add("your best results are around " + band + ":00"));
        return reasons.isEmpty() ? "available for this subject" : String.join(", ", reasons);
    }

    private double score(Student student, Tutor tutor, Subject subject,
                         ProgressReport report, Money mostExpensive) {
        double specialisation = isWeakSubject(report, subject) && tutor.teaches(subject) ? 1d : 0d;
        double experience = Math.min(1d, tutor.deliveredLessons() / (double) EXPERIENCE_SATURATION);
        double familiarity = Math.min(1d, student.lessonsWith(tutor) / (double) FAMILIARITY_SATURATION);
        double affordability = affordability(tutor, mostExpensive);
        return WEIGHT_SPECIALISATION * specialisation
                + WEIGHT_EXPERIENCE * experience
                + WEIGHT_FAMILIARITY * familiarity
                + WEIGHT_AFFORDABILITY * affordability;
    }

    private static boolean isWeakSubject(ProgressReport report, Subject subject) {
        if (report.isEmpty()) {
            return false;
        }
        Set<Subject> weak = report.weakSubjects();
        return weak.contains(subject);
    }

    private static Money mostExpensive(List<Tutor> candidates) {
        return candidates.stream()
                .map(Tutor::hourlyRate)
                .max(Money::compareTo)
                .orElse(Money.ZERO);
    }

    private static double affordability(Tutor tutor, Money mostExpensive) {
        if (mostExpensive.isZero()) {
            return 1d;
        }
        double rate = tutor.hourlyRate().amount().doubleValue();
        double maximum = mostExpensive.amount().doubleValue();
        return 1d - rate / maximum;
    }
}
