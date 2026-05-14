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

        User current = null;

        while (true) {
            if (current == null) {
                System.out.println("1) Register");
                System.out.println("2) Login");
                System.out.println("0) Exit");
                System.out.print("Choose: ");
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
                System.out.println("1) Create booking");
                System.out.println("2) Booking history");
                System.out.println("3) Logout");
                System.out.print("Choose: ");
                String choice = sc.nextLine().trim();
                if (choice.equals("1")) {
                    System.out.print("Type (venue/workspace): ");
                    String type = sc.nextLine().trim();
                    Booking booking = BookingFactory.getBooking(type);
                    System.out.print("VIP? (y/n): ");
                    String vip = sc.nextLine().trim();
                    if (vip.equalsIgnoreCase("y")) booking = new VIPDecorator(booking);
                    System.out.print("Catering? (y/n): ");
                    String cat = sc.nextLine().trim();
                    if (cat.equalsIgnoreCase("y")) booking = new CateringDecorator(booking);

                    String details = booking.createBooking();
                    System.out.println("Booking created: " + details);
                    historyService.addRecord(new BookingRecord(current.getEmail(), details));
                } else if (choice.equals("2")) {
                    List<BookingRecord> records = historyService.getForUser(current.getEmail());
                    if (records.isEmpty()) System.out.println("No bookings yet.");
                    for (BookingRecord r : records) {
                        System.out.println(r.getTimestamp() + " - " + r.getDetails());
                    }
                } else if (choice.equals("3")) {
                    current = null;
                }
            }
        }
        sc.close();
    }
}