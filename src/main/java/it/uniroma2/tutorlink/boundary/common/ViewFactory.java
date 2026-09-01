package it.uniroma2.tutorlink.boundary.common;

// Crea le schermate di un layout.
public interface ViewFactory {
    String layoutName();

    View loginView(Navigator navigator);

    View registrationView(Navigator navigator);

    View studentHomeView(Navigator navigator);

    View tutorHomeView(Navigator navigator);
}
