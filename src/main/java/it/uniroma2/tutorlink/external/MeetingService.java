package it.uniroma2.tutorlink.external;

import it.uniroma2.tutorlink.exception.MeetingLinkUnavailableException;
import it.uniroma2.tutorlink.model.Lesson;

public interface MeetingService {
    String createMeeting(Lesson lesson) throws MeetingLinkUnavailableException;
}
