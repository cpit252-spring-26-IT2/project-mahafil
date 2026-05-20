package sa.edu.kau.fcit.cpit252.project;

import sa.edu.kau.fcit.cpit252.project.auth.BookingRecord;
import sa.edu.kau.fcit.cpit252.project.auth.DataStore;
import sa.edu.kau.fcit.cpit252.project.auth.HistoryService;
import sa.edu.kau.fcit.cpit252.project.auth.User;
import sa.edu.kau.fcit.cpit252.project.auth.UserService;
import sa.edu.kau.fcit.cpit252.project.booking.Booking;
import sa.edu.kau.fcit.cpit252.project.booking.BookingFactory;
import sa.edu.kau.fcit.cpit252.project.booking.CateringDecorator;
import sa.edu.kau.fcit.cpit252.project.booking.EquipmentDecorator;
import sa.edu.kau.fcit.cpit252.project.booking.RecommendedBooking;
import sa.edu.kau.fcit.cpit252.project.booking.SmartBookingRecommendationService;
import sa.edu.kau.fcit.cpit252.project.booking.VIPDecorator;
import sa.edu.kau.fcit.cpit252.project.payment.CashPayment;
import sa.edu.kau.fcit.cpit252.project.payment.CreditCardPayment;
import sa.edu.kau.fcit.cpit252.project.payment.PaymentStrategy;
import sa.edu.kau.fcit.cpit252.project.receipt.Receipt;
import sa.edu.kau.fcit.cpit252.project.receipt.ReceiptService;
import sa.edu.kau.fcit.cpit252.project.venue.Venue;
import sa.edu.kau.fcit.cpit252.project.venue.VenueService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class MahafilGui extends JFrame {
    private static final Color BACKGROUND = new Color(246, 247, 249);
    private static final Color PANEL = Color.WHITE;
    private static final Color TEXT = new Color(31, 41, 55);
    private static final Color MUTED = new Color(107, 114, 128);
    private static final Color PRIMARY = new Color(22, 101, 52);
    private static final DateTimeFormatter HISTORY_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm").withZone(ZoneId.systemDefault());

    private final DataStore store = new DataStore("data");
    private final UserService userService = new UserService(store);
    private final HistoryService historyService = new HistoryService(store);
    private final SmartBookingRecommendationService recommendationService = new SmartBookingRecommendationService();
    private final ReceiptService receiptService = new ReceiptService();
    private final VenueService venueService = new VenueService(store);

    private final CardLayout layout = new CardLayout();
    private final JPanel root = new JPanel(layout);
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JLabel currentUserLabel = new JLabel();
    private final JTextArea historyArea = new JTextArea();
    private final JTextArea cityEventsArea = new JTextArea();
    private final JTextArea serviceDetailsArea = new JTextArea();
    private final JComboBox<String> providerSelector = new JComboBox<>();
    private final DefaultListModel<Venue> venueListModel = new DefaultListModel<>();
    private final JList<Venue> venueList = new JList<>(venueListModel);
    private final DefaultListModel<BookingRecord> bookingListModel = new DefaultListModel<>();
    private final JList<BookingRecord> bookingList = new JList<>(bookingListModel);
    private final JTextArea bookingDetailsArea = new JTextArea();
    private final JLabel manualSummary = new JLabel();
    private final JLabel smartSummary = new JLabel();
    private final List<CityEvent> cityEvents = createCityEvents();
    private final List<ServiceProvider> serviceProviders = createServiceProviders();

    private User currentUser;
    private Booking smartBaseBooking;
    private RecommendedBooking currentRecommendation;

    public MahafilGui() {
        super("Mahafil Booking Platform");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(940, 640));
        setLocationRelativeTo(null);
        root.add(createAuthPanel(), "auth");
        root.add(createAppPanel(), "app");
        setContentPane(root);
        layout.show(root, "auth");
    }

    private JPanel createAuthPanel() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(BACKGROUND);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(28, 32, 28, 32)
        ));
        panel.setPreferredSize(new Dimension(420, 390));

        GridBagConstraints c = constraints();
        JLabel title = new JLabel("Mahafil");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 34));
        title.setForeground(PRIMARY);
        panel.add(title, c);

        c.gridy++;
        JLabel subtitle = new JLabel("Venue and workspace booking");
        subtitle.setForeground(MUTED);
        subtitle.setBorder(new EmptyBorder(0, 0, 24, 0));
        panel.add(subtitle, c);

        c.gridy++;
        panel.add(labeledField("Email", emailField), c);

        c.gridy++;
        panel.add(labeledField("Password", passwordField), c);

        c.gridy++;
        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        JButton loginButton = primaryButton("Login");
        JButton registerButton = secondaryButton("Register");
        actions.add(loginButton, actionConstraints(0));
        actions.add(registerButton, actionConstraints(1));
        panel.add(actions, c);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());

        page.add(panel);
        return page;
    }

    private JPanel createAppPanel() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BACKGROUND);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL);
        header.setBorder(new EmptyBorder(16, 24, 16, 24));
        JLabel title = new JLabel("Mahafil");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        title.setForeground(PRIMARY);
        currentUserLabel.setForeground(MUTED);
        JButton logoutButton = secondaryButton("Logout");
        logoutButton.addActionListener(e -> logout());

        JPanel account = new JPanel(new BorderLayout(12, 0));
        account.setOpaque(false);
        account.add(currentUserLabel, BorderLayout.CENTER);
        account.add(logoutButton, BorderLayout.EAST);
        header.add(title, BorderLayout.WEST);
        header.add(account, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Manual Booking", createManualBookingPanel());
        tabs.addTab("Smart Recommendation", createSmartBookingPanel());
        tabs.addTab("Venues", createVenuePanel());
        tabs.addTab("Manage Bookings", createBookingManagementPanel());
        tabs.addTab("Nearby Events", createNearbyEventsPanel());
        tabs.addTab("Services", createServicesPanel());
        tabs.addTab("History", createHistoryPanel());
        tabs.addChangeListener(e -> {
            if (tabs.getTitleAt(tabs.getSelectedIndex()).equals("History")) {
                refreshHistory();
            } else if (tabs.getTitleAt(tabs.getSelectedIndex()).equals("Venues")) {
                refreshVenues();
            } else if (tabs.getTitleAt(tabs.getSelectedIndex()).equals("Manage Bookings")) {
                refreshBookingManagement();
            }
        });

        page.add(header, BorderLayout.NORTH);
        page.add(tabs, BorderLayout.CENTER);
        return page;
    }

    private JPanel createManualBookingPanel() {
        JPanel panel = contentPanel();
        GridBagConstraints c = constraints();

        JComboBox<String> bookingType = new JComboBox<>(new String[]{"venue", "workspace"});
        JComboBox<Venue> venueChoice = new JComboBox<>(venueService.getAll().toArray(new Venue[0]));
        JTextField guests = new JTextField("1");
        JCheckBox vip = new JCheckBox("VIP Service");
        JCheckBox catering = new JCheckBox("Catering");
        JCheckBox equipment = new JCheckBox("Equipment");
        JCheckBox bringingKids = new JCheckBox("Bringing kids");
        JComboBox<String> payment = new JComboBox<>(new String[]{"Cash", "Credit card"});
        CardFields cardFields = new CardFields();

        panel.add(sectionTitle("Create a booking"), c);
        c.gridy++;
        panel.add(twoColumn(labeledField("Type", bookingType), labeledField("Guests", guests)), c);
        c.gridy++;
        panel.add(labeledField("Choose venue or workspace", venueChoice), c);
        c.gridy++;
        panel.add(bringingKids, c);
        c.gridy++;
        panel.add(addOnPanel(vip, catering, equipment), c);
        c.gridy++;
        panel.add(labeledField("Payment method", payment), c);
        c.gridy++;
        panel.add(cardFields.panel, c);
        c.gridy++;
        manualSummary.setForeground(TEXT);
        panel.add(summaryPanel("Booking summary", manualSummary), c);
        c.gridy++;
        JButton create = primaryButton("Create Booking");
        panel.add(create, c);

        Runnable update = () -> updateManualSummary(bookingType, venueChoice, guests, vip, catering, equipment, bringingKids);
        bookingType.addActionListener(e -> update.run());
        venueChoice.addActionListener(e -> update.run());
        bringingKids.addActionListener(e -> update.run());
        vip.addActionListener(e -> update.run());
        catering.addActionListener(e -> update.run());
        equipment.addActionListener(e -> update.run());
        guests.addCaretListener(e -> update.run());
        payment.addActionListener(e -> cardFields.panel.setVisible(payment.getSelectedIndex() == 1));
        cardFields.panel.setVisible(false);
        update.run();

        create.addActionListener(e -> createManualBooking(
                bookingType,
                venueChoice,
                guests,
                vip,
                catering,
                equipment,
                bringingKids,
                payment,
                cardFields
        ));
        return wrapForScroll(panel);
    }

    private JPanel createSmartBookingPanel() {
        JPanel panel = contentPanel();
        GridBagConstraints c = constraints();

        JComboBox<String> eventType = new JComboBox<>(new String[]{
                "Business Meeting", "Workshop", "Wedding", "Celebration"
        });
        JTextField guests = new JTextField("20");
        JCheckBox vip = new JCheckBox("VIP Service");
        JCheckBox catering = new JCheckBox("Catering");
        JCheckBox equipment = new JCheckBox("Equipment");
        JCheckBox bringingKids = new JCheckBox("Bringing kids");
        JComboBox<String> payment = new JComboBox<>(new String[]{"Cash", "Credit card"});
        CardFields cardFields = new CardFields();
        JButton recommend = secondaryButton("Get Recommendation");
        JButton create = primaryButton("Create Recommended Booking");

        panel.add(sectionTitle("Smart recommendation"), c);
        c.gridy++;
        panel.add(twoColumn(labeledField("Event type", eventType), labeledField("Guests", guests)), c);
        c.gridy++;
        panel.add(recommend, c);
        c.gridy++;
        panel.add(bringingKids, c);
        c.gridy++;
        panel.add(addOnPanel(vip, catering, equipment), c);
        c.gridy++;
        panel.add(labeledField("Payment method", payment), c);
        c.gridy++;
        panel.add(cardFields.panel, c);
        c.gridy++;
        panel.add(summaryPanel("Recommendation summary", smartSummary), c);
        c.gridy++;
        panel.add(create, c);

        Runnable update = () -> updateSmartSummary(vip, catering, equipment, bringingKids);
        bringingKids.addActionListener(e -> update.run());
        vip.addActionListener(e -> update.run());
        catering.addActionListener(e -> update.run());
        equipment.addActionListener(e -> update.run());
        guests.addCaretListener(e -> currentRecommendation = null);
        eventType.addActionListener(e -> currentRecommendation = null);
        payment.addActionListener(e -> cardFields.panel.setVisible(payment.getSelectedIndex() == 1));
        cardFields.panel.setVisible(false);

        recommend.addActionListener(e -> {
            try {
                int guestCount = parseGuests(guests.getText());
                currentRecommendation = recommendationService.recommend(String.valueOf(eventType.getSelectedItem()), guestCount);
                smartBaseBooking = currentRecommendation.getBooking();
                update.run();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });
        create.addActionListener(e -> createSmartBooking(vip, catering, equipment, bringingKids, payment, cardFields));
        smartSummary.setText("Choose an event type and get a recommendation.");
        return wrapForScroll(panel);
    }

    private JPanel createVenuePanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        venueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        refreshVenues();

        JTextField name = new JTextField();
        JTextField city = new JTextField();
        JComboBox<String> type = new JComboBox<>(new String[]{"Venue", "Workspace"});
        JTextField capacity = new JTextField();
        JTextField price = new JTextField();
        JCheckBox kidsFriendly = new JCheckBox("Kids friendly");

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = constraints();
        form.add(sectionTitle("Venue management"), c);
        c.gridy++;
        form.add(labeledField("Name", name), c);
        c.gridy++;
        form.add(labeledField("City", city), c);
        c.gridy++;
        form.add(labeledField("Type", type), c);
        c.gridy++;
        form.add(twoColumn(labeledField("Capacity", capacity), labeledField("Price SAR", price)), c);
        c.gridy++;
        form.add(kidsFriendly, c);
        c.gridy++;

        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        JButton add = primaryButton("Add Venue");
        JButton update = secondaryButton("Update");
        JButton delete = secondaryButton("Delete");
        JButton clear = secondaryButton("Clear");
        actions.add(add, actionConstraints(0));
        actions.add(update, actionConstraints(1));
        actions.add(delete, actionConstraints(2));
        actions.add(clear, actionConstraints(3));
        form.add(actions, c);

        venueList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            Venue selected = venueList.getSelectedValue();
            if (selected == null) {
                return;
            }
            name.setText(selected.getName());
            city.setText(selected.getCity());
            type.setSelectedItem(selected.getType());
            capacity.setText(String.valueOf(selected.getCapacity()));
            price.setText(String.format("%.2f", selected.getPrice()));
            kidsFriendly.setSelected(selected.isKidsFriendly());
        });

        add.addActionListener(e -> {
            try {
                venueService.addVenue(
                        name.getText(),
                        city.getText(),
                        String.valueOf(type.getSelectedItem()),
                        parsePositiveInt(capacity.getText(), "Capacity"),
                        parsePositiveDouble(price.getText(), "Price"),
                        kidsFriendly.isSelected()
                );
                clearVenueForm(name, city, capacity, price, kidsFriendly);
                refreshVenues();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });

        update.addActionListener(e -> {
            Venue selected = venueList.getSelectedValue();
            if (selected == null) {
                showError("Choose a venue to update.");
                return;
            }
            try {
                venueService.updateVenue(
                        selected.getId(),
                        name.getText(),
                        city.getText(),
                        String.valueOf(type.getSelectedItem()),
                        parsePositiveInt(capacity.getText(), "Capacity"),
                        parsePositiveDouble(price.getText(), "Price"),
                        kidsFriendly.isSelected()
                );
                refreshVenues();
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });

        delete.addActionListener(e -> {
            Venue selected = venueList.getSelectedValue();
            if (selected == null) {
                showError("Choose a venue to delete.");
                return;
            }
            int confirmed = JOptionPane.showConfirmDialog(this,
                    "Delete " + selected.getName() + "?",
                    "Delete venue",
                    JOptionPane.YES_NO_OPTION);
            if (confirmed == JOptionPane.YES_OPTION) {
                venueService.deleteVenue(selected.getId());
                clearVenueForm(name, city, capacity, price, kidsFriendly);
                refreshVenues();
            }
        });

        clear.addActionListener(e -> {
            venueList.clearSelection();
            clearVenueForm(name, city, capacity, price, kidsFriendly);
        });

        panel.add(new JScrollPane(venueList), BorderLayout.CENTER);
        panel.add(form, BorderLayout.EAST);
        return panel;
    }

    private JPanel createBookingManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(16, 0));
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        bookingList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingDetailsArea.setEditable(true);
        bookingDetailsArea.setLineWrap(true);
        bookingDetailsArea.setWrapStyleWord(true);
        bookingDetailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        bookingDetailsArea.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setOpaque(false);
        right.add(sectionTitle("Update or cancel booking"), BorderLayout.NORTH);
        right.add(new JScrollPane(bookingDetailsArea), BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridBagLayout());
        actions.setOpaque(false);
        JButton refresh = secondaryButton("Refresh");
        JButton update = primaryButton("Update Booking");
        JButton cancel = secondaryButton("Cancel Booking");
        actions.add(refresh, actionConstraints(0));
        actions.add(update, actionConstraints(1));
        actions.add(cancel, actionConstraints(2));
        right.add(actions, BorderLayout.SOUTH);

        bookingList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            BookingRecord selected = bookingList.getSelectedValue();
            if (selected == null) {
                bookingDetailsArea.setText("");
                return;
            }
            bookingDetailsArea.setText(selected.getDetails());
        });

        refresh.addActionListener(e -> refreshBookingManagement());
        update.addActionListener(e -> updateSelectedBooking());
        cancel.addActionListener(e -> cancelSelectedBooking());

        panel.add(new JScrollPane(bookingList), BorderLayout.WEST);
        panel.add(right, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(sectionTitle("Booking history"), BorderLayout.WEST);
        JButton refresh = secondaryButton("Refresh");
        refresh.addActionListener(e -> refreshHistory());
        header.add(refresh, BorderLayout.EAST);

        historyArea.setEditable(false);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        historyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        historyArea.setBorder(new EmptyBorder(14, 14, 14, 14));
        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createNearbyEventsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JTextField cityField = new JTextField("Jeddah");
        JButton search = primaryButton("Search City");
        JPanel controls = new JPanel(new BorderLayout(12, 0));
        controls.setOpaque(false);
        controls.add(labeledField("City name", cityField), BorderLayout.CENTER);
        controls.add(search, BorderLayout.EAST);

        JPanel header = new JPanel(new BorderLayout(0, 14));
        header.setOpaque(false);
        header.add(sectionTitle("Nearby events and workshops"), BorderLayout.NORTH);
        header.add(controls, BorderLayout.CENTER);

        cityEventsArea.setEditable(false);
        cityEventsArea.setLineWrap(true);
        cityEventsArea.setWrapStyleWord(true);
        cityEventsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        cityEventsArea.setBorder(new EmptyBorder(14, 14, 14, 14));

        search.addActionListener(e -> showCityEvents(cityField.getText()));
        showCityEvents(cityField.getText());

        panel.add(header, BorderLayout.NORTH);
        panel.add(new JScrollPane(cityEventsArea), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createServicesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));

        JComboBox<String> category = new JComboBox<>(new String[]{
                "All services", "Catering", "Event Management", "Security", "Photography",
                "Decoration", "Audio Visual", "Transport"
        });
        JButton show = primaryButton("Show Services");
        JButton choose = secondaryButton("Choose Company");

        JPanel controls = new JPanel(new BorderLayout(12, 0));
        controls.setOpaque(false);
        controls.add(labeledField("Service type", category), BorderLayout.CENTER);
        controls.add(show, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(sectionTitle("Service company finder"), BorderLayout.NORTH);
        top.add(controls, BorderLayout.CENTER);

        JPanel chooser = new JPanel(new BorderLayout(12, 0));
        chooser.setOpaque(false);
        chooser.add(labeledField("Company", providerSelector), BorderLayout.CENTER);
        chooser.add(choose, BorderLayout.EAST);

        JPanel north = new JPanel(new BorderLayout(0, 12));
        north.setOpaque(false);
        north.add(top, BorderLayout.NORTH);
        north.add(chooser, BorderLayout.SOUTH);

        serviceDetailsArea.setEditable(false);
        serviceDetailsArea.setLineWrap(true);
        serviceDetailsArea.setWrapStyleWord(true);
        serviceDetailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        serviceDetailsArea.setBorder(new EmptyBorder(14, 14, 14, 14));

        show.addActionListener(e -> showServiceProviders(String.valueOf(category.getSelectedItem())));
        choose.addActionListener(e -> chooseProvider());
        showServiceProviders(String.valueOf(category.getSelectedItem()));

        panel.add(north, BorderLayout.NORTH);
        panel.add(new JScrollPane(serviceDetailsArea), BorderLayout.CENTER);
        return panel;
    }

    private void login() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password are required.");
            return;
        }

        User user = userService.login(email, password);
        if (user == null) {
            showError("Invalid email or password.");
            return;
        }

        currentUser = user;
        currentUserLabel.setText(user.getEmail());
        refreshHistory();
        layout.show(root, "app");
    }

    private void register() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        if (email.isEmpty() || password.isEmpty()) {
            showError("Email and password are required.");
            return;
        }

        if (userService.register(email, password)) {
            JOptionPane.showMessageDialog(this, "Account registered. You can log in now.");
        } else {
            showError("This email is already registered.");
        }
    }

    private void logout() {
        currentUser = null;
        passwordField.setText("");
        layout.show(root, "auth");
    }

    private void refreshVenues() {
        venueListModel.clear();
        for (Venue venue : venueService.getAll()) {
            venueListModel.addElement(venue);
        }
    }

    private void clearVenueForm(JTextField name, JTextField city, JTextField capacity, JTextField price) {
        clearVenueForm(name, city, capacity, price, null);
    }

    private void clearVenueForm(
            JTextField name,
            JTextField city,
            JTextField capacity,
            JTextField price,
            JCheckBox kidsFriendly
    ) {
        name.setText("");
        city.setText("");
        capacity.setText("");
        price.setText("");
        if (kidsFriendly != null) {
            kidsFriendly.setSelected(false);
        }
    }

    private void refreshBookingManagement() {
        bookingListModel.clear();
        bookingDetailsArea.setText("");
        if (currentUser == null) {
            return;
        }
        for (BookingRecord record : historyService.getForUser(currentUser.getEmail())) {
            bookingListModel.addElement(record);
        }
    }

    private void updateSelectedBooking() {
        BookingRecord selected = bookingList.getSelectedValue();
        if (selected == null) {
            showError("Choose a booking to update.");
            return;
        }
        if (selected.isCancelled()) {
            showError("Cancelled bookings cannot be updated.");
            return;
        }
        String details = bookingDetailsArea.getText().trim();
        if (details.isEmpty()) {
            showError("Booking details cannot be empty.");
            return;
        }

        if (historyService.updateBooking(currentUser.getEmail(), selected.getReferenceNumber(), details)) {
            refreshBookingManagement();
            refreshHistory();
            JOptionPane.showMessageDialog(this, "Booking updated.");
        } else {
            showError("Booking could not be updated.");
        }
    }

    private void cancelSelectedBooking() {
        BookingRecord selected = bookingList.getSelectedValue();
        if (selected == null) {
            showError("Choose a booking to cancel.");
            return;
        }

        int confirmed = JOptionPane.showConfirmDialog(this,
                "Cancel booking " + selected.getReferenceNumber() + "?",
                "Cancel booking",
                JOptionPane.YES_NO_OPTION);
        if (confirmed != JOptionPane.YES_OPTION) {
            return;
        }

        if (historyService.cancelBooking(currentUser.getEmail(), selected.getReferenceNumber())) {
            refreshBookingManagement();
            refreshHistory();
            JOptionPane.showMessageDialog(this, "Booking cancelled.");
        } else {
            showError("Booking could not be cancelled.");
        }
    }

    private void createManualBooking(
            JComboBox<String> bookingType,
            JComboBox<Venue> venueChoice,
            JTextField guests,
            JCheckBox vip,
            JCheckBox catering,
            JCheckBox equipment,
            JCheckBox bringingKids,
            JComboBox<String> payment,
            CardFields cardFields
    ) {
        try {
            int guestCount = parseGuests(guests.getText());
            Booking booking = buildBooking(String.valueOf(bookingType.getSelectedItem()), vip, catering, equipment);
            Venue venue = (Venue) venueChoice.getSelectedItem();
            boolean kidsComing = bringingKids.isSelected();
            validateKidsForVenue(venue, kidsComing);
            PaymentStrategy paymentStrategy = buildPaymentStrategy(payment, cardFields);
            paymentStrategy.pay(booking.getTotalPrice());

            String details = formatBookingDetails(booking, guestCount, venue, kidsComing);
            BookingRecord record = saveBooking(details, booking.getTotalPrice());
            JOptionPane.showMessageDialog(this, "Booking created.\nReference: "
                    + record.getReferenceNumber() + "\n\n" + details);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void createSmartBooking(
            JCheckBox vip,
            JCheckBox catering,
            JCheckBox equipment,
            JCheckBox bringingKids,
            JComboBox<String> payment,
            CardFields cardFields
    ) {
        if (currentRecommendation == null || smartBaseBooking == null) {
            showError("Generate a recommendation first.");
            return;
        }

        try {
            Booking booking = applyAddOns(smartBaseBooking, vip, catering, equipment);
            PaymentStrategy paymentStrategy = buildPaymentStrategy(payment, cardFields);
            paymentStrategy.pay(booking.getTotalPrice());

            String details = formatSmartRecommendationDetails(booking, currentRecommendation, bringingKids.isSelected());
            BookingRecord record = saveBooking(details, booking.getTotalPrice());
            JOptionPane.showMessageDialog(this, "Recommended booking created.\nReference: "
                    + record.getReferenceNumber() + "\n\n" + details);
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private BookingRecord saveBooking(String details, double amount) {
        BookingRecord record = new BookingRecord(currentUser.getEmail(), details);
        String receiptDetails = "Reference: " + record.getReferenceNumber() + System.lineSeparator() + details;
        historyService.addRecord(record);
        receiptService.generateReceipt(new Receipt(currentUser.getEmail(), receiptDetails, amount));
        refreshHistory();
        return record;
    }

    private void refreshHistory() {
        if (currentUser == null) {
            historyArea.setText("");
            return;
        }

        List<BookingRecord> records = historyService.getForUser(currentUser.getEmail());
        if (records.isEmpty()) {
            historyArea.setText("No bookings yet.");
            return;
        }

        StringBuilder text = new StringBuilder();
        for (BookingRecord record : records) {
            text.append(HISTORY_TIME_FORMAT.format(record.getTimestamp()))
                    .append(System.lineSeparator())
                    .append("Reference: ")
                    .append(record.getReferenceNumber())
                    .append(System.lineSeparator())
                    .append("Status: ")
                    .append(record.getStatus())
                    .append(System.lineSeparator())
                    .append(record.getDetails())
                    .append(System.lineSeparator())
                    .append(System.lineSeparator());
        }
        historyArea.setText(text.toString());
        historyArea.setCaretPosition(0);
    }

    private void showCityEvents(String cityName) {
        String normalizedCity = cityName == null ? "" : cityName.trim();
        if (normalizedCity.isEmpty()) {
            cityEventsArea.setText("Enter a city name.");
            return;
        }

        StringBuilder text = new StringBuilder();
        for (CityEvent event : cityEvents) {
            if (event.city.equalsIgnoreCase(normalizedCity)) {
                text.append(event.title)
                        .append(System.lineSeparator())
                        .append("City: ").append(event.city)
                        .append(" | Date: ").append(event.date)
                        .append(" | Type: ").append(event.type)
                        .append(System.lineSeparator())
                        .append("Location: ").append(event.location)
                        .append(System.lineSeparator())
                        .append("Details: ").append(event.description)
                        .append(System.lineSeparator())
                        .append(System.lineSeparator());
            }
        }

        if (text.length() == 0) {
            text.append("No fake sample events found for ")
                    .append(normalizedCity)
                    .append(". Try Jeddah, Riyadh, Makkah, Dammam, or Madinah.");
        }

        cityEventsArea.setText(text.toString());
        cityEventsArea.setCaretPosition(0);
    }

    private void showServiceProviders(String category) {
        providerSelector.removeAllItems();
        StringBuilder text = new StringBuilder();
        for (ServiceProvider provider : serviceProviders) {
            if (category.equals("All services") || provider.category.equals(category)) {
                providerSelector.addItem(provider.name);
                text.append(provider.name)
                        .append(System.lineSeparator())
                        .append("Service: ").append(provider.category)
                        .append(" | City: ").append(provider.city)
                        .append(System.lineSeparator())
                        .append("Phone: ").append(provider.phone)
                        .append(System.lineSeparator())
                        .append("Website: ").append(provider.website)
                        .append(System.lineSeparator())
                        .append("Notes: ").append(provider.description)
                        .append(System.lineSeparator())
                        .append(System.lineSeparator());
            }
        }
        serviceDetailsArea.setText(text.toString());
        serviceDetailsArea.setCaretPosition(0);
    }

    private void chooseProvider() {
        Object selected = providerSelector.getSelectedItem();
        if (selected == null) {
            showError("Choose a company first.");
            return;
        }

        for (ServiceProvider provider : serviceProviders) {
            if (provider.name.equals(selected.toString())) {
                JOptionPane.showMessageDialog(this,
                        "Selected company:\n" + provider.name
                                + "\nService: " + provider.category
                                + "\nPhone: " + provider.phone
                                + "\nWebsite: " + provider.website);
                return;
            }
        }
    }

    private List<CityEvent> createCityEvents() {
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

    private List<ServiceProvider> createServiceProviders() {
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

    private void updateManualSummary(
            JComboBox<String> bookingType,
            JComboBox<Venue> venueChoice,
            JTextField guests,
            JCheckBox vip,
            JCheckBox catering,
            JCheckBox equipment,
            JCheckBox bringingKids
    ) {
        try {
            int guestCount = parseGuests(guests.getText());
            Booking booking = buildBooking(String.valueOf(bookingType.getSelectedItem()), vip, catering, equipment);
            Venue venue = (Venue) venueChoice.getSelectedItem();
            validateKidsForVenue(venue, bringingKids.isSelected());
            manualSummary.setText(toHtml(formatBookingDetails(booking, guestCount, venue, bringingKids.isSelected())));
        } catch (IllegalArgumentException ex) {
            manualSummary.setText(ex.getMessage());
        }
    }

    private void updateSmartSummary(JCheckBox vip, JCheckBox catering, JCheckBox equipment, JCheckBox bringingKids) {
        if (currentRecommendation == null || smartBaseBooking == null) {
            smartSummary.setText("Choose an event type and get a recommendation.");
            return;
        }

        Booking booking = applyAddOns(smartBaseBooking, vip, catering, equipment);
        smartSummary.setText(toHtml(formatSmartRecommendationDetails(
                booking,
                currentRecommendation,
                bringingKids.isSelected()
        )));
    }

    private Booking buildBooking(String type, JCheckBox vip, JCheckBox catering, JCheckBox equipment) {
        return applyAddOns(BookingFactory.getBooking(type), vip, catering, equipment);
    }

    private Booking applyAddOns(Booking booking, JCheckBox vip, JCheckBox catering, JCheckBox equipment) {
        if (vip.isSelected()) {
            booking = new VIPDecorator(booking);
        }
        if (catering.isSelected()) {
            booking = new CateringDecorator(booking);
        }
        if (equipment.isSelected()) {
            booking = new EquipmentDecorator(booking);
        }
        return booking;
    }

    private PaymentStrategy buildPaymentStrategy(JComboBox<String> payment, CardFields cardFields) {
        if (payment.getSelectedIndex() == 0) {
            return new CashPayment();
        }

        String name = cardFields.name.getText().trim();
        String cardNumber = cardFields.number.getText().trim();
        String cvv = cardFields.cvv.getText().trim();
        String expiration = cardFields.expiration.getText().trim();

        if (name.isEmpty() || !name.matches("[A-Za-z ]+")) {
            throw new IllegalArgumentException("Cardholder name must contain letters only.");
        }
        if (cardNumber.isEmpty() || !cardNumber.matches("\\d+")) {
            throw new IllegalArgumentException("Card number must contain numbers only.");
        }
        if (cvv.isEmpty() || !cvv.matches("\\d+")) {
            throw new IllegalArgumentException("CVV must contain numbers only.");
        }
        if (expiration.isEmpty() || !expiration.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            throw new IllegalArgumentException("Expiration date must be in MM/YY format.");
        }

        return new CreditCardPayment(name, cardNumber, cvv, expiration);
    }

    private int parseGuests(String value) {
        try {
            int guests = Integer.parseInt(value.trim());
            if (guests < 1) {
                throw new IllegalArgumentException("Guest count must be at least 1.");
            }
            return guests;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Guests must be a number.");
        }
    }

    private int parsePositiveInt(String value, String label) {
        try {
            int number = Integer.parseInt(value.trim());
            if (number < 1) {
                throw new IllegalArgumentException(label + " must be at least 1.");
            }
            return number;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a number.");
        }
    }

    private double parsePositiveDouble(String value, String label) {
        try {
            double number = Double.parseDouble(value.trim());
            if (number <= 0) {
                throw new IllegalArgumentException(label + " must be greater than 0.");
            }
            return number;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + " must be a number.");
        }
    }

    private String formatBookingDetails(Booking booking, int guestCount) {
        return String.format(
                "%s | Price per person: %.2f SAR | Total: %.2f SAR",
                booking.createBooking(),
                booking.getTotalPrice() / guestCount,
                booking.getTotalPrice()
        );
    }

    private String formatBookingDetails(Booking booking, int guestCount, Venue venue) {
        return formatBookingDetails(booking, guestCount, venue, false);
    }

    private String formatBookingDetails(Booking booking, int guestCount, Venue venue, boolean bringingKids) {
        String baseDetails = formatBookingDetails(booking, guestCount);
        if (venue == null) {
            return baseDetails + System.lineSeparator() + formatKidsDetails(bringingKids);
        }
        return baseDetails
                + System.lineSeparator() + "Selected place: " + venue.getSummary()
                + System.lineSeparator() + formatKidsDetails(bringingKids);
    }

    private String formatSmartRecommendationDetails(Booking booking, RecommendedBooking recommendation) {
        return formatSmartRecommendationDetails(booking, recommendation, false);
    }

    private String formatSmartRecommendationDetails(
            Booking booking,
            RecommendedBooking recommendation,
            boolean bringingKids
    ) {
        return String.format(
                "Booking: %s%nServices: %s%nPrice per person: %.2f SAR%nTotal: %.2f SAR%n%s%nReason: %s",
                recommendation.getBooking().createBooking(),
                formatSelectedServices(booking, recommendation.getBooking()),
                booking.getTotalPrice() / recommendation.getGuestCount(),
                booking.getTotalPrice(),
                formatKidsDetails(bringingKids),
                recommendation.getReason().replace(" You can choose add-on services before payment.", "")
        );
    }

    private void validateKidsForVenue(Venue venue, boolean bringingKids) {
        if (bringingKids && venue != null && !venue.isKidsFriendly()) {
            throw new IllegalArgumentException("The selected place is not kids friendly. Choose another place.");
        }
    }

    private String formatKidsDetails(boolean bringingKids) {
        return "Bringing kids: " + (bringingKids ? "Yes" : "No");
    }

    private String formatSelectedServices(Booking booking, Booking baseBooking) {
        String bookingDetails = booking.createBooking();
        String baseBookingDetails = baseBooking.createBooking();
        if (bookingDetails.equals(baseBookingDetails)) {
            return "None";
        }
        return bookingDetails
                .replaceFirst("^" + java.util.regex.Pattern.quote(baseBookingDetails), "")
                .replaceFirst("^ \\+ ", "");
    }

    private JPanel contentPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BACKGROUND);
        panel.setBorder(new EmptyBorder(24, 24, 24, 24));
        return panel;
    }

    private JPanel wrapForScroll(JPanel panel) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BACKGROUND);
        wrapper.add(panel, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(wrapper);
        scrollPane.setBorder(null);
        JPanel outer = new JPanel(new BorderLayout());
        outer.add(scrollPane);
        return outer;
    }

    private JLabel sectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        label.setForeground(TEXT);
        return label;
    }

    private JPanel labeledField(String label, Component field) {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel title = new JLabel(label);
        title.setForeground(TEXT);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        field.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(260, 38));
        panel.add(title, BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JPanel twoColumn(Component left, Component right) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints leftC = actionConstraints(0);
        leftC.weightx = 1;
        leftC.fill = GridBagConstraints.HORIZONTAL;
        GridBagConstraints rightC = actionConstraints(1);
        rightC.weightx = 1;
        rightC.fill = GridBagConstraints.HORIZONTAL;
        panel.add(left, leftC);
        panel.add(right, rightC);
        return panel;
    }

    private JPanel addOnPanel(JCheckBox vip, JCheckBox catering, JCheckBox equipment) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(12, 12, 12, 12)
        ));
        panel.add(vip, actionConstraints(0));
        panel.add(catering, actionConstraints(1));
        panel.add(equipment, actionConstraints(2));
        return panel;
    }

    private JPanel summaryPanel(String title, JLabel summary) {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(14, 16, 14, 16)
        ));
        JLabel label = new JLabel(title);
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        label.setForeground(TEXT);
        summary.setVerticalAlignment(SwingConstants.TOP);
        panel.add(label, BorderLayout.NORTH);
        panel.add(summary, BorderLayout.CENTER);
        return panel;
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        button.setPreferredSize(new Dimension(180, 40));
        return button;
    }

    private JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(229, 231, 235));
        button.setForeground(TEXT);
        button.setFocusPainted(false);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        button.setPreferredSize(new Dimension(150, 40));
        return button;
    }

    private GridBagConstraints constraints() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 16, 0);
        return c;
    }

    private GridBagConstraints actionConstraints(int x) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = 0;
        c.insets = new Insets(0, x == 0 ? 0 : 10, 0, 0);
        return c;
    }

    private String toHtml(String value) {
        return "<html>" + value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace(System.lineSeparator(), "<br>")
                .replace("\n", "<br>") + "</html>";
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Mahafil", JOptionPane.ERROR_MESSAGE);
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

    private class CardFields {
        private final JPanel panel = new JPanel(new GridBagLayout());
        private final JTextField name = new JTextField();
        private final JTextField number = new JTextField();
        private final JTextField cvv = new JTextField();
        private final JTextField expiration = new JTextField();

        private CardFields() {
            panel.setOpaque(false);
            panel.add(twoColumn(labeledField("Cardholder name", name), labeledField("Card number", number)), constraints());
            GridBagConstraints c = constraints();
            c.gridy = 1;
            panel.add(twoColumn(labeledField("CVV", cvv), labeledField("Expiration MM/YY", expiration)), c);
        }
    }
}
