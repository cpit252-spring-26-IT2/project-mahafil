package sa.edu.kau.fcit.cpit252.project.booking;

public class RecommendedBooking {
    private final Booking booking;
    private final String bookingType;
    private final String reason;
    private final int guestCount;

    public RecommendedBooking(Booking booking, String bookingType, String reason, int guestCount) {
        this.booking = booking;
        this.bookingType = bookingType;
        this.reason = reason;
        this.guestCount = guestCount;
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

    public int getGuestCount() {
        return guestCount;
    }

    public double getPricePerPerson() {
        return booking.getTotalPrice() / guestCount;
    }

    public String getSummary() {
        return String.format(
                "Recommended type: %s%nPrice per person: %.2f SAR%nTotal: %.2f SAR%nReason: %s",
                bookingType,
                getPricePerPerson(),
                booking.getTotalPrice(),
                reason
        );
    }
}
