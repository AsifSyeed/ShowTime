package com.example.showtime.common.pdf;

import com.example.showtime.common.qr.QRCodeGenerator;
import com.example.showtime.ticket.model.entity.Ticket;
import com.example.showtime.user.model.entity.UserAccount;
import com.google.zxing.WriterException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PdfGenerator {

    private static String OUTPUT_PATH = "";

    public void generateTicketPdf(UserAccount createdBy, Ticket ticket) {
        //add eventId from ticket to the template path
        String TEMPLATE_PATH = "src/main/resources/assets/genericTickets/generic_ticket_" + ticket.getEventId() + ".pdf";
        // Append ticket.eventName to the output path
        String folderPath = "src/main/resources/assets/tickets/" + ticket.getEventId() + "/";
        OUTPUT_PATH = folderPath + ticket.getTicketQrCode() + ".pdf";

        // Create the folder if it doesn't exist
        try {
            Path directory = Paths.get(folderPath);
            Files.createDirectories(directory);
        } catch (IOException e) {
            // Handle the exception, e.g., log it or throw a runtime exception
            e.printStackTrace();
        }

        try {
            PDDocument document = Loader.loadPDF(new File(TEMPLATE_PATH));
            PDPage page = document.getPage(0);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                setFontAndColor(contentStream);

                addText(contentStream, createdBy.getUserName(), 545);
                addText(contentStream, createdBy.getEmail(), 525);
                addText(contentStream, createdBy.getPhoneNumber(), 505);

                String qrCodeData = ticket.getTicketQrCode();
                BufferedImage qrCodeImage;
                try {
                    qrCodeImage = QRCodeGenerator.getQRCodeImage(qrCodeData, 150, 150);
                } catch (WriterException e) {
                    throw new RuntimeException(e);
                }
                addImage(contentStream, document, qrCodeImage);
            }

            saveAndCloseDocument(document);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setFontAndColor(PDPageContentStream contentStream) throws IOException {
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 18);
        contentStream.setNonStrokingColor(1.0f, 1.0f, 1.0f); // RGB values for white
    }

    private void addText(PDPageContentStream contentStream, String text, float y) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset((float) 175, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void addImage(PDPageContentStream contentStream, PDDocument document, BufferedImage image) throws IOException {
        PDImageXObject qrCodeImage = LosslessFactory.createFromImage(document, image);
        contentStream.drawImage(qrCodeImage, (float) 420, (float) 150);
    }

    private void saveAndCloseDocument(PDDocument document) throws IOException {
        document.save(OUTPUT_PATH);
        document.close();
    }
}
