package sa.edu.kau.fcit.cpit252.project.payment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class CreditCardPayment implements PaymentStrategy {
    private final String transactionId;
    private final String name;
    private final String cardNumber;
    private final String cvv;
    private final String monthYearExpiration;
    private final Date date;

    public CreditCardPayment(String name, String cardNumber, String cvv, String monthYearExpiration) {
        this.transactionId = UUID.randomUUID().toString();
        this.name = name;
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.monthYearExpiration = monthYearExpiration;
        this.date = new Date();
    }

    @Override
    public void pay(double amount) {
        System.out.println(amount + " was processed on a credit card.");
    }

    @Override
    public String toString() {
        String datePattern = "dd-MM-yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(datePattern);
        return "Credit Card Payment " +
                "\n\tTransaction Id: " + this.transactionId +
                "\n\tDate: " + simpleDateFormat.format(this.date) +
                "\n\tCard Number: ****" + getMaskedCardSuffix();
    }

    private String getMaskedCardSuffix() {
        if (cardNumber == null || cardNumber.length() <= 4) {
            return cardNumber;
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }
}
