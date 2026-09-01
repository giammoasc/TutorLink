package it.uniroma2.tutorlink.bean;

import it.uniroma2.tutorlink.exception.ValidationException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RegistrationBean extends AbstractBean {
    private static final int MIN_PASSWORD_LENGTH = 8;

    private String email;
    private String fullName;
    private char[] password;
    private char[] confirmPassword;
    private String role;
    private String hourlyRate;
    private List<String> subjects = new ArrayList<>();

    public RegistrationBean() {
        super();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public char[] getPassword() {
        return password == null ? new char[0] : password.clone();
    }

    public void setPassword(char[] password) {
        this.password = password == null ? null : password.clone();
    }

    public void setConfirmPassword(char[] confirmPassword) {
        this.confirmPassword = confirmPassword == null ? null : confirmPassword.clone();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(String hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public List<String> getSubjects() {
        return List.copyOf(subjects);
    }

    public void setSubjects(List<String> subjects) {
        this.subjects = subjects == null ? new ArrayList<>() : new ArrayList<>(subjects);
    }

    public boolean isTutor() {
        return "TUTOR".equalsIgnoreCase(role);
    }

    public void clearPasswords() {
        if (password != null) {
            Arrays.fill(password, '\0');
        }
        if (confirmPassword != null) {
            Arrays.fill(confirmPassword, '\0');
        }
    }

    @Override
    public void validateSyntax() throws ValidationException {
        this.email = requireEmail(email, "email");
        this.fullName = requireText(fullName, "full name");
        this.role = requireText(role, "role").toUpperCase(java.util.Locale.ROOT);
        if (!"STUDENT".equals(role) && !"TUTOR".equals(role)) {
            throw new ValidationException("role", "the role must be STUDENT or TUTOR");
        }
        if (password == null || password.length < MIN_PASSWORD_LENGTH) {
            throw new ValidationException("password",
                    "the password must be at least " + MIN_PASSWORD_LENGTH + " characters long");
        }
        if (!Arrays.equals(password, confirmPassword)) {
            throw new ValidationException("confirmPassword", "the two passwords do not match");
        }
        if (isTutor()) {
            requirePositiveDecimal(hourlyRate, "hourly rate");
            if (subjects.isEmpty()) {
                throw new ValidationException("subjects", "a tutor has to declare at least one subject");
            }
        }
    }
}
