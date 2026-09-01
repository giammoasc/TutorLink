package it.uniroma2.tutorlink.util;

// Contatore degli id usati dal livello di persistenza.
public final class IdGenerator {
    private static long counter = System.currentTimeMillis();

    private IdGenerator() {
        // costruttore privato: la classe non va istanziata
    }

    public static long next() {
        counter = counter + 1;
        return counter;
    }

    public static void observe(long existingId) {
        if (existingId > counter) {
            counter = existingId;
        }
    }
}
