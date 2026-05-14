package sa.edu.kau.fcit.cpit252.project.booking;

public class CateringDecorator extends BookingDecorator {
    private static final double CATERING_PRICE = 1500.0;

    public CateringDecorator(Booking booking) {
        super(booking);
    }

    @Override
    public String createBooking() {
        return booking.createBooking() + " + Catering";
    }

    @Override
    public double getTotalPrice() {
        return booking.getTotalPrice() + CATERING_PRICE;
    }
}
