package sa.edu.kau.fcit.cpit252.project.auth;

import java.io.Serializable;
import java.time.Instant;

public class BookingRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String userEmail;
    private final String details;
    private final Instant timestamp;

    public BookingRecord(String userEmail, String details) {
        this.userEmail = userEmail;
        this.details = details;
        this.timestamp = Instant.now();
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getDetails() {
        return details;
    }

    public Instant getTimestamp() {
        return timestamp;
    }
}
