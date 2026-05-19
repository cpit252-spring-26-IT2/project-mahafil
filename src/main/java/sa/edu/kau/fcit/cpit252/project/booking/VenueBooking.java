package sa.edu.kau.fcit.cpit252.project.booking;

public class VenueBooking implements Booking {
    private static final double BASE_PRICE = 5000.0;

    @Override
    public String createBooking() {
        return "Venue booking";
    }

    @Override
    public double getTotalPrice() {
        return BASE_PRICE;
    }
}
