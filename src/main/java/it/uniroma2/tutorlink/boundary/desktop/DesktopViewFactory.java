package it.uniroma2.tutorlink.boundary.desktop;

import it.uniroma2.tutorlink.boundary.common.Navigator;
import it.uniroma2.tutorlink.boundary.common.View;
import it.uniroma2.tutorlink.boundary.common.ViewFactory;

public class DesktopViewFactory implements ViewFactory {
    @Override
    public String layoutName() {
        return "desktop";
    }

    @Override
    public View loginView(Navigator navigator) {
        return new DesktopLoginGraphicController(navigator);
    }

    @Override
    public View registrationView(Navigator navigator) {
        return new DesktopRegistrationGraphicController(navigator);
    }

    @Override
    public View studentHomeView(Navigator navigator) {
        return new DesktopStudentGraphicController(navigator);
    }

    @Override
    public View tutorHomeView(Navigator navigator) {
        return new DesktopTutorGraphicController(navigator);
    }
}
