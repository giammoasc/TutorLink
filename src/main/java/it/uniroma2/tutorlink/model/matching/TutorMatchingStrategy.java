package it.uniroma2.tutorlink.model.matching;

import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.Tutor;
import java.util.List;

public interface TutorMatchingStrategy {
    String name();

    List<Tutor> rank(Student student, List<Tutor> candidates, Subject subject);

    String explain(Student student, Tutor tutor, Subject subject);
}
