package it.uniroma2.tutorlink.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.uniroma2.tutorlink.model.progress.ProgressPoint;
import it.uniroma2.tutorlink.model.progress.ProgressReport;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Test sulle statistiche dei progressi.
// In carico a: Ascenzi Gianmarco
class ProgressReportTest {
    private static final LocalDate DAY = LocalDate.of(2026, 3, 1);

    private static ProgressReport report() {
        return new ProgressReport(List.of(
                new ProgressPoint(DAY, 9, Subject.MATHEMATICS, 5, "Giulia"),
                new ProgressPoint(DAY.plusDays(7), 16, Subject.MATHEMATICS, 6, "Giulia"),
                new ProgressPoint(DAY.plusDays(14), 10, Subject.COMPUTER_SCIENCE, 9, "Luca"),
                new ProgressPoint(DAY.plusDays(21), 10, Subject.COMPUTER_SCIENCE, 10, "Luca")));
    }

    @Test
    @DisplayName("average and per subject averages are computed on the whole history")
    void averages() {
        ProgressReport report = report();
        assertEquals(7.5, report.average(), 0.0001);
        assertEquals(5.5, report.averageBySubject().get(Subject.MATHEMATICS), 0.0001);
        assertEquals(9.5, report.averageBySubject().get(Subject.COMPUTER_SCIENCE), 0.0001);
    }

    @Test
    @DisplayName("a rising history produces a positive trend and a positive variation")
    void trendIsPositive() {
        ProgressReport report = report();
        assertTrue(report.trendSlope() > 0, "the scores are increasing");
        assertTrue(report.isImproving());
        assertTrue(report.improvementPercentage() > 0);
    }

    @Test
    @DisplayName("the weak subjects are the ones below the overall average")
    void weakAndBestSubjects() {
        ProgressReport report = report();
        assertEquals(Subject.MATHEMATICS, report.weakestSubject().orElseThrow());
        assertEquals(Subject.COMPUTER_SCIENCE, report.bestSubject().orElseThrow());
        assertTrue(report.weakSubjects().contains(Subject.MATHEMATICS));
        assertFalse(report.weakSubjects().contains(Subject.COMPUTER_SCIENCE));
    }

    @Test
    @DisplayName("the suggested time band is the one with the best average, not the most frequent")
    void bestTimeBandUsesTheAverage() {
        ProgressReport report = report();
        assertEquals(9, report.bestTimeBand().orElseThrow().intValue(),
                "band 09:00-12:00 holds the scores 5, 9 and 10, band 15:00-18:00 only the 6");
    }

    @Test
    @DisplayName("an empty history answers without exploding")
    void emptyHistoryIsSafe() {
        ProgressReport empty = new ProgressReport(List.of());
        assertTrue(empty.isEmpty());
        assertEquals(0d, empty.average(), 0.0001);
        assertEquals(0d, empty.trendSlope(), 0.0001);
        assertTrue(empty.bestTimeBand().isEmpty());
        assertTrue(empty.weakSubjects().isEmpty());
    }
}
