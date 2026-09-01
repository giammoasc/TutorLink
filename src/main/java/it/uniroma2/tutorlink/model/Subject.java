package it.uniroma2.tutorlink.model;

import java.util.Locale;

public enum Subject {
    MATHEMATICS("Mathematics"),
    PHYSICS("Physics"),
    COMPUTER_SCIENCE("Computer Science"),
    CHEMISTRY("Chemistry"),
    ECONOMICS("Economics"),
    ENGLISH("English");

    private final String displayName;

    Subject(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static Subject fromDisplayName(String name) {
        String normalised = name == null ? "" : name.trim().toLowerCase(Locale.ROOT);
        for (Subject subject : values()) {
            if (subject.displayName.toLowerCase(Locale.ROOT).equals(normalised)
                    || subject.name().toLowerCase(Locale.ROOT).equals(normalised)) {
                return subject;
            }
        }
        throw new IllegalArgumentException("unknown subject: " + name);
    }
}
