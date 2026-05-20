package sa.edu.kau.fcit.cpit252.project.venue;

import java.io.Serializable;
import java.util.UUID;

public class Venue implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private String name;
    private String city;
    private String type;
    private int capacity;
    private double price;
    private boolean kidsFriendly;

    public Venue(String name, String city, String type, int capacity, double price) {
        this(name, city, type, capacity, price, false);
    }

    public Venue(String name, String city, String type, int capacity, double price, boolean kidsFriendly) {
        this.id = "VEN-" + UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();
        this.name = name;
        this.city = city;
        this.type = type;
        this.capacity = capacity;
        this.price = price;
        this.kidsFriendly = kidsFriendly;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public String getType() {
        return type;
    }

    public int getCapacity() {
        return capacity;
    }

    public double getPrice() {
        return price;
    }

    public boolean isKidsFriendly() {
        return kidsFriendly;
    }

    public void update(String name, String city, String type, int capacity, double price) {
        update(name, city, type, capacity, price, kidsFriendly);
    }

    public void update(String name, String city, String type, int capacity, double price, boolean kidsFriendly) {
        this.name = name;
        this.city = city;
        this.type = type;
        this.capacity = capacity;
        this.price = price;
        this.kidsFriendly = kidsFriendly;
    }

    public String getSummary() {
        return String.format(
                "%s | %s | %s | Capacity: %d | %.2f SAR | Kids friendly: %s",
                name,
                city,
                type,
                capacity,
                price,
                kidsFriendly ? "Yes" : "No"
        );
    }

    @Override
    public String toString() {
        return name + " (" + city + ", " + type + ", Kids friendly: " + (kidsFriendly ? "Yes" : "No") + ")";
    }
}
