package it.uniroma2.tutorlink.model.matching;

import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.Tutor;
import java.util.Comparator;
import java.util.List;

// Ordine alfabetico, tenuto come termine di confronto.
public class AlphabeticMatchingStrategy implements TutorMatchingStrategy {
    @Override
    public String name() {
        return "alphabetic";
    }

    @Override
    public List<Tutor> rank(Student student, List<Tutor> candidates, Subject subject) {
        return candidates.stream()
                .sorted(Comparator.comparing(Tutor::fullName))
                .toList();
    }

    @Override
    public String explain(Student student, Tutor tutor, Subject subject) {
        return "listed in alphabetical order";
    }
}
