package sa.edu.kau.fcit.cpit252.project.venue;

import sa.edu.kau.fcit.cpit252.project.auth.DataStore;

import java.util.ArrayList;
import java.util.List;

public class VenueService {
    private final DataStore store;
    private final List<Venue> venues;

    public VenueService(DataStore store) {
        this.store = store;
        this.venues = new ArrayList<>(store.loadVenues());
        if (venues.isEmpty()) {
            seedDefaultVenues();
            save();
        }
    }

    public List<Venue> getAll() {
        return new ArrayList<>(venues);
    }

    public Venue addVenue(String name, String city, String type, int capacity, double price) {
        return addVenue(name, city, type, capacity, price, false);
    }

    public Venue addVenue(String name, String city, String type, int capacity, double price, boolean kidsFriendly) {
        validate(name, city, type, capacity, price);
        Venue venue = new Venue(name.trim(), city.trim(), type.trim(), capacity, price, kidsFriendly);
        venues.add(venue);
        save();
        return venue;
    }

    public boolean updateVenue(String id, String name, String city, String type, int capacity, double price) {
        Venue venue = findById(id);
        return updateVenue(
                id,
                name,
                city,
                type,
                capacity,
                price,
                venue != null && venue.isKidsFriendly()
        );
    }

    public boolean updateVenue(
            String id,
            String name,
            String city,
            String type,
            int capacity,
            double price,
            boolean kidsFriendly
    ) {
        validate(name, city, type, capacity, price);
        Venue venue = findById(id);
        if (venue == null) {
            return false;
        }
        venue.update(name.trim(), city.trim(), type.trim(), capacity, price, kidsFriendly);
        save();
        return true;
    }

    public boolean deleteVenue(String id) {
        boolean removed = venues.removeIf(venue -> venue.getId().equals(id));
        if (removed) {
            save();
        }
        return removed;
    }

    public Venue findById(String id) {
        for (Venue venue : venues) {
            if (venue.getId().equals(id)) {
                return venue;
            }
        }
        return null;
    }

    private void validate(String name, String city, String type, int capacity, double price) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Venue name is required.");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City is required.");
        }
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Venue type is required.");
        }
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity must be at least 1.");
        }
        if (price <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0.");
        }
    }

    private void seedDefaultVenues() {
        venues.add(new Venue("Corniche Grand Hall", "Jeddah", "Venue", 450, 9500.0, true));
        venues.add(new Venue("Riyadh Business Loft", "Riyadh", "Workspace", 40, 1800.0, false));
        venues.add(new Venue("Makkah Pearl Ballroom", "Makkah", "Venue", 300, 7800.0, true));
        venues.add(new Venue("Dammam Expo Suite", "Dammam", "Workspace", 70, 2600.0, false));
        venues.add(new Venue("Madinah Garden Hall", "Madinah", "Venue", 220, 6200.0, true));
    }

    private void save() {
        store.saveVenues(venues);
    }
}
