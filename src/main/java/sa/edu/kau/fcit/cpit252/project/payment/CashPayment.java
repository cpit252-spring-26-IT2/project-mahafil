package sa.edu.kau.fcit.cpit252.project.payment;

public class CashPayment implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.printf("Paid %.2f using cash.%n", amount);
    }
}
