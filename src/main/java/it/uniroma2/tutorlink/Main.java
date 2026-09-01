package it.uniroma2.tutorlink;

import it.uniroma2.tutorlink.boundary.common.Navigator;
import it.uniroma2.tutorlink.boundary.common.ViewFactory;
import it.uniroma2.tutorlink.boundary.compact.CompactViewFactory;
import it.uniroma2.tutorlink.boundary.desktop.DesktopViewFactory;
import it.uniroma2.tutorlink.bootstrap.ApplicationBootstrap;
import it.uniroma2.tutorlink.config.AppConfig;
import it.uniroma2.tutorlink.config.GuiLayout;
import javafx.application.Application;
import javafx.stage.Stage;

// Avvio dell'applicazione: sceglie il layout dalla configurazione e apre il login.
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        ApplicationBootstrap.start();
        ViewFactory viewFactory = AppConfig.getInstance().getGuiLayout() == GuiLayout.COMPACT
                ? new CompactViewFactory()
                : new DesktopViewFactory();
        Navigator navigator = new Navigator(primaryStage, viewFactory);
        navigator.showLogin();
    }

    public static void main(String[] args) {
        AppConfig.getInstance().applyCommandLine(args);
        launch(args);
    }
}
