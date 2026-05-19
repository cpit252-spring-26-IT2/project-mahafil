package sa.edu.kau.fcit.cpit252.project.receipt;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Receipt {
    private final String receiptId;
    private final String customerEmail;
    private final String bookingDetails;
    private final double amount;
    private final LocalDateTime paidAt;

    public Receipt(String customerEmail, String bookingDetails, double amount) {
        this.receiptId = UUID.randomUUID().toString();
        this.customerEmail = customerEmail;
        this.bookingDetails = bookingDetails;
        this.amount = amount;
        this.paidAt = LocalDateTime.now();
    }

    public String getReceiptId() {
        return receiptId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getBookingDetails() {
        return bookingDetails;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public String getFormattedPaidAt() {
        return paidAt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
    }
}
