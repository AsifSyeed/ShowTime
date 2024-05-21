package com.example.showtime.email.service.impl;

import com.example.showtime.common.exception.BaseException;
import com.example.showtime.common.pdf.PdfGenerator;
import com.example.showtime.email.service.IEmailService;
import com.example.showtime.ticket.model.entity.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {
    private final PdfGenerator pdfGenerator;

    @Value("${spring.mail.username}")
    private String from;

    private final JavaMailSender javaMailSender;

    @Override
    public void sendTicketConfirmationMail(List<Ticket> transactionTicketList) {
        try {
            for (Ticket ticket : transactionTicketList) {
                sendPdf(ticket);
            }
        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
        }
    }

    @Override
    public void sendGenericEmail(String email, String subject, String htmlContent) {
        // Send OTP to the user's email
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(email);
            mimeMessageHelper.setSubject(subject);

            mimeMessageHelper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void sendPdf(Ticket ticket) {
        try {
            byte[] pdfBytes = pdfGenerator.generateTicketPdf(ticket);

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(ticket.getTicketOwnerEmail());
            mimeMessageHelper.setSubject("Ticket for " + ticket.getEventName());

            // Set HTML content
            String htmlContent = "<p style=\"font-size: 16px;\"><strong>Dear " + ticket.getTicketOwnerName() + ",</strong></p>"
                    + "<p>Your ticket has been attached to this email. Please check the attached file.</p>"
                    + "<p>Regards,<br/>Relevant Bangladesh</p>";

            mimeMessageHelper.setText(htmlContent, true);

            // Create a DataSource from PDF byte array
            ByteArrayDataSource dataSource = new ByteArrayDataSource(pdfBytes, "application/pdf");

            // Use the QR code as part of the filename, for uniqueness
            String filename = ticket.getTicketId() + "-" + ticket.getTicketOwnerName() + ".pdf";
            mimeMessageHelper.addAttachment(filename, dataSource);

            javaMailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


//    private List<Ticket> getTicketsByTransactionRefNo(String transactionRefNo) {
//        return ticketService.getTicketListByTransactionRefNo(transactionRefNo);
//    }
//
//    private TransactionItem getTransaction(String email, String transactionRefNo) {
//        return transactionService.getTransactionByUserEmailAndTransactionRefNo(email, transactionRefNo);
//    }
}
