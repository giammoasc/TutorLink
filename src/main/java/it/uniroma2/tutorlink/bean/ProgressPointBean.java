package it.uniroma2.tutorlink.bean;

public class ProgressPointBean extends AbstractBean {
    private String date;
    private String subject;
    private int score;
    private String tutorName;

    public ProgressPointBean() {
        super();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getTutorName() {
        return tutorName;
    }

    public void setTutorName(String tutorName) {
        this.tutorName = tutorName;
    }

    @Override
    public void validateSyntax() {
        // bean di sola uscita: non c’è niente da controllare
    }
}
