package sa.edu.kau.fcit.cpit252.project.booking;

public abstract class BookingDecorator implements Booking {
    protected Booking booking;

    public BookingDecorator(Booking booking) {
        this.booking = booking;
    }

    public String createBooking() {
        return booking.createBooking();
    }
}