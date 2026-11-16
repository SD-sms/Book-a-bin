// src/main/java/com/example/bookabin/util/PDFReceiptGenerator.java
package com.example.bookabin.util;

import com.example.bookabin.entity.Payment;
import com.example.bookabin.entity.User;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

public class PDFReceiptGenerator {

    public static String generateReceipt(Payment payment) {
        try {
            File folder = new File("receipts");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            String fileName = "receipt_" + payment.getId() + ".pdf";
            String filePath = folder.getAbsolutePath() + File.separator + fileName;

            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20);
            Paragraph title = new Paragraph("BBMP Payment Receipt", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(Chunk.NEWLINE);

            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

            User user = payment.getUser();  // explicit typed user

            document.add(new Paragraph("Receipt ID: " + payment.getId(), boldFont));
            document.add(new Paragraph("Date & Time: " + payment.getPaymentDate().format(formatter)));
            document.add(new Paragraph("Paid By: " + (user != null ? user.getUsername() : "N/A")));
            document.add(new Paragraph("Username: " + (user != null ? user.getUsername() : "N/A")));
            document.add(new Paragraph("Purpose: " + payment.getPurpose()));
            document.add(new Paragraph("Amount Paid: ₹" + payment.getAmount()));

            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("Thank you for your payment!", boldFont));

            document.close();

            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generating PDF receipt");
        }
    }
}