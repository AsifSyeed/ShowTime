package com.example.showtime.common.pdf;

import com.example.showtime.common.qr.QRCodeGenerator;
import com.example.showtime.s3.config.StorageConfig;
import com.example.showtime.s3.service.StorageService;
import com.example.showtime.ticket.model.entity.Ticket;
import com.google.zxing.WriterException;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class PdfGenerator {

    private final StorageService storageService;
    public byte[] generateTicketPdf(Ticket ticket) throws IOException {
        String fileName = "generic_ticket_" + ticket.getEventId() + "_" + ticket.getTicketCategory() + ".pdf";

        // Download the PDF template from S3
        byte[] pdfTemplate = storageService.downloadFile(fileName);
        // Load the PDF template

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PDDocument document = Loader.loadPDF(pdfTemplate)) {
            PDPage page = document.getPage(0);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                setFontAndColor(contentStream);

                addText(contentStream, ticket.getTicketOwnerName(), 450);
                addText(contentStream, ticket.getTicketOwnerEmail(), 430);
                addText(contentStream, ticket.getTicketOwnerNumber(), 410);
                addText(contentStream, ticket.getTicketId(), 320);

                String qrCodeData = ticket.getTicketId();
                BufferedImage qrCodeImage;
                try {
                    qrCodeImage = QRCodeGenerator.getQRCodeImage(qrCodeData, 185, 185);
                } catch (WriterException e) {
                    throw new RuntimeException(e);
                }
                addImage(contentStream, document, qrCodeImage);
            }

            // Instead of saving to a file, write to the ByteArrayOutputStream
            document.save(outputStream);
        }

        return outputStream.toByteArray();
    }

    private void setFontAndColor(PDPageContentStream contentStream) throws IOException {
        contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 18);
        // set black
        contentStream.setNonStrokingColor(0, 0, 0);
    }

    private void addText(PDPageContentStream contentStream, String text, float y) throws IOException {
        contentStream.beginText();
        contentStream.newLineAtOffset((float) 175, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void addImage(PDPageContentStream contentStream, PDDocument document, BufferedImage image) throws IOException {
        PDImageXObject qrCodeImage = LosslessFactory.createFromImage(document, image);
        contentStream.drawImage(qrCodeImage, (float) 347, (float) 117);
    }
}
