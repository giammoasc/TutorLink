package it.uniroma2.tutorlink.bean;

public class LessonBean extends AbstractBean {
    private long id;
    private String subject;
    private String tutorName;
    private String studentName;
    private String start;
    private int minutes;
    private String price;
    private String state;
    private String meetingLink;
    private int materialCount;
    private Integer score;

    public LessonBean() {
        super();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTutorName() {
        return tutorName;
    }

    public void setTutorName(String tutorName) {
        this.tutorName = tutorName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public int getMaterialCount() {
        return materialCount;
    }

    public void setMaterialCount(int materialCount) {
        this.materialCount = materialCount;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    @Override
    public void validateSyntax() {
        // bean di sola uscita: non c’è niente da controllare
    }

    @Override
    public String toString() {
        return start + " - " + subject + " with " + tutorName + " [" + state + "]";
    }
}
