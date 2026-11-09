package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public class TrackingIdGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * Generates a unique tracking ID conforming to standard:
     * Example: DLM2026100001
     */
    public static String generateTrackingId() {
        String yearMonth = LocalDate.now().format(FORMATTER);
        int randomSeq = ThreadLocalRandom.current().nextInt(10000, 99999);
        return String.format("DLM%s%05d", yearMonth, randomSeq);
    }
}
