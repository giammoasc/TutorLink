package it.uniroma2.tutorlink.boundary.compact;

import it.uniroma2.tutorlink.boundary.common.Navigator;
import it.uniroma2.tutorlink.boundary.common.View;
import it.uniroma2.tutorlink.boundary.common.ViewFactory;

public class CompactViewFactory implements ViewFactory {
    @Override
    public String layoutName() {
        return "compact";
    }

    @Override
    public View loginView(Navigator navigator) {
        return new CompactLoginGraphicController(navigator);
    }

    @Override
    public View registrationView(Navigator navigator) {
        return new CompactRegistrationGraphicController(navigator);
    }

    @Override
    public View studentHomeView(Navigator navigator) {
        return new CompactStudentGraphicController(navigator);
    }

    @Override
    public View tutorHomeView(Navigator navigator) {
        return new CompactTutorGraphicController(navigator);
    }
}
