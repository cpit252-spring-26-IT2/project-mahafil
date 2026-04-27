package sa.edu.kau.fcit.cpit252.project.booking;

public class VIPDecorator extends BookingDecorator {

    public VIPDecorator(Booking booking) {
        super(booking);
    }

    @Override
    public String createBooking() {
        return booking.createBooking() + " + VIP Service";
    }
}