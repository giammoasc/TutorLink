package it.uniroma2.tutorlink.model.matching;

import it.uniroma2.tutorlink.model.Availability;
import it.uniroma2.tutorlink.model.Student;
import java.util.List;

public interface SlotRankingStrategy {
    String name();

    List<Availability> rank(Student student, List<Availability> freeSlots);
}
