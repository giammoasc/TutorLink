package it.uniroma2.tutorlink.exception;

import java.util.List;

// Estensione non ammessa. Porta l'elenco dei formati validi.
public class UnsupportedMaterialFormatException extends MaterialRejectedException {
    private static final long serialVersionUID = 1L;

    // List non e' Serializable, quindi il campo resta fuori dalla serializzazione
    private final transient List<String> acceptedFormats;

    public UnsupportedMaterialFormatException(String message, List<String> acceptedFormats) {
        super(message);
        this.acceptedFormats = List.copyOf(acceptedFormats);
    }

    public List<String> getAcceptedFormats() {
        return acceptedFormats;
    }
}
