package sa.edu.kau.fcit.cpit252.project.booking;

public abstract class BookingDecorator implements Booking {
    protected final Booking booking;

    public BookingDecorator(Booking booking) {
        this.booking = booking;
    }

    @Override
    public String createBooking() {
        return booking.createBooking();
    }

    @Override
    public double getTotalPrice() {
        return booking.getTotalPrice();
    }
}
