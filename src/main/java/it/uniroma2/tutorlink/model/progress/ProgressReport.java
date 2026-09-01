package it.uniroma2.tutorlink.model.progress;

import it.uniroma2.tutorlink.model.Subject;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

// Statistiche sui voti: media, andamento, materie deboli, fascia migliore.
public class ProgressReport {
    private final List<ProgressPoint> points;

    public ProgressReport(List<ProgressPoint> points) {
        this.points = List.copyOf(points);
    }

    public List<ProgressPoint> points() {
        return Collections.unmodifiableList(points);
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }

    public int size() {
        return points.size();
    }

    public double average() {
        return points.stream().mapToInt(ProgressPoint::score).average().orElse(0d);
    }

    public Map<Subject, Double> averageBySubject() {
        Map<Subject, Double> sums = new EnumMap<>(Subject.class);
        Map<Subject, Integer> counts = new EnumMap<>(Subject.class);
        for (ProgressPoint point : points) {
            sums.merge(point.subject(), (double) point.score(), Double::sum);
            counts.merge(point.subject(), 1, Integer::sum);
        }
        Map<Subject, Double> averages = new EnumMap<>(Subject.class);
        sums.forEach((subject, total) -> averages.put(subject, total / counts.get(subject)));
        return averages;
    }

    // pendenza della retta di regressione sui voti: positiva se sta migliorando
    public double trendSlope() {
        int n = points.size();
        if (n < 2) {
            return 0d;
        }
        double meanX = (n - 1) / 2d;
        double meanY = average();
        double numerator = 0d;
        double denominator = 0d;
        for (int i = 0; i < n; i++) {
            double deltaX = i - meanX;
            numerator += deltaX * (points.get(i).score() - meanY);
            denominator += deltaX * deltaX;
        }
        return denominator == 0d ? 0d : numerator / denominator;
    }

    public boolean isImproving() {
        return trendSlope() > 0.05d;
    }

    public double improvementPercentage() {
        int n = points.size();
        if (n < 3) {
            return 0d;
        }
        int window = Math.max(1, n / 3);
        double first = points.subList(0, window).stream().mapToInt(ProgressPoint::score).average().orElse(0d);
        double last = points.subList(n - window, n).stream().mapToInt(ProgressPoint::score).average().orElse(0d);
        return first == 0d ? 0d : (last - first) / first * 100d;
    }

    public Optional<Subject> weakestSubject() {
        return averageBySubject().entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    public Optional<Subject> bestSubject() {
        return averageBySubject().entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    public Optional<Integer> bestTimeBand() {
        Map<Integer, Double> sums = new HashMap<>();
        Map<Integer, Integer> counts = new HashMap<>();
        for (ProgressPoint point : points) {
            sums.merge(point.timeBand(), (double) point.score(), Double::sum);
            counts.merge(point.timeBand(), 1, Integer::sum);
        }
        Integer best = null;
        double bestAverage = Double.NEGATIVE_INFINITY;
        for (Map.Entry<Integer, Double> entry : sums.entrySet()) {
            double average = entry.getValue() / counts.get(entry.getKey());
            if (average > bestAverage) {
                bestAverage = average;
                best = entry.getKey();
            }
        }
        return Optional.ofNullable(best);
    }

    public Set<Subject> weakSubjects() {
        double overall = average();
        Set<Subject> weak = new LinkedHashSet<>();
        averageBySubject().forEach((subject, value) -> {
            if (value < overall) {
                weak.add(subject);
            }
        });
        return weak;
    }
}
