package sa.edu.kau.fcit.cpit252.project.receipt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PdfReceiptStrategy implements ReceiptStrategy {
    private static final String DEFAULT_RECEIPTS_DIR = "receipts";

    private final Path receiptsDirectory;

    public PdfReceiptStrategy() {
        this(Path.of(DEFAULT_RECEIPTS_DIR));
    }

    public PdfReceiptStrategy(Path receiptsDirectory) {
        this.receiptsDirectory = receiptsDirectory;
    }

    @Override
    public void generateReceipt(Receipt receipt) {
        try {
            Files.createDirectories(receiptsDirectory);
            Path pdfPath = receiptsDirectory.resolve(buildFileName(receipt));
            Files.write(pdfPath, buildPdf(receipt));
            System.out.println("Receipt PDF saved to " + pdfPath.toAbsolutePath() + ".");
        } catch (IOException ex) {
            System.out.println("Receipt PDF could not be saved: " + ex.getMessage());
        }
    }

    private String buildFileName(Receipt receipt) {
        return "receipt-" + receipt.getReceiptId().replaceAll("[^A-Za-z0-9-]", "") + ".pdf";
    }

    private byte[] buildPdf(Receipt receipt) {
        List<String> lines = new ArrayList<>();
        lines.add("Mahafil Payment Receipt");
        lines.add("");
        lines.add("Receipt ID: " + receipt.getReceiptId());
        lines.add("Customer email: " + receipt.getCustomerEmail());
        lines.add("Paid at: " + receipt.getFormattedPaidAt());
        lines.add("Total: " + String.format("%.2f SAR", receipt.getAmount()));
        lines.add("");
        lines.add("Booking details:");
        for (String line : receipt.getBookingDetails().split("\\R")) {
            lines.add(line);
        }

        String contentStream = buildContentStream(lines);
        byte[] contentBytes = contentStream.getBytes(StandardCharsets.UTF_8);

        StringBuilder pdf = new StringBuilder();
        List<Integer> offsets = new ArrayList<>();

        pdf.append("%PDF-1.4\n");
        offsets.add(pdf.length());
        pdf.append("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        offsets.add(pdf.length());
        pdf.append("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
        offsets.add(pdf.length());
        pdf.append("3 0 obj\n")
                .append("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] ")
                .append("/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\n")
                .append("endobj\n");
        offsets.add(pdf.length());
        pdf.append("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
        offsets.add(pdf.length());
        pdf.append("5 0 obj\n<< /Length ").append(contentBytes.length).append(" >>\nstream\n")
                .append(contentStream)
                .append("endstream\nendobj\n");

        int xrefStart = pdf.length();
        pdf.append("xref\n0 6\n");
        pdf.append("0000000000 65535 f \n");
        for (Integer offset : offsets) {
            pdf.append(String.format("%010d 00000 n \n", offset));
        }
        pdf.append("trailer\n<< /Size 6 /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xrefStart).append("\n%%EOF");

        return pdf.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String buildContentStream(List<String> lines) {
        StringBuilder content = new StringBuilder();
        content.append("BT\n");
        content.append("/F1 18 Tf\n");
        content.append("50 790 Td\n");
        content.append("(").append(escapePdfText(lines.get(0))).append(") Tj\n");
        content.append("/F1 11 Tf\n");

        int visibleLineNumber = 0;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineSpacing = line.isBlank() ? 18 : 16;
            content.append("0 -").append(lineSpacing).append(" Td\n");
            if (!line.isBlank()) {
                content.append("(").append(escapePdfText(line)).append(") Tj\n");
            }

            visibleLineNumber++;
            if (visibleLineNumber > 38) {
                content.append("0 -16 Td\n");
                content.append("(...) Tj\n");
                break;
            }
        }

        content.append("ET\n");
        return content.toString();
    }

    private String escapePdfText(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }
}
