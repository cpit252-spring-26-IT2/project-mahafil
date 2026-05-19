import booking.*;

public class Main {
    public static void main(String[] args) {

        Booking b1 = BookingFactory.getBooking("venue");
        b1.createBooking();

        Booking b2 = BookingFactory.getBooking("workspace");
        b2.createBooking();
    }
}