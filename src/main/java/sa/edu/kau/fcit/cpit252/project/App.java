package sa.edu.kau.fcit.cpit252.project;

import sa.edu.kau.fcit.cpit252.project.auth.BookingRecord;
import sa.edu.kau.fcit.cpit252.project.auth.DataStore;
import sa.edu.kau.fcit.cpit252.project.auth.HistoryService;
import sa.edu.kau.fcit.cpit252.project.auth.User;
import sa.edu.kau.fcit.cpit252.project.auth.UserService;
import sa.edu.kau.fcit.cpit252.project.booking.*;

import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        DataStore store = new DataStore("data");
        UserService userService = new UserService(store);
        HistoryService historyService = new HistoryService(store);
        SmartBookingRecommendationService recommendationService = new SmartBookingRecommendationService();

        User current = null;

        while (true) {
            if (current == null) {
                System.out.println("1) Register");
                System.out.println("2) Login");
                System.out.println("0) Exit");
                System.out.print("Choose: ");
                if (!sc.hasNextLine()) {
                    System.out.println("Bye.");
                    break;
                }
                String choice = sc.nextLine().trim();
                if (choice.equals("1")) {
                    System.out.print("Email: ");
                    String email = sc.nextLine().trim();
                    System.out.print("Password: ");
                    String pwd = sc.nextLine().trim();
                    boolean ok = userService.register(email, pwd);
                    System.out.println(ok ? "Registered." : "Email already used.");
                } else if (choice.equals("2")) {
                    System.out.print("Email: ");
                    String email = sc.nextLine().trim();
                    System.out.print("Password: ");
                    String pwd = sc.nextLine().trim();
                    User u = userService.login(email, pwd);
                    if (u != null) {
                        current = u;
                        System.out.println("Logged in as " + current.getEmail());
                    } else {
                        System.out.println("Invalid credentials.");
                    }
                } else if (choice.equals("0")) {
                    System.out.println("Bye.");
                    break;
                }
            } else {
                System.out.println("1) Create booking manually");
                System.out.println("2) Smart auto-recommendation booking");
                System.out.println("3) Booking history");
                System.out.println("4) Logout");
                System.out.print("Choose: ");
                if (!sc.hasNextLine()) {
                    System.out.println("Bye.");
                    break;
                }
                String choice = sc.nextLine().trim();
                if (choice.equals("1")) {
                    createManualBooking(sc, historyService, current);
                } else if (choice.equals("2")) {
                    System.out.print("Event type (Business Meeting / Workshop / Wedding / Celebration): ");
                    String eventType = sc.nextLine().trim();
                    System.out.print("Guests: ");
                    int guestCount;
                    try {
                        guestCount = Integer.parseInt(sc.nextLine().trim());
                        RecommendedBooking recommendation = recommendationService.recommend(eventType, guestCount);
                        System.out.println("----------------------------------------");
                        System.out.println("Smart recommendation");
                        System.out.println("----------------------------------------");
                        System.out.println(recommendation.getSummary());
                        System.out.println("----------------------------------------");
                        System.out.print("Do you like this recommendation? (y/n): ");
                        String accepted = sc.nextLine().trim();
                        if (accepted.equalsIgnoreCase("y")) {
                            String details = "Smart recommendation accepted - " + recommendation.getSummary();
                            System.out.println("Booking created: " + details);
                            historyService.addRecord(new BookingRecord(current.getEmail(), details));
                            System.out.println("Bye.");
                            sc.close();
                            return;
                        } else {
                            System.out.println("Create the booking manually instead.");
                            createManualBooking(sc, historyService, current);
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println("Guests must be a number.");
                    } catch (IllegalArgumentException ex) {
                        System.out.println(ex.getMessage());
                    }
                } else if (choice.equals("3")) {
                    List<BookingRecord> records = historyService.getForUser(current.getEmail());
                    if (records.isEmpty()) System.out.println("No bookings yet.");
                    for (BookingRecord r : records) {
                        System.out.println(r.getTimestamp() + " - " + r.getDetails());
                    }
                } else if (choice.equals("4")) {
                    current = null;
                }
            }
        }
        sc.close();
    }

    private static void createManualBooking(Scanner sc, HistoryService historyService, User current) {
        System.out.print("Type (venue/workspace): ");
        String type = sc.nextLine().trim();
        Booking booking;
        try {
            booking = BookingFactory.getBooking(type);
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
            return;
        }

        booking = askForManualServices(sc, booking);

        String details = formatBookingDetails(booking);
        System.out.println("Booking created: " + details);
        historyService.addRecord(new BookingRecord(current.getEmail(), details));
    }

    private static Booking askForManualServices(Scanner sc, Booking booking) {
        System.out.print("VIP? (y/n): ");
        String vip = sc.nextLine().trim();
        if (vip.equalsIgnoreCase("y")) booking = new VIPDecorator(booking);

        System.out.print("Catering? (y/n): ");
        String catering = sc.nextLine().trim();
        if (catering.equalsIgnoreCase("y")) booking = new CateringDecorator(booking);

        System.out.print("Equipment? (y/n): ");
        String equipment = sc.nextLine().trim();
        if (equipment.equalsIgnoreCase("y")) booking = new EquipmentDecorator(booking);

        return booking;
    }

    private static String formatBookingDetails(Booking booking) {
        return String.format("%s | Total: %.2f SAR", booking.createBooking(), booking.getTotalPrice());
    }
}
