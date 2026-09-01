package it.uniroma2.tutorlink.bean;

import java.util.ArrayList;
import java.util.List;

public class ProgressSummaryBean extends AbstractBean {
    private String average;
    private String trend;
    private String improvement;
    private String bestSubject;
    private String weakestSubject;
    private String suggestedTimeBand;
    private List<ProgressPointBean> points = new ArrayList<>();

    public ProgressSummaryBean() {
        super();
    }

    public String getAverage() {
        return average;
    }

    public void setAverage(String average) {
        this.average = average;
    }

    public String getTrend() {
        return trend;
    }

    public void setTrend(String trend) {
        this.trend = trend;
    }

    public String getImprovement() {
        return improvement;
    }

    public void setImprovement(String improvement) {
        this.improvement = improvement;
    }

    public String getBestSubject() {
        return bestSubject;
    }

    public void setBestSubject(String bestSubject) {
        this.bestSubject = bestSubject;
    }

    public String getWeakestSubject() {
        return weakestSubject;
    }

    public void setWeakestSubject(String weakestSubject) {
        this.weakestSubject = weakestSubject;
    }

    public String getSuggestedTimeBand() {
        return suggestedTimeBand;
    }

    public void setSuggestedTimeBand(String suggestedTimeBand) {
        this.suggestedTimeBand = suggestedTimeBand;
    }

    public List<ProgressPointBean> getPoints() {
        return List.copyOf(points);
    }

    public void setPoints(List<ProgressPointBean> points) {
        this.points = points == null ? new ArrayList<>() : new ArrayList<>(points);
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }

    @Override
    public void validateSyntax() {
        // bean di sola uscita: non c’è niente da controllare
    }
}
