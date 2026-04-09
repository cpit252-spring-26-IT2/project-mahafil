package booking;

public class BookingFactory {
    public static Booking getBooking(String type) {
        if (type.equalsIgnoreCase("venue")) {
            return new VenueBooking();
        } else if (type.equalsIgnoreCase("workspace")) {
            return new WorkspaceBooking();
        }
        return null;
    }
}