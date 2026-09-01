package it.uniroma2.tutorlink.bean;

import it.uniroma2.tutorlink.exception.ValidationException;
import it.uniroma2.tutorlink.model.Feedback;

public class FeedbackBean extends AbstractBean {
    private static final int MAX_COMMENT_LENGTH = 500;

    private long lessonId;
    private String score;
    private String comment;

    public FeedbackBean() {
        super();
    }

    public long getLessonId() {
        return lessonId;
    }

    public void setLessonId(long lessonId) {
        this.lessonId = lessonId;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int parsedScore() throws ValidationException {
        return requireIntInRange(score, "score", Feedback.MIN_SCORE, Feedback.MAX_SCORE);
    }

    @Override
    public void validateSyntax() throws ValidationException {
        if (lessonId <= 0) {
            throw new ValidationException("lessonId", "no lesson has been selected");
        }
        requireIntInRange(score, "score", Feedback.MIN_SCORE, Feedback.MAX_SCORE);
        if (comment != null && comment.length() > MAX_COMMENT_LENGTH) {
            throw new ValidationException("comment",
                    "the comment cannot be longer than " + MAX_COMMENT_LENGTH + " characters");
        }
    }
}
