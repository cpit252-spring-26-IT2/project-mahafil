package sa.edu.kau.fcit.cpit252.project.auth;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataStore {
    private final File usersFile;
    private final File bookingsFile;

    public DataStore(String baseDir) {
        File dir = new File(baseDir);
        if (!dir.exists()) dir.mkdirs();
        this.usersFile = new File(dir, "users.dat");
        this.bookingsFile = new File(dir, "bookings.dat");
    }

    public List<User> loadUsers() {
        return readList(usersFile);
    }

    public void saveUsers(List<User> users) {
        writeList(usersFile, users);
    }

    public List<BookingRecord> loadBookings() {
        return readList(bookingsFile);
    }

    public void saveBookings(List<BookingRecord> bookings) {
        writeList(bookingsFile, bookings);
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> readList(File f) {
        if (!f.exists()) return new ArrayList<>();
        try (
                FileInputStream fileInputStream = new FileInputStream(f);
                ObjectInputStream in = new ObjectInputStream(fileInputStream)
        ) {
            Object o = in.readObject();
            return (List<T>) o;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void writeList(File f, List<?> list) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f))) {
            out.writeObject(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
