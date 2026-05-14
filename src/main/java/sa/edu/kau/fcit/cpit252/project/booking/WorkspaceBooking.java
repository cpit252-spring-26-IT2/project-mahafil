package sa.edu.kau.fcit.cpit252.project.booking;

public class WorkspaceBooking implements Booking {
    private static final double BASE_PRICE = 1200.0;

    @Override
    public String createBooking() {
        return "Workspace booking";
    }

    @Override
    public double getTotalPrice() {
        return BASE_PRICE;
    }
}
