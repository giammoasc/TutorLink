package it.uniroma2.tutorlink.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

// Configurazione: modalita' demo o full, dove salvare i dati e quale layout usare.
public final class AppConfig {
    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final String CONFIG_FILE = "/config.properties";

    private boolean dataPermanence;
    private PersistenceProvider persistenceProvider;
    private GuiLayout guiLayout;
    private String filesystemRoot;
    private String jdbcUrl;
    private String jdbcUser;
    private String jdbcPassword;
    private long materialQuotaBytes;

    private AppConfig() {
        Properties properties = loadProperties();
        this.dataPermanence = Boolean.parseBoolean(properties.getProperty("data.permanence", "false"));
        this.persistenceProvider = readProvider(properties.getProperty("persistence.provider", "MEMORY"));
        this.guiLayout = readLayout(properties.getProperty("gui.layout", "DESKTOP"));
        this.filesystemRoot = properties.getProperty("filesystem.root", "data");
        this.jdbcUrl = properties.getProperty("jdbc.url", "");
        this.jdbcUser = properties.getProperty("jdbc.user", "");
        this.jdbcPassword = properties.getProperty("jdbc.password", "");
        this.materialQuotaBytes = readLong(properties.getProperty("material.quota.bytes"), 20L * 1024 * 1024);
        reconcile();
    }

    private static final class Holder {
        private static final AppConfig INSTANCE = new AppConfig();

        private Holder() {
            // costruttore privato: la classe non va istanziata
        }
    }

    public static AppConfig getInstance() {
        return Holder.INSTANCE;
    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        try (InputStream stream = AppConfig.class.getResourceAsStream(CONFIG_FILE)) {
            if (stream == null) {
                LOGGER.warning("config.properties not found on the classpath, falling back to the defaults");
                return properties;
            }
            properties.load(stream);
        } catch (IOException e) {
            // senza file di configurazione uso i valori di default
            LOGGER.log(Level.WARNING, "config.properties could not be read, falling back to the defaults", e);
        }
        return properties;
    }

    private static PersistenceProvider readProvider(String raw) {
        try {
            return PersistenceProvider.valueOf(raw.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "unknown persistence provider ''{0}'', using MEMORY", raw);
            return PersistenceProvider.MEMORY;
        }
    }

    private static GuiLayout readLayout(String raw) {
        try {
            return GuiLayout.valueOf(raw.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "unknown gui layout ''{0}'', using DESKTOP", raw);
            return GuiLayout.DESKTOP;
        }
    }

    private static long readLong(String raw, long fallback) {
        if (raw == null) {
            return fallback;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public void applyCommandLine(String... arguments) {
        for (String argument : arguments) {
            applySingleArgument(argument.trim().toLowerCase(java.util.Locale.ROOT));
        }
        reconcile();
    }

    private void applySingleArgument(String argument) {
        switch (argument) {
            case "--demo" -> this.dataPermanence = false;
            case "--full" -> this.dataPermanence = true;
            case "--memory" -> this.persistenceProvider = PersistenceProvider.MEMORY;
            case "--file" -> this.persistenceProvider = PersistenceProvider.FILE_SYSTEM;
            case "--dbms" -> this.persistenceProvider = PersistenceProvider.DBMS;
            case "--desktop" -> this.guiLayout = GuiLayout.DESKTOP;
            case "--compact" -> this.guiLayout = GuiLayout.COMPACT;
            default -> LOGGER.log(Level.INFO, "argument ignored: {0}", argument);
        }
    }

    // demo e persistenza sono legate: non possono restare in disaccordo
    private void reconcile() {
        if (!dataPermanence && persistenceProvider != PersistenceProvider.MEMORY) {
            LOGGER.info("demo-version requested: the persistence provider is forced to MEMORY");
            persistenceProvider = PersistenceProvider.MEMORY;
        }
        if (dataPermanence && persistenceProvider == PersistenceProvider.MEMORY) {
            LOGGER.info("full-version requested without a provider: FILE_SYSTEM is assumed");
            persistenceProvider = PersistenceProvider.FILE_SYSTEM;
        }
    }


    public PersistenceProvider getPersistenceProvider() {
        return persistenceProvider;
    }

    public GuiLayout getGuiLayout() {
        return guiLayout;
    }

    public String getFilesystemRoot() {
        return filesystemRoot;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getJdbcUser() {
        return jdbcUser;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    public long getMaterialQuotaBytes() {
        return materialQuotaBytes;
    }

    public String describe() {
        return String.format("%s / %s / %s layout",
                dataPermanence ? "full-version" : "demo-version",
                persistenceProvider, guiLayout);
    }
}
