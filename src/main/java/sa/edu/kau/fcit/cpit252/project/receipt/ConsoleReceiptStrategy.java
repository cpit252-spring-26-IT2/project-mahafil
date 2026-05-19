package sa.edu.kau.fcit.cpit252.project.receipt;

public class ConsoleReceiptStrategy implements ReceiptStrategy {
    @Override
    public void generateReceipt(Receipt receipt) {
        System.out.println("Receipt generated for " + receipt.getCustomerEmail() + ".");
    }
}
