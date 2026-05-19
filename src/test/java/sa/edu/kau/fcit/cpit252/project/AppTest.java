package sa.edu.kau.fcit.cpit252.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sa.edu.kau.fcit.cpit252.project.auth.BookingRecord;
import sa.edu.kau.fcit.cpit252.project.auth.DataStore;
import sa.edu.kau.fcit.cpit252.project.auth.HistoryService;
import sa.edu.kau.fcit.cpit252.project.auth.User;
import sa.edu.kau.fcit.cpit252.project.auth.UserService;
import sa.edu.kau.fcit.cpit252.project.booking.Booking;
import sa.edu.kau.fcit.cpit252.project.booking.BookingDecorator;
import sa.edu.kau.fcit.cpit252.project.booking.BookingFactory;
import sa.edu.kau.fcit.cpit252.project.booking.CateringDecorator;
import sa.edu.kau.fcit.cpit252.project.booking.EquipmentDecorator;
import sa.edu.kau.fcit.cpit252.project.booking.RecommendedBooking;
import sa.edu.kau.fcit.cpit252.project.booking.SmartBookingRecommendationService;
import sa.edu.kau.fcit.cpit252.project.booking.VIPDecorator;
import sa.edu.kau.fcit.cpit252.project.booking.VenueBooking;
import sa.edu.kau.fcit.cpit252.project.booking.WorkspaceBooking;
import sa.edu.kau.fcit.cpit252.project.payment.CashPayment;
import sa.edu.kau.fcit.cpit252.project.payment.CreditCardPayment;
import sa.edu.kau.fcit.cpit252.project.payment.PaymentStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AppTest {
    @TempDir
    Path tempDir;

    @Test
    public void userStoresEmailAndPasswordHash() {
        User user = new User("user@example.com", "hashed-password");

        assertEquals("user@example.com", user.getEmail());
        assertEquals("hashed-password", user.getPasswordHash());
    }

    @Test
    public void bookingRecordStoresUserDetailsAndTimestamp() {
        BookingRecord record = new BookingRecord("user@example.com", "Venue booking");

        assertEquals("user@example.com", record.getUserEmail());
        assertEquals("Venue booking", record.getDetails());
        assertNotNull(record.getTimestamp());
    }

    @Test
    public void dataStoreReturnsEmptyListsWhenFilesDoNotExist() {
        DataStore store = new DataStore(tempDir.toString());

        assertTrue(store.loadUsers().isEmpty());
        assertTrue(store.loadBookings().isEmpty());
    }

    @Test
    public void dataStorePersistsUsersAndBookings() {
        DataStore store = new DataStore(tempDir.toString());
        User user = new User("user@example.com", "hash");
        BookingRecord record = new BookingRecord("user@example.com", "Workspace booking");

        store.saveUsers(List.of(user));
        store.saveBookings(List.of(record));

        assertEquals("user@example.com", store.loadUsers().get(0).getEmail());
        assertEquals("Workspace booking", store.loadBookings().get(0).getDetails());
    }

    @Test
    public void dataStoreReturnsEmptyListForCorruptedFile() throws Exception {
        Files.write(tempDir.resolve("users.dat"), "not serialized data".getBytes(StandardCharsets.UTF_8));
        DataStore store = new DataStore(tempDir.toString());

        assertTrue(store.loadUsers().isEmpty());
    }

    @Test
    public void userServiceRegistersAndLogsInUser() {
        UserService service = new UserService(new DataStore(tempDir.toString()));

        assertTrue(service.register("user@example.com", "secret"));
        User loggedIn = service.login("user@example.com", "secret");

        assertNotNull(loggedIn);
        assertEquals("user@example.com", loggedIn.getEmail());
        assertNotEquals("secret", loggedIn.getPasswordHash());
    }

    @Test
    public void userServiceRejectsDuplicateEmailIgnoringCase() {
        UserService service = new UserService(new DataStore(tempDir.toString()));

        assertTrue(service.register("user@example.com", "secret"));
        assertFalse(service.register("USER@example.com", "another-secret"));
    }

    @Test
    public void userServiceReturnsNullForInvalidLogin() {
        UserService service = new UserService(new DataStore(tempDir.toString()));
        service.register("user@example.com", "secret");

        assertNull(service.login("user@example.com", "wrong"));
        assertNull(service.login("missing@example.com", "secret"));
    }

    @Test
    public void historyServiceAddsFiltersAndPersistsRecords() {
        DataStore store = new DataStore(tempDir.toString());
        HistoryService historyService = new HistoryService(store);

        historyService.addRecord(new BookingRecord("user@example.com", "First booking"));
        historyService.addRecord(new BookingRecord("other@example.com", "Other booking"));

        List<BookingRecord> records = historyService.getForUser("USER@example.com");
        assertEquals(1, records.size());
        assertEquals("First booking", records.get(0).getDetails());

        HistoryService reloadedHistoryService = new HistoryService(store);
        assertEquals(1, reloadedHistoryService.getForUser("user@example.com").size());
    }

    @Test
    public void venueBookingHasExpectedDescriptionAndPrice() {
        Booking booking = new VenueBooking();

        assertEquals("Venue booking", booking.createBooking());
        assertEquals(5000.0, booking.getTotalPrice(), 0.001);
    }

    @Test
    public void workspaceBookingHasExpectedDescriptionAndPrice() {
        Booking booking = new WorkspaceBooking();

        assertEquals("Workspace booking", booking.createBooking());
        assertEquals(1200.0, booking.getTotalPrice(), 0.001);
    }

    @Test
    public void bookingFactoryCreatesVenueAndWorkspaceBookings() {
        assertInstanceOf(VenueBooking.class, BookingFactory.getBooking("venue"));
        assertInstanceOf(WorkspaceBooking.class, BookingFactory.getBooking("WORKSPACE"));
    }

    @Test
    public void bookingFactoryRejectsUnknownType() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> BookingFactory.getBooking("garden")
        );

        assertEquals("Unknown booking type: garden", exception.getMessage());
    }

    @Test
    public void bookingDecoratorDelegatesToWrappedBooking() {
        Booking wrapped = new WorkspaceBooking();
        Booking decorator = new BookingDecorator(wrapped) {
        };

        assertEquals(wrapped.createBooking(), decorator.createBooking());
        assertEquals(wrapped.getTotalPrice(), decorator.getTotalPrice(), 0.001);
    }

    @Test
    public void decoratorsAddDescriptionsAndPrices() {
        Booking booking = new VenueBooking();
        booking = new VIPDecorator(booking);
        booking = new CateringDecorator(booking);
        booking = new EquipmentDecorator(booking);

        assertEquals("Venue booking + VIP Service + Catering + Equipment", booking.createBooking());
        assertEquals(8150.0, booking.getTotalPrice(), 0.001);
    }

    @Test
    public void recommendedBookingReturnsItsValuesAndSummary() {
        Booking booking = new EquipmentDecorator(new WorkspaceBooking());
        RecommendedBooking recommendedBooking = new RecommendedBooking(booking, "workspace", "Best fit", 7);

        assertSame(booking, recommendedBooking.getBooking());
        assertEquals("workspace", recommendedBooking.getBookingType());
        assertEquals("Best fit", recommendedBooking.getReason());
        assertEquals(7, recommendedBooking.getGuestCount());
        assertEquals(300.0, recommendedBooking.getPricePerPerson(), 0.001);
        assertFalse(recommendedBooking.getSummary().contains("Booking:"));
        assertTrue(recommendedBooking.getSummary().contains("Recommended type: workspace"));
        assertTrue(recommendedBooking.getSummary().contains("Price per person: 300.00 SAR"));
        assertTrue(recommendedBooking.getSummary().contains("Total: 2100.00 SAR"));
        assertTrue(recommendedBooking.getSummary().contains("Reason: Best fit"));
    }

    @Test
    public void smartRecommendationRejectsInvalidInput() {
        SmartBookingRecommendationService service = new SmartBookingRecommendationService();

        assertEquals(
                "Event type is required.",
                assertThrows(IllegalArgumentException.class, () -> service.recommend(" ", 5)).getMessage()
        );
        assertEquals(
                "Guest count must be at least 1.",
                assertThrows(IllegalArgumentException.class, () -> service.recommend("Workshop", 0)).getMessage()
        );
    }

    @Test
    public void smartRecommendationCreatesWorkspaceBookingForFocusedEvents() {
        RecommendedBooking recommendation = new SmartBookingRecommendationService().recommend("Business Meeting", 8);

        assertEquals("workspace", recommendation.getBookingType());
        assertEquals("Workspace booking", recommendation.getBooking().createBooking());
        assertEquals(1200.0, recommendation.getBooking().getTotalPrice(), 0.001);
    }

    @Test
    public void smartRecommendationCreatesVenueBookingForSmallSocialEvents() {
        RecommendedBooking recommendation = new SmartBookingRecommendationService().recommend("Celebration", 30);

        assertEquals("venue", recommendation.getBookingType());
        assertEquals("Venue booking", recommendation.getBooking().createBooking());
        assertEquals(5000.0, recommendation.getBooking().getTotalPrice(), 0.001);
    }

    @Test
    public void smartRecommendationCreatesVipCateringVenueForLargeOrWeddingEvents() {
        RecommendedBooking recommendation = new SmartBookingRecommendationService().recommend("Wedding", 100);

        assertEquals("venue", recommendation.getBookingType());
        assertEquals("Venue booking", recommendation.getBooking().createBooking());
        assertEquals(5000.0, recommendation.getBooking().getTotalPrice(), 0.001);
    }

    @Test
    public void creditCardPaymentImplementsPaymentStrategyAndPrintsDetails() {
        PaymentStrategy paymentStrategy = new CreditCardPayment(
                "Mohamad Alghamdi",
                "1234567812345678",
                "123",
                "12/30"
        );

        assertInstanceOf(PaymentStrategy.class, paymentStrategy);
        assertTrue(captureOutput(() -> paymentStrategy.pay(150.50)).contains("150.5 was processed on a credit card."));
    }

    @Test
    public void creditCardPaymentToStringShowsTransactionDateAndMaskedCardNumber() {
        CreditCardPayment payment = new CreditCardPayment(
                "Mohamad Alghamdi",
                "1234567812345678",
                "123",
                "12/30"
        );

        String details = payment.toString();

        assertTrue(details.contains("Credit Card Payment"));
        assertTrue(details.contains("Transaction Id: "));
        assertTrue(details.contains("Date: "));
        assertTrue(details.contains("Card Number: ****5678"));
    }

    @Test
    public void creditCardPaymentStoresDetailsAfterAppValidation() {
        CreditCardPayment payment = new CreditCardPayment("12345", "1234567812345678", "123", "May");

        assertTrue(payment.toString().contains("Card Number: ****5678"));
    }

    @Test
    public void cashPaymentImplementsPaymentStrategyAndPrintsDetails() {
        PaymentStrategy paymentStrategy = new CashPayment();

        assertInstanceOf(PaymentStrategy.class, paymentStrategy);
        assertTrue(captureOutput(() -> paymentStrategy.pay(75.25)).contains("Paid 75.25 using cash."));
    }

    @Test
    public void appFormatsBookingDetails() throws Exception {
        String details = formatBookingDetails(new CateringDecorator(new WorkspaceBooking()), 9);

        assertEquals("Workspace booking + Catering | Price per person: 300.00 SAR | Total: 2700.00 SAR", details);
    }

    @Test
    public void appAsksForManualServicesAndAppliesSelectedDecorators() throws Exception {
        Booking booking = askForManualServices("y\nn\ny\n", new VenueBooking());

        assertEquals("Venue booking + VIP Service + Equipment", booking.createBooking());
        assertEquals(6650.0, booking.getTotalPrice(), 0.001);
    }

    @Test
    public void appChoosesCreditCardPaymentStrategy() throws Exception {
        PaymentStrategy paymentStrategy = choosePaymentStrategy("1\nMohamad Alghamdi\n1234567812345678\n123\n12/30\n");

        assertInstanceOf(CreditCardPayment.class, paymentStrategy);
    }

    @Test
    public void appChoosesCashPaymentStrategy() throws Exception {
        PaymentStrategy paymentStrategy = choosePaymentStrategy("2\n");

        assertInstanceOf(CashPayment.class, paymentStrategy);
    }

    @Test
    public void appRepeatsPaymentChoiceUntilValid() {
        String output = captureOutput(() -> {
            try {
                PaymentStrategy paymentStrategy = choosePaymentStrategy("9\n2\n");
                assertInstanceOf(CashPayment.class, paymentStrategy);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        assertTrue(output.contains("Invalid payment method."));
    }

    @Test
    public void appRepeatsCreditCardPaymentChoiceUntilCardDetailsAreValid() {
        String output = captureOutput(() -> {
            try {
                PaymentStrategy paymentStrategy = choosePaymentStrategy(
                        "1\n12345\nMohamad Alghamdi\n1234567812345678\nh\n123\nu\n12/30\n"
                );
                assertInstanceOf(CreditCardPayment.class, paymentStrategy);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        assertTrue(output.contains("Cardholder name must contain letters only."));
        assertTrue(output.contains("CVV must contain numbers only."));
        assertTrue(output.contains("Expiration date must be in MM/YY format."));
        assertFalse(output.contains("Invalid payment method."));
    }

    @Test
    public void appProcessesPaymentUsingSelectedStrategy() {
        String output = captureOutput(() -> {
            try {
                processPayment("1\nMohamad Alghamdi\n1234567812345678\n123\n12/30\n", 250.00);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        assertTrue(output.contains("250.0 was processed on a credit card."));
    }

    @Test
    public void appCreatesManualBookingAndStoresHistory() throws Exception {
        HistoryService historyService = new HistoryService(new DataStore(tempDir.toString()));
        User current = new User("user@example.com", "hash");

        String output = captureOutput(() -> {
            try {
                createManualBooking("workspace\n3\ny\ny\nn\n2\n", historyService, current);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        List<BookingRecord> records = historyService.getForUser("user@example.com");
        assertEquals(1, records.size());
        assertEquals(
                "Workspace booking + VIP Service + Catering | Price per person: 1150.00 SAR | Total: 3450.00 SAR",
                records.get(0).getDetails()
        );
        assertTrue(output.contains("Booking created: Workspace booking + VIP Service + Catering | Price per person: 1150.00 SAR | Total: 3450.00 SAR"));
        assertTrue(output.contains("Paid 3450.00 using cash."));
    }

    @Test
    public void appCreatesSmartRecommendedBookingWithPreferredAddOns() throws Exception {
        HistoryService historyService = new HistoryService(new DataStore(tempDir.toString()));
        User current = new User("user@example.com", "hash");
        RecommendedBooking recommendation = new SmartBookingRecommendationService().recommend("Business Meeting", 8);

        String output = captureOutput(() -> {
            try {
                createSmartRecommendationBooking("y\nn\ny\n2\n", historyService, current, recommendation);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        List<BookingRecord> records = historyService.getForUser("user@example.com");
        assertEquals(1, records.size());
        assertTrue(records.get(0).getDetails().contains("Booking: Workspace booking"));
        assertTrue(records.get(0).getDetails().contains("Services: VIP Service + Equipment"));
        assertFalse(records.get(0).getDetails().contains("Recommended type:"));
        assertTrue(records.get(0).getDetails().contains("Price per person: 356.25 SAR"));
        assertTrue(records.get(0).getDetails().contains("Total: 2850.00 SAR"));
        assertTrue(records.get(0).getDetails().contains("Reason: Workspace is best for focused business events."));
        assertFalse(records.get(0).getDetails().contains("You can choose add-on services before payment."));
        assertTrue(output.contains("Choose preferred add-on services for this recommendation."));
        assertTrue(output.contains("Smart recommendation summary"));
        assertTrue(output.contains("Booking: Workspace booking"));
        assertFalse(output.contains("Recommended type:"));
        assertTrue(output.contains("Price per person: 356.25 SAR"));
        assertTrue(output.contains("Total: 2850.00 SAR"));
        assertTrue(output.contains("Paid 2850.00 using cash."));
    }

    @Test
    public void appDoesNotStoreManualBookingForUnknownType() throws Exception {
        HistoryService historyService = new HistoryService(new DataStore(tempDir.toString()));
        User current = new User("user@example.com", "hash");

        String output = captureOutput(() -> {
            try {
                createManualBooking("invalid\n", historyService, current);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        assertTrue(historyService.getForUser("user@example.com").isEmpty());
        assertTrue(output.contains("Unknown booking type: invalid"));
    }

    private static Booking askForManualServices(String input, Booking booking) throws Exception {
        Method method = App.class.getDeclaredMethod("askForManualServices", Scanner.class, Booking.class);
        method.setAccessible(true);

        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        return (Booking) method.invoke(null, scanner, booking);
    }

    private static PaymentStrategy choosePaymentStrategy(String input) throws Exception {
        Method method = App.class.getDeclaredMethod("choosePaymentStrategy", Scanner.class);
        method.setAccessible(true);

        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        return (PaymentStrategy) method.invoke(null, scanner);
    }

    private static void processPayment(String input, double amount) throws Exception {
        Method method = App.class.getDeclaredMethod("processPayment", Scanner.class, double.class);
        method.setAccessible(true);

        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        method.invoke(null, scanner, amount);
    }

    private static String formatBookingDetails(Booking booking, int guestCount) throws Exception {
        Method method = App.class.getDeclaredMethod("formatBookingDetails", Booking.class, int.class);
        method.setAccessible(true);

        return (String) method.invoke(null, booking, guestCount);
    }

    private static void createManualBooking(String input, HistoryService historyService, User current) throws Exception {
        Method method = App.class.getDeclaredMethod("createManualBooking", Scanner.class, HistoryService.class, User.class);
        method.setAccessible(true);

        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        method.invoke(null, scanner, historyService, current);
    }

    private static void createSmartRecommendationBooking(
            String input,
            HistoryService historyService,
            User current,
            RecommendedBooking recommendation
    ) throws Exception {
        Method method = App.class.getDeclaredMethod(
                "createSmartRecommendationBooking",
                Scanner.class,
                HistoryService.class,
                User.class,
                RecommendedBooking.class
        );
        method.setAccessible(true);

        Scanner scanner = new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        method.invoke(null, scanner, historyService, current, recommendation);
    }

    private static String captureOutput(Runnable runnable) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        try {
            runnable.run();
        } finally {
            System.setOut(originalOut);
        }

        return output.toString(StandardCharsets.UTF_8);
    }
}
