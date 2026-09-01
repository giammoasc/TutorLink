package it.uniroma2.tutorlink.dao;

import it.uniroma2.tutorlink.config.AppConfig;
import it.uniroma2.tutorlink.dao.cache.DaoCacheInvalidator;
import it.uniroma2.tutorlink.dao.cache.Invalidatable;
import it.uniroma2.tutorlink.dao.filesystem.FileSystemDaoFactory;
import it.uniroma2.tutorlink.dao.jdbc.JdbcDaoFactory;
import it.uniroma2.tutorlink.dao.memory.InMemoryDaoFactory;
import it.uniroma2.tutorlink.exception.PersistenceException;
import it.uniroma2.tutorlink.observer.AvailabilityPublisher;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

// Sceglie la famiglia di DAO in base alla configurazione.
public final class DaoFactoryProvider {
    private static final Logger LOGGER = Logger.getLogger(DaoFactoryProvider.class.getName());

    private DaoFactory factory;

    private DaoFactoryProvider() {
        this.factory = build();
    }

    private static final class Holder {
        private static final DaoFactoryProvider INSTANCE = new DaoFactoryProvider();

        private Holder() {
            // costruttore privato: la classe non va istanziata
        }
    }

    public static DaoFactoryProvider getInstance() {
        return Holder.INSTANCE;
    }

    public DaoFactory factory() {
        return factory;
    }



    private static DaoFactory build() {
        AppConfig config = AppConfig.getInstance();
        return switch (config.getPersistenceProvider()) {
            case FILE_SYSTEM -> buildFileSystem();
            case DBMS -> buildJdbc();
            case MEMORY -> new InMemoryDaoFactory();
        };
    }

    private static DaoFactory buildJdbc() {
        JdbcDaoFactory jdbcFactory = new JdbcDaoFactory();
        return registerCaches(jdbcFactory, jdbcFactory.caches());
    }

    private static DaoFactory buildFileSystem() {
        try {
            FileSystemDaoFactory fileSystemFactory = new FileSystemDaoFactory();
            return registerCaches(fileSystemFactory, fileSystemFactory.caches());
        } catch (PersistenceException e) {
            // cartella dati non usabile: parto lo stesso, tenendo tutto in memoria
            LOGGER.log(Level.SEVERE, e,
                    () -> "the file system storage is not usable, falling back to the in-memory family");
            return new InMemoryDaoFactory();
        }
    }

    private static DaoFactory registerCaches(DaoFactory built, List<Invalidatable> caches) {
        AvailabilityPublisher.getInstance().attach(new DaoCacheInvalidator(caches));
        return built;
    }
}
