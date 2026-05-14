package sa.edu.kau.fcit.cpit252.project.booking;

public class RecommendedBooking {
    private final Booking booking;
    private final String bookingType;
    private final String reason;

    public RecommendedBooking(Booking booking, String bookingType, String reason) {
        this.booking = booking;
        this.bookingType = bookingType;
        this.reason = reason;
    }

    public Booking getBooking() {
        return booking;
    }

    public String getBookingType() {
        return bookingType;
    }

    public String getReason() {
        return reason;
    }

    public String getSummary() {
        return String.format(
                "%s \nRecommended type: %s  Total: %.2f SAR \nReason: %s",
                booking.createBooking(),
                bookingType,
                booking.getTotalPrice(),
                reason
        );
    }
}
