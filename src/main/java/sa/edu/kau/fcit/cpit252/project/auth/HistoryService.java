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

    public boolean cancelBooking(String email, String referenceNumber) {
        BookingRecord record = findForUser(email, referenceNumber);
        if (record == null || record.isCancelled()) {
            return false;
        }
        record.cancel();
        store.saveBookings(bookings);
        return true;
    }

    public boolean updateBooking(String email, String referenceNumber, String details) {
        BookingRecord record = findForUser(email, referenceNumber);
        if (record == null || record.isCancelled()) {
            return false;
        }
        record.updateDetails(details);
        store.saveBookings(bookings);
        return true;
    }

    public BookingRecord findForUser(String email, String referenceNumber) {
        for (BookingRecord booking : bookings) {
            if (booking.getUserEmail().equalsIgnoreCase(email)
                    && booking.getReferenceNumber().equalsIgnoreCase(referenceNumber)) {
                return booking;
            }
        }
        return null;
    }
}
