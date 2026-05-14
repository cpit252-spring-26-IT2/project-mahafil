package sa.edu.kau.fcit.cpit252.project.booking;

public class EquipmentDecorator extends BookingDecorator {
    private static final double EQUIPMENT_PRICE = 900.0;

    public EquipmentDecorator(Booking booking) {
        super(booking);
    }

    @Override
    public String createBooking() {
        return booking.createBooking() + " + Equipment";
    }

    @Override
    public double getTotalPrice() {
        return booking.getTotalPrice() + EQUIPMENT_PRICE;
    }
}
