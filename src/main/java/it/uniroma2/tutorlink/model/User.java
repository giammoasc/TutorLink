package it.uniroma2.tutorlink.model;

import it.uniroma2.tutorlink.util.PasswordHasher;
import java.util.Locale;
import java.util.Objects;

// Parte comune di studente e tutor. La password non si legge, si verifica.
public abstract class User {
    private final String email;
    private final String fullName;
    private String passwordDigest;

    protected User(String email, String fullName, String passwordDigest) {
        this.email = normaliseEmail(email);
        this.fullName = requireText(fullName, "fullName");
        this.passwordDigest = requireText(passwordDigest, "passwordDigest");
    }

    private static String normaliseEmail(String email) {
        String value = requireText(email, "email");
        return value.toLowerCase(Locale.ROOT);
    }

    protected static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be empty");
        }
        return value.trim();
    }

    public String email() {
        return email;
    }

    public String fullName() {
        return fullName;
    }

    public String passwordDigest() {
        return passwordDigest;
    }

    public boolean authenticate(char[] rawPassword) {
        return PasswordHasher.matches(rawPassword, passwordDigest);
    }


    public abstract UserRole role();

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof User)) {
            return false;
        }
        User user = (User) other;
        return email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return fullName + " <" + email + ">";
    }
}
