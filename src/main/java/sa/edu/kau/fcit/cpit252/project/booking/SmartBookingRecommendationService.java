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
            return new RecommendedBooking(
                    booking,
                    "workspace",
                    "Workspace is best for focused business events. You can choose add-on services before payment.",
                    guestCount
            );
        }

        Booking booking = BookingFactory.getBooking("venue");
        return new RecommendedBooking(
                booking,
                "venue",
                "Venue is best for larger social events. You can choose add-on services before payment.",
                guestCount
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
