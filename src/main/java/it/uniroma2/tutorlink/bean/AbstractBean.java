package it.uniroma2.tutorlink.bean;

import it.uniroma2.tutorlink.exception.ValidationException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

// Controlli di forma comuni ai bean: campo pieno, email, data, numero.
public abstract class AbstractBean {
    protected static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    protected static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    protected AbstractBean() {
        // i bean hanno il costruttore vuoto
    }

    public abstract void validateSyntax() throws ValidationException;

    protected static String requireText(String value, String field) throws ValidationException {
        if (value == null || value.isBlank()) {
            throw new ValidationException(field, "the field '" + field + "' cannot be empty");
        }
        return value.trim();
    }

    protected static String requireEmail(String value, String field) throws ValidationException {
        String text = requireText(value, field);
        if (!EMAIL_PATTERN.matcher(text).matches()) {
            throw new ValidationException(field, "'" + text + "' is not a well formed e-mail address");
        }
        return text;
    }

    protected static int requireIntInRange(String value, String field, int min, int max)
            throws ValidationException {
        String text = requireText(value, field);
        int parsed;
        try {
            parsed = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new ValidationException(field, "'" + text + "' is not an integer number");
        }
        if (parsed < min || parsed > max) {
            throw new ValidationException(field,
                    "the field '" + field + "' must be between " + min + " and " + max);
        }
        return parsed;
    }

    protected static double requirePositiveDecimal(String value, String field) throws ValidationException {
        String text = requireText(value, field).replace(',', '.');
        double parsed;
        try {
            parsed = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new ValidationException(field, "'" + text + "' is not a decimal number");
        }
        if (parsed <= 0) {
            throw new ValidationException(field, "the field '" + field + "' must be positive");
        }
        return parsed;
    }

    protected static LocalDate requireDate(String value, String field) throws ValidationException {
        String text = requireText(value, field);
        try {
            return LocalDate.parse(text, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException(field, "'" + text + "' is not a date in the format dd/MM/yyyy");
        }
    }

    protected static LocalTime requireTime(String value, String field) throws ValidationException {
        String text = requireText(value, field);
        try {
            return LocalTime.parse(text, TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException(field, "'" + text + "' is not a time in the format HH:mm");
        }
    }
}
