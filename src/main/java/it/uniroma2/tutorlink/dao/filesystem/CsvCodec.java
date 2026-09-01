package it.uniroma2.tutorlink.dao.filesystem;

import java.util.ArrayList;
import java.util.List;

// Legge e scrive una riga CSV, con virgolette e separatori.
final class CsvCodec {
    private static final char SEPARATOR = ';';
    private static final char QUOTE = '"';

    private CsvCodec() {
        // costruttore privato: la classe non va istanziata
    }

    static String encode(String... fields) {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                line.append(SEPARATOR);
            }
            line.append(encodeField(fields[i]));
        }
        return line.toString();
    }

    private static String encodeField(String field) {
        String value = field == null ? "" : field;
        boolean mustQuote = value.indexOf(SEPARATOR) >= 0
                || value.indexOf(QUOTE) >= 0
                || value.indexOf('\n') >= 0;
        if (!mustQuote) {
            return value;
        }
        return QUOTE + value.replace(String.valueOf(QUOTE), "\"\"") + QUOTE;
    }

    static String[] decode(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        int i = 0;
        while (i < line.length()) {
            char character = line.charAt(i);
            if (inQuotes) {
                if (character == QUOTE) {
                    if (i + 1 < line.length() && line.charAt(i + 1) == QUOTE) {
                        current.append(QUOTE);
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(character);
                }
            } else if (character == QUOTE) {
                inQuotes = true;
            } else if (character == SEPARATOR) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(character);
            }
            i++;
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }
}
