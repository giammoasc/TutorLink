package it.uniroma2.tutorlink.exception;

// Slot non piu' prenotabile. Porta l'id, cosi' si aggiorna solo quel pezzo di calendario.
public class SlotUnavailableException extends TutorLinkException {
    private static final long serialVersionUID = 1L;

    private final long slotId;

    public SlotUnavailableException(String message, long slotId) {
        super(message);
        this.slotId = slotId;
    }

    public long getSlotId() {
        return slotId;
    }
}
