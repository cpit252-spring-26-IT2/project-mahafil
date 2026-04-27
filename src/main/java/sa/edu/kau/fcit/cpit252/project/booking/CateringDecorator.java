package sa.edu.kau.fcit.cpit252.project.booking;

public class CateringDecorator extends BookingDecorator {

    public CateringDecorator(Booking booking) {
        super(booking);
    }

    @Override
    public String createBooking() {
        return booking.createBooking() + " + Catering";
    }
}