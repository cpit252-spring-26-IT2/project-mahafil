package sa.edu.kau.fcit.cpit252.project.booking;

public class SmartBookingRecommendationService {

    public RecommendedBooking recommend(String eventType, int guestCount) {
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("Event type is required.");
        }
        if (guestCount < 1) {
            throw new IllegalArgumentException("Guest count must be at least 1.");
        }

        String normalizedEventType = eventType.trim().toLowerCase();
        if (isWorkspaceEvent(normalizedEventType, guestCount)) {
            Booking booking = BookingFactory.getBooking("workspace");
            booking = new EquipmentDecorator(booking);
            if (guestCount >= 8 || normalizedEventType.contains("meeting")) {
                booking = new CateringDecorator(booking);
            }
            return new RecommendedBooking(
                    booking,
                    "workspace",
                    "Workspace is best for focused business events, with equipment and catering selected for the group size."
            );
        }

        Booking booking = BookingFactory.getBooking("venue");
        if (guestCount >= 50 || normalizedEventType.contains("wedding")) {
            booking = new CateringDecorator(booking);
            booking = new VIPDecorator(booking);
        } else {
            booking = new EquipmentDecorator(booking);
        }
        return new RecommendedBooking(
                booking,
                "venue",
                "Venue is best for larger social events, with services selected to support guests and event presentation."
        );
    }

    private boolean isWorkspaceEvent(String eventType, int guestCount) {
        return eventType.contains("business")
                || eventType.contains("meeting")
                || eventType.contains("workshop")
                || eventType.contains("training")
                || guestCount <= 20;
    }
}
