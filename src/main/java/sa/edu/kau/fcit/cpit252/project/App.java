package sa.edu.kau.fcit.cpit252.project;

import sa.edu.kau.fcit.cpit252.project.auth.BookingRecord;
import sa.edu.kau.fcit.cpit252.project.auth.DataStore;
import sa.edu.kau.fcit.cpit252.project.auth.HistoryService;
import sa.edu.kau.fcit.cpit252.project.auth.User;
import sa.edu.kau.fcit.cpit252.project.auth.UserService;
import sa.edu.kau.fcit.cpit252.project.booking.*;
import sa.edu.kau.fcit.cpit252.project.payment.CashPayment;
import sa.edu.kau.fcit.cpit252.project.payment.CreditCardPayment;
import sa.edu.kau.fcit.cpit252.project.payment.PaymentStrategy;

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
                            createSmartRecommendationBooking(sc, historyService, current, recommendation);
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

        System.out.print("Guests: ");
        int guestCount;
        try {
            guestCount = Integer.parseInt(sc.nextLine().trim());
            if (guestCount < 1) {
                System.out.println("Guest count must be at least 1.");
                return;
            }
        } catch (NumberFormatException ex) {
            System.out.println("Guests must be a number.");
            return;
        }

        booking = askForManualServices(sc, booking);

        String details = formatBookingDetails(booking, guestCount);
        System.out.println("Booking created: " + details);
        processPayment(sc, booking.getTotalPrice());
        historyService.addRecord(new BookingRecord(current.getEmail(), details));
    }

    private static void createSmartRecommendationBooking(
            Scanner sc,
            HistoryService historyService,
            User current,
            RecommendedBooking recommendation
    ) {
        System.out.println("Choose preferred add-on services for this recommendation.");
        Booking booking = askForManualServices(sc, recommendation.getBooking());
        String details = formatSmartRecommendationDetails(booking, recommendation);
        System.out.println("----------------------------------------");
        System.out.println("Smart recommendation summary");
        System.out.println("----------------------------------------");
        System.out.println(details);
        System.out.println("----------------------------------------");
        System.out.println("Booking created.");
        processPayment(sc, booking.getTotalPrice());
        historyService.addRecord(new BookingRecord(current.getEmail(), details));
    }

    private static void processPayment(Scanner sc, double amount) {
        PaymentStrategy paymentStrategy = choosePaymentStrategy(sc);
        paymentStrategy.pay(amount);
    }

    private static PaymentStrategy choosePaymentStrategy(Scanner sc) {
        while (true) {
            System.out.println("Payment method:");
            System.out.println("1) Credit card");
            System.out.println("2) Cash");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                String name;
                while (true) {
                    System.out.print("Cardholder name: ");
                    name = sc.nextLine().trim();
                    if (name.isEmpty()) {
                        System.out.println("Cardholder name is required.");
                    } else if (!name.matches("[A-Za-z ]+")) {
                        System.out.println("Cardholder name must contain letters only.");
                    } else {
                        break;
                    }
                }

                String cardNumber;
                while (true) {
                    System.out.print("Card number: ");
                    cardNumber = sc.nextLine().trim();
                    if (cardNumber.isEmpty()) {
                        System.out.println("Card number is required.");
                    } else if (!cardNumber.matches("\\d+")) {
                        System.out.println("Card number must contain numbers only.");
                    } else {
                        break;
                    }
                }

                String cvv;
                while (true) {
                    System.out.print("CVV: ");
                    cvv = sc.nextLine().trim();
                    if (cvv.isEmpty()) {
                        System.out.println("CVV is required.");
                    } else if (!cvv.matches("\\d+")) {
                        System.out.println("CVV must contain numbers only.");
                    } else {
                        break;
                    }
                }

                String monthYearExpiration;
                while (true) {
                    System.out.print("Expiration date (MM/YY): ");
                    monthYearExpiration = sc.nextLine().trim();
                    if (monthYearExpiration.isEmpty()) {
                        System.out.println("Expiration date is required.");
                    } else if (!monthYearExpiration.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                        System.out.println("Expiration date must be in MM/YY format.");
                    } else {
                        break;
                    }
                }

                return new CreditCardPayment(name, cardNumber, cvv, monthYearExpiration);
            } else if (choice.equals("2")) {
                return new CashPayment();
            }

            System.out.println("Invalid payment method.");
        }
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

    private static String formatBookingDetails(Booking booking, int guestCount) {
        return String.format(
                "%s | Price per person: %.2f SAR | Total: %.2f SAR",
                booking.createBooking(),
                booking.getTotalPrice() / guestCount,
                booking.getTotalPrice()
        );
    }

    private static String formatSmartRecommendationDetails(Booking booking, RecommendedBooking recommendation) {
        return String.format(
                "Booking: %s%nServices: %s%nPrice per person: %.2f SAR%nTotal: %.2f SAR%nReason: %s",
                recommendation.getBooking().createBooking(),
                formatSelectedServices(booking, recommendation.getBooking()),
                booking.getTotalPrice() / recommendation.getGuestCount(),
                booking.getTotalPrice(),
                formatFinalRecommendationReason(recommendation.getReason())
        );
    }

    private static String formatSelectedServices(Booking booking, Booking baseBooking) {
        String bookingDetails = booking.createBooking();
        String baseBookingDetails = baseBooking.createBooking();

        if (bookingDetails.equals(baseBookingDetails)) {
            return "None";
        }

        return bookingDetails
                .replaceFirst("^" + java.util.regex.Pattern.quote(baseBookingDetails), "")
                .replaceFirst("^ \\+ ", "");
    }

    private static String formatFinalRecommendationReason(String reason) {
        return reason.replace(" You can choose add-on services before payment.", "");
    }
}
