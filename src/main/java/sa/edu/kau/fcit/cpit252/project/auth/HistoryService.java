package sa.edu.kau.fcit.cpit252.project.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryService {
    private final DataStore store;
    private final List<BookingRecord> bookings;

    public HistoryService(DataStore store) {
        this.store = store;
        this.bookings = new ArrayList<>(store.loadBookings());
    }

    public void addRecord(BookingRecord r) {
        bookings.add(r);
        store.saveBookings(bookings);
    }

    public List<BookingRecord> getForUser(String email) {
        return bookings.stream()
                .filter(b -> b.getUserEmail().equalsIgnoreCase(email))
                .collect(Collectors.toList());
    }
}
