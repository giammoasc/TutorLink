package it.uniroma2.tutorlink.external;

import it.uniroma2.tutorlink.exception.MeetingLinkUnavailableException;
import it.uniroma2.tutorlink.model.Lesson;
import java.util.Locale;
import java.util.Random;

// Crea il link della videochiamata partendo dall'id della lezione.
public class GoogleMeetAdapter implements MeetingService {
    private static final String BASE_URL = "https://meet.google.com/";
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    private static final int[] GROUPS = {3, 4, 3};

    private boolean serviceDown;

    public void simulateOutage(boolean down) {
        this.serviceDown = down;
    }

    @Override
    public String createMeeting(Lesson lesson) throws MeetingLinkUnavailableException {
        if (serviceDown) {
            throw new MeetingLinkUnavailableException(
                    "the calendar service did not answer, the link will be issued later");
        }
        // seme fisso: la stessa lezione da' sempre lo stesso link
        Random deterministic = new Random(lesson.id());
        StringBuilder code = new StringBuilder(BASE_URL);
        for (int groupIndex = 0; groupIndex < GROUPS.length; groupIndex++) {
            if (groupIndex > 0) {
                code.append('-');
            }
            for (int i = 0; i < GROUPS[groupIndex]; i++) {
                code.append(ALPHABET.charAt(deterministic.nextInt(ALPHABET.length())));
            }
        }
        return code.toString().toLowerCase(Locale.ROOT);
    }
}
