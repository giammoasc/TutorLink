package it.uniroma2.tutorlink.model.matching;

import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.progress.ProgressReport;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

// Mette per primi gli slot nella fascia in cui lo studente va meglio.
public class BestTimeBandSlotStrategy implements SlotRankingStrategy {
    @Override
    public String name() {
        return "best-time-band";
    }

    @Override
    public List<Availability> rank(Student student, List<Availability> freeSlots) {
        ProgressReport report = student.progressReport();
        Optional<Integer> bestBand = report.bestTimeBand();
        List<Availability> ranked = new ArrayList<>(freeSlots);
        if (bestBand.isEmpty()) {
            ranked.sort(Comparator.comparing(Availability::slot));
            return List.copyOf(ranked);
        }
        int band = bestBand.get();
        ranked.sort(Comparator
                .comparingInt((Availability availability) -> distanceFromBand(availability, band))
                .thenComparing(Availability::slot));
        return List.copyOf(ranked);
    }

    private static int distanceFromBand(Availability availability, int band) {
        int slotBand = availability.slot().start().getHour() / 3 * 3;
        return Math.abs(slotBand - band);
    }
}
