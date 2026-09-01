package it.uniroma2.tutorlink.bean;

public class TutorBean extends AbstractBean {
    private String email;
    private String fullName;
    private String hourlyRate;
    private String subjects;
    private String matchingReason;
    private int deliveredLessons;
    private int rankingPosition;

    public TutorBean() {
        super();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(String hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public String getSubjects() {
        return subjects;
    }

    public void setSubjects(String subjects) {
        this.subjects = subjects;
    }

    public String getMatchingReason() {
        return matchingReason;
    }

    public void setMatchingReason(String matchingReason) {
        this.matchingReason = matchingReason;
    }

    public int getDeliveredLessons() {
        return deliveredLessons;
    }

    public void setDeliveredLessons(int deliveredLessons) {
        this.deliveredLessons = deliveredLessons;
    }

    public int getRankingPosition() {
        return rankingPosition;
    }

    public void setRankingPosition(int rankingPosition) {
        this.rankingPosition = rankingPosition;
    }

    @Override
    public void validateSyntax() {
        // bean di sola uscita: non c’è niente da controllare
    }

    @Override
    public String toString() {
        return rankingPosition + ". " + fullName + " - " + hourlyRate + "/h - " + matchingReason;
    }
}
