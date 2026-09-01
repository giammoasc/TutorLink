package it.uniroma2.tutorlink.exception;

// Spazio esaurito per la lezione. Porta i byte ancora liberi.
public class MaterialQuotaExceededException extends MaterialRejectedException {
    private static final long serialVersionUID = 1L;

    private final long residualBytes;

    public MaterialQuotaExceededException(String message, long residualBytes) {
        super(message);
        this.residualBytes = residualBytes;
    }

    public long getResidualBytes() {
        return residualBytes;
    }
}
