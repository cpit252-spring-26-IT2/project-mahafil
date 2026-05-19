package sa.edu.kau.fcit.cpit252.project.auth;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public class BookingRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String userEmail;
    private String details;
    private final Instant timestamp;
    private final String referenceNumber;
    private String status;

    public BookingRecord(String userEmail, String details) {
        this.userEmail = userEmail;
        this.details = details;
        this.timestamp= Instant.now();
        this.referenceNumber = generateReferenceNumber();
        this.status = "Active";
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getDetails() {
        return details;
    }

    public void updateDetails(String details) {
        this.details = details;
        this.status = "Updated";
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getReferenceNumber() {
        if (referenceNumber == null) {
            return "MAH-LEGACY";
        }
        return referenceNumber;
    }

    public String getStatus() {
        if (status == null) {
            return "Active";
        }
        return status;
    }

    public boolean isCancelled() {
        return getStatus().equalsIgnoreCase("Cancelled");
    }

    public void cancel() {
        this.status = "Cancelled";
    }

    private String generateReferenceNumber() {
        return "MAH-" + UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }

    @Override
    public String toString() {
        return getReferenceNumber() + " (" + getStatus() + ")";
    }
}
