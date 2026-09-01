package it.uniroma2.tutorlink.bean;

import it.uniroma2.tutorlink.exception.ValidationException;

public class MaterialBean extends AbstractBean {
    private static final int MAX_TITLE_LENGTH = 120;

    private long id;
    private long lessonId;
    private String title;
    private String fileName;
    private String sourcePath;
    private long sizeBytes;
    private String status;
    private String publishedAt;

    public MaterialBean() {
        super();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLessonId() {
        return lessonId;
    }

    public void setLessonId(long lessonId) {
        this.lessonId = lessonId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String readableSize() {
        return sizeBytes < 1024 ? sizeBytes + " B" : (sizeBytes / 1024) + " KB";
    }

    @Override
    public void validateSyntax() throws ValidationException {
        if (lessonId <= 0) {
            throw new ValidationException("lessonId", "no lesson has been selected");
        }
        this.title = requireText(title, "title");
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new ValidationException("title",
                    "the title cannot be longer than " + MAX_TITLE_LENGTH + " characters");
        }
        this.fileName = requireText(fileName, "file name");
        if (!fileName.contains(".")) {
            throw new ValidationException("fileName", "the file name must carry an extension");
        }
        if (sizeBytes <= 0) {
            throw new ValidationException("sizeBytes", "the selected file is empty");
        }
    }

    @Override
    public String toString() {
        return title + " (" + fileName + ", " + readableSize() + ", " + status + ")";
    }
}
