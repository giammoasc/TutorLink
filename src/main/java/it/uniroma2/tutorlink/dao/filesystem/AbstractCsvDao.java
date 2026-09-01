package it.uniroma2.tutorlink.dao.filesystem;

import it.uniroma2.tutorlink.exception.PersistenceException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// Lettura, scrittura e aggiornamento di una tabella CSV.
public abstract class AbstractCsvDao<T> {
    private final Path file;

    protected AbstractCsvDao(Path root, String fileName) throws PersistenceException {
        this.file = root.resolve(fileName);
        prepareStorage(root);
    }

    private void prepareStorage(Path root) throws PersistenceException {
        try {
            Files.createDirectories(root);
            if (!Files.exists(file)) {
                Files.createFile(file);
            }
        } catch (IOException e) {
            throw new PersistenceException("the file " + file + " cannot be prepared", e);
        }
    }

    // da oggetto a riga
    protected abstract String[] toRecord(T item);

    // da riga a oggetto
    protected abstract T fromRecord(String[] record) throws PersistenceException;

    // colonna che fa da chiave, serve per l'aggiornamento
    protected abstract String keyOf(String[] record);

    protected final List<String[]> readRecords() throws PersistenceException {
        List<String[]> records = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (!line.isBlank()) {
                    records.add(CsvCodec.decode(line));
                }
            }
        } catch (IOException e) {
            throw new PersistenceException("the file " + file + " cannot be read", e);
        }
        return records;
    }

    protected final void writeRecords(List<String[]> records) throws PersistenceException {
        List<String> lines = new ArrayList<>(records.size());
        for (String[] record : records) {
            lines.add(CsvCodec.encode(record));
        }
        try {
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new PersistenceException("the file " + file + " cannot be written", e);
        }
    }

    protected final void upsert(T item) throws PersistenceException {
        String[] incoming = toRecord(item);
        String key = keyOf(incoming);
        List<String[]> records = readRecords();
        boolean replaced = false;
        for (int i = 0; i < records.size(); i++) {
            if (keyOf(records.get(i)).equals(key)) {
                records.set(i, incoming);
                replaced = true;
                break;
            }
        }
        if (!replaced) {
            records.add(incoming);
        }
        writeRecords(records);
    }

    protected final List<T> loadAll() throws PersistenceException {
        List<T> items = new ArrayList<>();
        for (String[] record : readRecords()) {
            items.add(fromRecord(record));
        }
        return items;
    }

    protected final Path file() {
        return file;
    }
}
