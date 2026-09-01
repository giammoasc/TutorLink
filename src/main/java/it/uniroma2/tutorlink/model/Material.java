package it.uniroma2.tutorlink.model;

import it.uniroma2.tutorlink.exception.UnsupportedMaterialFormatException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

// File condiviso dal tutor. Controlla da solo il formato.
public final class Material {
    // formati che il tutor puo' condividere
    public static final List<String> ACCEPTED_FORMATS =
            List.of("pdf", "docx", "pptx", "txt", "md", "png", "jpg", "zip");

    private final long id;
    private final Lesson lesson;
    private final String title;
    private final String fileName;
    private final long sizeBytes;
    private MaterialStatus status;
    private LocalDateTime publishedAt;

    public Material(long id, Lesson lesson, String title, String fileName, long sizeBytes)
            throws UnsupportedMaterialFormatException {
        this.id = id;
        this.lesson = Objects.requireNonNull(lesson, "lesson");
        this.title = requireText(title, "title");
        this.fileName = requireText(fileName, "fileName");
        if (sizeBytes <= 0) {
            throw new IllegalArgumentException("the size of a material must be positive");
        }
        this.sizeBytes = sizeBytes;
        this.status = MaterialStatus.DRAFT;
        validateFormat();
    }

    public Material(long id, Lesson lesson, String title, String fileName, long sizeBytes,
                    MaterialStatus status, LocalDateTime publishedAt)
            throws UnsupportedMaterialFormatException {
        this(id, lesson, title, fileName, sizeBytes);
        this.status = Objects.requireNonNull(status, "status");
        this.publishedAt = publishedAt;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be empty");
        }
        return value.trim();
    }

    private void validateFormat() throws UnsupportedMaterialFormatException {
        String extension = extension();
        if (!ACCEPTED_FORMATS.contains(extension)) {
            throw new UnsupportedMaterialFormatException(
                    "the format ." + extension + " cannot be shared on TutorLink", ACCEPTED_FORMATS);
        }
    }

    public String extension() {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    public long id() {
        return id;
    }

    public Lesson lesson() {
        return lesson;
    }

    public String title() {
        return title;
    }

    public String fileName() {
        return fileName;
    }

    public long sizeBytes() {
        return sizeBytes;
    }

    public MaterialStatus status() {
        return status;
    }

    public LocalDateTime publishedAt() {
        return publishedAt;
    }

    public boolean isDraft() {
        return status == MaterialStatus.DRAFT;
    }

    public boolean isPublished() {
        return status == MaterialStatus.PUBLISHED;
    }

    public void publish(LocalDateTime when) {
        if (status == MaterialStatus.DRAFT) {
            this.status = MaterialStatus.PUBLISHED;
            this.publishedAt = Objects.requireNonNull(when, "when");
        }
    }

    public void withdraw() {
        this.status = MaterialStatus.WITHDRAWN;
    }

    public boolean isVisibleToStudent() {
        return status == MaterialStatus.PUBLISHED;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Material)) {
            return false;
        }
        Material material = (Material) other;
        return id == material.id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public String toString() {
        return title + " (" + fileName + ", " + sizeBytes / 1024 + " KB, " + status + ")";
    }
}
