package it.uniroma2.tutorlink.bean;

import it.uniroma2.tutorlink.exception.ValidationException;
import it.uniroma2.tutorlink.model.TimeSlot;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

// Uno slot, come lo scrive il tutor e come lo vede lo studente.
public class AvailabilityBean extends AbstractBean {
    private long id;
    private String tutorEmail;
    private String tutorName;
    private String date;
    private String time;
    private String minutes;
    private boolean reserved;
    private String price;

    public AvailabilityBean() {
        super();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTutorEmail() {
        return tutorEmail;
    }

    public void setTutorEmail(String tutorEmail) {
        this.tutorEmail = tutorEmail;
    }

    public String getTutorName() {
        return tutorName;
    }

    public void setTutorName(String tutorName) {
        this.tutorName = tutorName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMinutes() {
        return minutes;
    }

    public void setMinutes(String minutes) {
        this.minutes = minutes;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public TimeSlot toTimeSlot() throws ValidationException {
        LocalDate parsedDate = requireDate(date, "date");
        LocalTime parsedTime = requireTime(time, "time");
        int parsedMinutes = requireIntInRange(minutes, "duration",
                TimeSlot.MIN_MINUTES, TimeSlot.MAX_MINUTES);
        if (parsedMinutes % TimeSlot.MIN_MINUTES != 0) {
            throw new ValidationException("duration",
                    "the duration must be a multiple of " + TimeSlot.MIN_MINUTES + " minutes");
        }
        return new TimeSlot(LocalDateTime.of(parsedDate, parsedTime), parsedMinutes);
    }

    @Override
    public void validateSyntax() throws ValidationException {
        requireDate(date, "date");
        requireTime(time, "time");
        requireIntInRange(minutes, "duration", TimeSlot.MIN_MINUTES, TimeSlot.MAX_MINUTES);
    }

    @Override
    public String toString() {
        return date + " " + time + " (" + minutes + " min)" + (price == null ? "" : " - " + price);
    }
}
