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
import sa.edu.kau.fcit.cpit252.project.receipt.Receipt;
import sa.edu.kau.fcit.cpit252.project.receipt.ReceiptService;
import sa.edu.kau.fcit.cpit252.project.venue.Venue;
import sa.edu.kau.fcit.cpit252.project.venue.VenueService;

import javax.swing.SwingUtilities;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class App {
    private static final DateTimeFormatter HISTORY_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").withZone(ZoneId.systemDefault());
    private static final List<CityEvent> CITY_EVENTS = createCityEvents();
    private static final List<ServiceProvider> SERVICE_PROVIDERS = createServiceProviders();

    public static void main(String[] args) {
        boolean isDocker = args.length > 0 && args[0].equalsIgnoreCase("--docker");
        Scanner sc = new Scanner(System.in);

        if (!isDocker) {
            System.out.println("Choose mode:");
            System.out.println("1) GUI");
            System.out.println("2) Console");
            System.out.print("Enter choice: ");

            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                System.out.println("Running in GUI mode");
                SwingUtilities.invokeLater(() -> new MahafilGui().setVisible(true));
                return;
            } else {
                System.out.println("Running in Console mode");
            }
        } else {
            System.out.println("Running in Docker (Console mode)");
        }

        DataStore store = new DataStore("data");
        UserService userService = new UserService(store);
        HistoryService historyService = new HistoryService(store);
        SmartBookingRecommendationService recommendationService = new SmartBookingRecommendationService();
        ReceiptService receiptService = new ReceiptService();
        VenueService venueService = new VenueService(store);

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
                System.out.println("4) Venue management");
                System.out.println("5) Manage bookings");
                System.out.println("6) Nearby events and workshops");
                System.out.println("7) Service company finder");
                System.out.println("8) Logout");
                System.out.print("Choose: ");
                if (!sc.hasNextLine()) {
                    System.out.println("Bye.");
                    break;
                }
                String choice = sc.nextLine().trim();
                if (choice.equals("1")) {
                    createManualBooking(sc, historyService, current, receiptService, venueService);
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
                            createSmartRecommendationBooking(sc, historyService, current, recommendation, receiptService);
                        } else {
                            System.out.println("Create the booking manually instead.");
                            createManualBooking(sc, historyService, current, receiptService, venueService);
                        }
                    } catch (NumberFormatException ex) {
                        System.out.println("Guests must be a number.");
                    } catch (IllegalArgumentException ex) {
                        System.out.println(ex.getMessage());
                    }
                } else if (choice.equals("3")) {
                    showBookingHistory(historyService, current);
                } else if (choice.equals("4")) {
                    manageVenues(sc, venueService);
                } else if (choice.equals("5")) {
                    manageBookings(sc, historyService, current);
                } else if (choice.equals("6")) {
                    showNearbyEvents(sc);
                } else if (choice.equals("7")) {
                    showServiceFinder(sc);
                } else if (choice.equals("8")) {
                    current = null;
                }
            }
        }
        sc.close();
    }

    private static void createManualBooking(Scanner sc, HistoryService historyService, User current) {
        createManualBooking(sc, historyService, current, new ReceiptService());
    }

    private static void createManualBooking(
            Scanner sc,
            HistoryService historyService,
            User current,
            ReceiptService receiptService
    ) {
        createManualBooking(sc, historyService, current, receiptService, null);
    }

    private static void createManualBooking(
            Scanner sc,
            HistoryService historyService,
            User current,
            ReceiptService receiptService,
            VenueService venueService
    ) {
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
        Venue selectedVenue = chooseVenueForBooking(sc, venueService);
        if (selectedVenue != null) {
            details += System.lineSeparator() + "Selected place: " + selectedVenue.getSummary();
        }
        System.out.println("Booking created: " + details);
        processPayment(sc, booking.getTotalPrice());
        BookingRecord record = new BookingRecord(current.getEmail(), details);
        receiptService.generateReceipt(new Receipt(
                current.getEmail(),
                "Reference: " + record.getReferenceNumber() + System.lineSeparator() + details,
                booking.getTotalPrice()
        ));
        historyService.addRecord(record);
        System.out.println("Reference number: " + record.getReferenceNumber());
    }

    private static Venue chooseVenueForBooking(Scanner sc, VenueService venueService) {
        if (venueService == null) {
            return null;
        }

        List<Venue> venues = venueService.getAll();
        if (venues.isEmpty()) {
            return null;
        }

        System.out.print("Choose a venue/workspace from the saved list? (y/n): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("y")) {
            return null;
        }

        printVenues(venues);
        int index = readInt(sc, "Venue number (0 to skip): ");
        if (index == 0) {
            return null;
        }
        if (index < 1 || index > venues.size()) {
            System.out.println("Invalid venue number. No venue selected.");
            return null;
        }
        return venues.get(index - 1);
    }

    private static void createSmartRecommendationBooking(
            Scanner sc,
            HistoryService historyService,
            User current,
            RecommendedBooking recommendation
    ) {
        createSmartRecommendationBooking(sc, historyService, current, recommendation, new ReceiptService());
    }

    private static void createSmartRecommendationBooking(
            Scanner sc,
            HistoryService historyService,
            User current,
            RecommendedBooking recommendation,
            ReceiptService receiptService
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
        BookingRecord record = new BookingRecord(current.getEmail(), details);
        receiptService.generateReceipt(new Receipt(
                current.getEmail(),
                "Reference: " + record.getReferenceNumber() + System.lineSeparator() + details,
                booking.getTotalPrice()
        ));
        historyService.addRecord(record);
        System.out.println("Reference number: " + record.getReferenceNumber());
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

    private static void showBookingHistory(HistoryService historyService, User current) {
        List<BookingRecord> records = historyService.getForUser(current.getEmail());
        if (records.isEmpty()) {
            System.out.println("No bookings yet.");
            return;
        }

        for (BookingRecord record : records) {
            System.out.println("----------------------------------------");
            System.out.println(HISTORY_TIME_FORMAT.format(record.getTimestamp()));
            System.out.println("Reference: " + record.getReferenceNumber());
            System.out.println("Status: " + record.getStatus());
            System.out.println(record.getDetails());
        }
        System.out.println("----------------------------------------");
    }

    private static void manageVenues(Scanner sc, VenueService venueService) {
        while (true) {
            System.out.println("1) View venues");
            System.out.println("2) Add venue");
            System.out.println("3) Update venue");
            System.out.println("4) Delete venue");
            System.out.println("0) Back");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                printVenues(venueService.getAll());
            } else if (choice.equals("2")) {
                addVenue(sc, venueService);
            } else if (choice.equals("3")) {
                updateVenue(sc, venueService);
            } else if (choice.equals("4")) {
                deleteVenue(sc, venueService);
            } else if (choice.equals("0")) {
                return;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private static void addVenue(Scanner sc, VenueService venueService) {
        try {
            Venue venue = venueService.addVenue(
                    readRequired(sc, "Name: "),
                    readRequired(sc, "City: "),
                    readRequired(sc, "Type (Venue/Workspace): "),
                    readPositiveInt(sc, "Capacity: ", "Capacity"),
                    readPositiveDouble(sc, "Price SAR: ", "Price")
            );
            System.out.println("Venue added: " + venue.getSummary());
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void updateVenue(Scanner sc, VenueService venueService) {
        List<Venue> venues = venueService.getAll();
        printVenues(venues);
        int index = readInt(sc, "Venue number to update: ");
        if (index < 1 || index > venues.size()) {
            System.out.println("Invalid venue number.");
            return;
        }

        Venue selected = venues.get(index - 1);
        try {
            boolean updated = venueService.updateVenue(
                    selected.getId(),
                    readRequired(sc, "Name: "),
                    readRequired(sc, "City: "),
                    readRequired(sc, "Type (Venue/Workspace): "),
                    readPositiveInt(sc, "Capacity: ", "Capacity"),
                    readPositiveDouble(sc, "Price SAR: ", "Price")
            );
            System.out.println(updated ? "Venue updated." : "Venue could not be updated.");
        } catch (IllegalArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void deleteVenue(Scanner sc, VenueService venueService) {
        List<Venue> venues = venueService.getAll();
        printVenues(venues);
        int index = readInt(sc, "Venue number to delete: ");
        if (index < 1 || index > venues.size()) {
            System.out.println("Invalid venue number.");
            return;
        }

        Venue selected = venues.get(index - 1);
        System.out.print("Delete " + selected.getName() + "? (y/n): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("Delete cancelled.");
            return;
        }

        System.out.println(venueService.deleteVenue(selected.getId())
                ? "Venue deleted."
                : "Venue could not be deleted.");
    }

    private static void printVenues(List<Venue> venues) {
        if (venues.isEmpty()) {
            System.out.println("No venues found.");
            return;
        }

        for (int i = 0; i < venues.size(); i++) {
            System.out.println((i + 1) + ") " + venues.get(i).getSummary());
        }
    }

    private static void manageBookings(Scanner sc, HistoryService historyService, User current) {
        while (true) {
            System.out.println("1) View bookings");
            System.out.println("2) Update booking details");
            System.out.println("3) Cancel booking");
            System.out.println("0) Back");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                showBookingHistory(historyService, current);
            } else if (choice.equals("2")) {
                updateBookingDetails(sc, historyService, current);
            } else if (choice.equals("3")) {
                cancelBooking(sc, historyService, current);
            } else if (choice.equals("0")) {
                return;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private static void updateBookingDetails(Scanner sc, HistoryService historyService, User current) {
        List<BookingRecord> records = historyService.getForUser(current.getEmail());
        printBookingReferences(records);
        if (records.isEmpty()) {
            return;
        }

        System.out.print("Reference number to update: ");
        String reference = sc.nextLine().trim();
        BookingRecord record = historyService.findForUser(current.getEmail(), reference);
        if (record == null) {
            System.out.println("Booking not found.");
            return;
        }
        if (record.isCancelled()) {
            System.out.println("Cancelled bookings cannot be updated.");
            return;
        }

        System.out.println("Current details:");
        System.out.println(record.getDetails());
        System.out.print("New details: ");
        String details = sc.nextLine().trim();
        if (details.isEmpty()) {
            System.out.println("Booking details cannot be empty.");
            return;
        }

        System.out.println(historyService.updateBooking(current.getEmail(), reference, details)
                ? "Booking updated."
                : "Booking could not be updated.");
    }

    private static void cancelBooking(Scanner sc, HistoryService historyService, User current) {
        List<BookingRecord> records = historyService.getForUser(current.getEmail());
        printBookingReferences(records);
        if (records.isEmpty()) {
            return;
        }

        System.out.print("Reference number to cancel: ");
        String reference = sc.nextLine().trim();
        System.out.print("Cancel booking " + reference + "? (y/n): ");
        if (!sc.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("Cancel request ignored.");
            return;
        }

        System.out.println(historyService.cancelBooking(current.getEmail(), reference)
                ? "Booking cancelled."
                : "Booking could not be cancelled.");
    }

    private static void printBookingReferences(List<BookingRecord> records) {
        if (records.isEmpty()) {
            System.out.println("No bookings yet.");
            return;
        }

        for (BookingRecord record : records) {
            System.out.println(record.getReferenceNumber() + " (" + record.getStatus() + ")");
        }
    }

    private static void showNearbyEvents(Scanner sc) {
        System.out.print("City name: ");
        String city = sc.nextLine().trim();
        if (city.isEmpty()) {
            System.out.println("Enter a city name.");
            return;
        }

        boolean found = false;
        for (CityEvent event : CITY_EVENTS) {
            if (event.city.equalsIgnoreCase(city)) {
                found = true;
                System.out.println("----------------------------------------");
                System.out.println(event.title);
                System.out.println("City: " + event.city + " | Date: " + event.date + " | Type: " + event.type);
                System.out.println("Location: " + event.location);
                System.out.println("Details: " + event.description);
            }
        }

        if (!found) {
            System.out.println("opps!! sorry try these "
                    + city + ". Try Jeddah, Riyadh, Makkah, Dammam, or Madinah.");
        } else {
            System.out.println("----------------------------------------");
        }
    }

    private static void showServiceFinder(Scanner sc) {
        while (true) {
            System.out.println("Service type:");
            System.out.println("1) All services");
            System.out.println("2) Catering");
            System.out.println("3) Event Management");
            System.out.println("4) Security");
            System.out.println("5) Photography");
            System.out.println("6) Decoration");
            System.out.println("7) Audio Visual");
            System.out.println("8) Transport");
            System.out.println("0) Back");
            System.out.print("Choose: ");
            String choice = sc.nextLine().trim();
            if (choice.equals("0")) {
                return;
            }

            String category = serviceCategory(choice);
            if (category == null) {
                System.out.println("Invalid service type.");
                continue;
            }

            List<ServiceProvider> matches = SERVICE_PROVIDERS.stream()
                    .filter(provider -> category.equals("All services") || provider.category.equals(category))
                    .toList();
            for (int i = 0; i < matches.size(); i++) {
                ServiceProvider provider = matches.get(i);
                System.out.println("----------------------------------------");
                System.out.println((i + 1) + ") " + provider.name);
                System.out.println("Service: " + provider.category + " | City: " + provider.city);
                System.out.println("Phone: " + provider.phone);
                System.out.println("Website: " + provider.website);
                System.out.println("Notes: " + provider.description);
            }
            System.out.println("----------------------------------------");
            int selected = readInt(sc, "Company number for contact details (0 to back): ");
            if (selected > 0 && selected <= matches.size()) {
                ServiceProvider provider = matches.get(selected - 1);
                System.out.println("Selected company:");
                System.out.println(provider.name);
                System.out.println("Service: " + provider.category);
                System.out.println("Phone: " + provider.phone);
                System.out.println("Website: " + provider.website);
            }
        }
    }

    private static String serviceCategory(String choice) {
        return switch (choice) {
            case "1" -> "All services";
            case "2" -> "Catering";
            case "3" -> "Event Management";
            case "4" -> "Security";
            case "5" -> "Photography";
            case "6" -> "Decoration";
            case "7" -> "Audio Visual";
            case "8" -> "Transport";
            default -> null;
        };
    }

    private static int readInt(Scanner sc, String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static int readPositiveInt(Scanner sc, String prompt, String label) {
        int value = readInt(sc, prompt);
        if (value < 1) {
            throw new IllegalArgumentException(label + " must be at least 1.");
        }
        return value;
    }

    private static double readPositiveDouble(Scanner sc, String prompt, String label) {
        System.out.print(prompt);
        try {
            double value = Double.parseDouble(sc.nextLine().trim());
            if (value <= 0) {
                throw new IllegalArgumentException(label + " must be greater than 0.");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a number.");
        }
    }

    private static String readRequired(Scanner sc, String prompt) {
        System.out.print(prompt);
        String value = sc.nextLine().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("This field is required.");
        }
        return value;
    }

    private static List<CityEvent> createCityEvents() {
        return Arrays.asList(
                new CityEvent("Jeddah", "Startup Founders Workshop", "Workshop", "26-05-2026",
                        "Jeddah Innovation Hub", "Practical sessions for early business planning and pitching."),
                new CityEvent("Jeddah", "Red Sea Wedding Expo", "Event", "29-05-2026",
                        "Corniche Grand Hall", "Venue, catering, decoration, and photography showcases."),
                new CityEvent("Riyadh", "Corporate Events Forum", "Workshop", "27-05-2026",
                        "King Abdullah Financial District", "Operations workshop for company event planners."),
                new CityEvent("Riyadh", "Creative Nights Market", "Event", "31-05-2026",
                        "Diriyah Square", "Local vendors, food booths, and family entertainment."),
                new CityEvent("Makkah", "Hospitality Planning Session", "Workshop", "24-05-2026",
                        "Makkah Business Center", "Guest flow, safety, and large gathering planning."),
                new CityEvent("Dammam", "Eastern Province Business Meetup", "Event", "28-05-2026",
                        "Dammam Expo Center", "Networking event for small companies and service providers."),
                new CityEvent("Madinah", "Community Celebration Planning", "Workshop", "30-05-2026",
                        "Madinah Civic Hall", "Budgeting and supplier coordination for family events.")
        );
    }

    private static List<ServiceProvider> createServiceProviders() {
        return Arrays.asList(
                new ServiceProvider("Golden Spoon Catering", "Catering", "Jeddah",
                        "+966 50 100 2468", "https://www.goldenspoon-events.example",
                        "Buffet, coffee service, dessert stations, and wedding menus."),
                new ServiceProvider("Najd Banquet Co.", "Catering", "Riyadh",
                        "+966 55 210 8844", "https://www.najdbanquet.example",
                        "Large-scale Saudi dinner service and corporate lunch packages."),
                new ServiceProvider("Mahra Event Planners", "Event Management", "Jeddah",
                        "+966 54 330 7712", "https://www.mahraevents.example",
                        "Full event planning, guest registration, schedules, and vendor coordination."),
                new ServiceProvider("Tuwaiq Occasions", "Event Management", "Riyadh",
                        "+966 53 450 1900", "https://www.tuwaiqoccasions.example",
                        "Conference, graduation, and private celebration management."),
                new ServiceProvider("Shield Gate Security", "Security", "Dammam",
                        "+966 56 700 1122", "https://www.shieldgate-sa.example",
                        "Entry control, crowd guidance, and trained security teams."),
                new ServiceProvider("Falcon Guard Services", "Security", "Makkah",
                        "+966 50 640 9090", "https://www.falconguard.example",
                        "VIP bodyguards, event security, and private escort teams."),
                new ServiceProvider("Lens House Studio", "Photography", "Madinah",
                        "+966 59 808 3311", "https://www.lenshouse.example",
                        "Photography, videography, highlight reels, and same-day edits."),
                new ServiceProvider("Layali Decor", "Decoration", "Jeddah",
                        "+966 57 222 6130", "https://www.layalidecor.example",
                        "Stage design, flowers, tables, lighting themes, and entrance styling."),
                new ServiceProvider("Clear Sound AV", "Audio Visual", "Riyadh",
                        "+966 58 411 7070", "https://www.clearsoundav.example",
                        "Speakers, microphones, screens, projectors, and technical operators."),
                new ServiceProvider("Route One Transport", "Transport", "Dammam",
                        "+966 52 909 4545", "https://www.routeonetransport.example",
                        "Guest buses, VIP cars, airport pickup, and event shuttle routes.")
        );
    }

    private static class CityEvent {
        private final String city;
        private final String title;
        private final String type;
        private final String date;
        private final String location;
        private final String description;

        private CityEvent(String city, String title, String type, String date, String location, String description) {
            this.city = city;
            this.title = title;
            this.type = type;
            this.date = date;
            this.location = location;
            this.description = description;
        }
    }

    private static class ServiceProvider {
        private final String name;
        private final String category;
        private final String city;
        private final String phone;
        private final String website;
        private final String description;

        private ServiceProvider(
                String name,
                String category,
                String city,
                String phone,
                String website,
                String description
        ) {
            this.name = name;
            this.category = category;
            this.city = city;
            this.phone = phone;
            this.website = website;
            this.description = description;
        }
    }
}
