package sa.edu.kau.fcit.cpit252.project.booking;

public class VIPDecorator extends BookingDecorator {
    private static final double VIP_PRICE = 750.0;

    public VIPDecorator(Booking booking) {
        super(booking);
    }

    @Override
    public String createBooking() {
        return booking.createBooking() + " + VIP Service";
    }

    @Override
    public double getTotalPrice() {
        return booking.getTotalPrice() + VIP_PRICE;
    }
}
