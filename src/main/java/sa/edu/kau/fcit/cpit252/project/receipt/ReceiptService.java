package sa.edu.kau.fcit.cpit252.project.receipt;

public class ReceiptService {
    private ReceiptStrategy receiptStrategy;

    public ReceiptService() {
        this(new PdfReceiptStrategy());
    }

    public ReceiptService(ReceiptStrategy receiptStrategy) {
        this.receiptStrategy = receiptStrategy;
    }

    public void setReceiptStrategy(ReceiptStrategy receiptStrategy) {
        this.receiptStrategy = receiptStrategy;
    }

    public void generateReceipt(Receipt receipt) {
        receiptStrategy.generateReceipt(receipt);
    }

}
