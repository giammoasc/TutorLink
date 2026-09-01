package it.uniroma2.tutorlink.control;

import it.uniroma2.tutorlink.dao.DaoFactory;
import it.uniroma2.tutorlink.dao.DaoFactoryProvider;
import it.uniroma2.tutorlink.session.SessionManager;

// Base dei controller applicativi: accesso ai DAO e alla sessione.
public abstract class AbstractApplicationController {
    private final DaoFactory daoFactory;

    protected AbstractApplicationController() {
        this(DaoFactoryProvider.getInstance().factory());
    }

    protected AbstractApplicationController(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
    }

    protected DaoFactory daos() {
        return daoFactory;
    }

    protected SessionManager session() {
        return SessionManager.getInstance();
    }
}
