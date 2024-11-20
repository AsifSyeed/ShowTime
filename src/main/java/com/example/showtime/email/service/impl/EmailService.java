package com.example.showtime.email.service.impl;

import com.example.showtime.common.exception.BaseException;
import com.example.showtime.common.pdf.PdfGenerator;
import com.example.showtime.email.service.IEmailService;
import com.example.showtime.ticket.model.entity.Ticket;
import com.example.showtime.user.model.entity.UserAccount;
import com.example.showtime.user.repository.UserRepository;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {
    private final PdfGenerator pdfGenerator;
    private final UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String from;

    private final JavaMailSender javaMailSender;

    @Override
    public void sendTicketConfirmationMail(List<Ticket> transactionTicketList) {
        try {
            sendPdf(transactionTicketList);
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
            System.out.println("Not valid email");
        }
    }

    private void sendPdf(List<Ticket> tickets) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(tickets.get(0).getTicketCreatedBy());
            mimeMessageHelper.setSubject("Ticket for " + tickets.get(0).getEventName());

            Optional<UserAccount> user = userRepository.findByEmail(tickets.get(0).getTicketCreatedBy());

            if (user.isEmpty()) {
                throw new BaseException(HttpStatus.NOT_FOUND.value(), "User not found");
            }

            // Set HTML content
            String htmlContent = "<p style=\"font-size: 16px;\"><strong>Dear " + user.get().getFirstName() + ",</strong></p>"
                    + "<p>Your ticket has been attached to this email. Please check the attached file(s).</p>"
                    + "<p>Regards,<br/>Relevant Bangladesh</p>";

            mimeMessageHelper.setText(htmlContent, true);

            for (Ticket ticket : tickets) {
                byte[] pdf = pdfGenerator.generateTicketPdf(ticket);
                ByteArrayDataSource dataSource = new ByteArrayDataSource(pdf, "application/pdf");
                mimeMessageHelper.addAttachment("ticket_" + ticket.getTicketId() + ".pdf", dataSource);
            }

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
