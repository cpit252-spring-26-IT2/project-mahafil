package sa.edu.kau.fcit.cpit252.project.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private final DataStore store;
    private final List<User> users;

    public UserService(DataStore store) {
        this.store = store;
        this.users = new ArrayList<>(store.loadUsers());
    }

    public boolean register(String email, String password) {
        if (findByEmail(email) != null) return false;
        String hash = hash(password);
        User u = new User(email, hash);
        users.add(u);
        store.saveUsers(users);
        return true;
    }

    public User login(String email, String password) {
        User u = findByEmail(email);
        if (u == null) return null;
        if (u.getPasswordHash().equals(hash(password))) return u;
        return null;
    }

    private User findByEmail(String email) {
        for (User u : users) if (u.getEmail().equalsIgnoreCase(email)) return u;
        return null;
    }

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte by : b) sb.append(String.format("%02x", by));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
