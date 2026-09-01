package it.uniroma2.tutorlink.control;

import it.uniroma2.tutorlink.bean.ProgressPointBean;
import it.uniroma2.tutorlink.bean.ProgressSummaryBean;
import it.uniroma2.tutorlink.dao.DaoFactory;
import it.uniroma2.tutorlink.exception.AuthenticationException;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.model.Student;
import it.uniroma2.tutorlink.model.Subject;
import it.uniroma2.tutorlink.model.progress.ProgressReport;
import java.util.List;
import java.util.Locale;

public class ProgressController extends AbstractApplicationController {
    public ProgressController() {
        super();
    }

    public ProgressController(DaoFactory daoFactory) {
        super(daoFactory);
    }

    public ProgressSummaryBean myProgress() throws AuthenticationException, PersistenceException {
        Student student = session().requireStudent();
        daos().createLessonDao().findByStudent(student);
        return summarise(student.progressReport());
    }

    private static ProgressSummaryBean summarise(ProgressReport report) {
        ProgressSummaryBean bean = new ProgressSummaryBean();
        bean.setAverage(String.format(Locale.ROOT, "%.1f", report.average()));
        bean.setTrend(describeTrend(report));
        bean.setImprovement(String.format(Locale.ROOT, "%+.1f%%", report.improvementPercentage()));
        bean.setBestSubject(report.bestSubject().map(Subject::displayName).orElse("-"));
        bean.setWeakestSubject(report.weakestSubject().map(Subject::displayName).orElse("-"));
        bean.setSuggestedTimeBand(report.bestTimeBand()
                .map(band -> String.format("%02d:00 - %02d:00", band, band + 3))
                .orElse("-"));
        List<ProgressPointBean> points = report.points().stream().map(BeanMapper::toBean).toList();
        bean.setPoints(points);
        return bean;
    }

    private static String describeTrend(ProgressReport report) {
        if (report.size() < 2) {
            return "not enough lessons yet";
        }
        double slope = report.trendSlope();
        if (slope > 0.05) {
            return String.format(Locale.ROOT, "improving (%+.2f per lesson)", slope);
        }
        if (slope < -0.05) {
            return String.format(Locale.ROOT, "declining (%+.2f per lesson)", slope);
        }
        return "stable";
    }
}
