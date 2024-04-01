package com.example.showtime.email.service.impl;

import com.example.showtime.common.exception.BaseException;
import com.example.showtime.email.service.IEmailService;
import com.example.showtime.ticket.model.entity.Ticket;
import com.example.showtime.ticket.service.ITicketService;
import com.example.showtime.transaction.model.entity.TransactionItem;
import com.example.showtime.transaction.model.request.CheckTransactionStatusRequest;
import com.example.showtime.transaction.service.ITransactionService;
import com.example.showtime.user.model.entity.UserAccount;
import com.example.showtime.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    @Value("${spring.mail.username}")
    private String from;

    private final JavaMailSender javaMailSender;
    private final IUserService userService;
    private final ITransactionService transactionService;
    private final ITicketService ticketService;

    @Override
    public void sendTicketConfirmationMail(CheckTransactionStatusRequest checkTransactionStatusRequest) {

        String transactionRefNo = validateRequest(checkTransactionStatusRequest);

        try {
            List<Ticket> transactionTicketList = getTicketsByTransactionRefNo(transactionRefNo);

            for (int i = 0; i < transactionTicketList.size(); i++) {
                sendPdf(transactionTicketList.get(i));
            }

        } catch (AccessDeniedException e) {
            throw new BaseException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized Access");
        }
    }

    private String validateRequest(CheckTransactionStatusRequest request) {

        if (Objects.isNull(request) ||
                StringUtils.isEmpty(request.getTransactionRefNo())) {

            throw new BaseException(HttpStatus.BAD_REQUEST.value(), "Request body is not valid");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String createdByUserEmail = authentication.getName();

        UserAccount createdBy = userService.getUserByEmail(createdByUserEmail);

        if (getTransaction(createdBy.getEmail(), request.getTransactionRefNo()) == null) {
            throw new BaseException(HttpStatus.NOT_FOUND.value(), "Transaction not found");
        }

        return request.getTransactionRefNo();
    }

    private void sendPdf(Ticket ticket) {
        try {
            File file = new File(ticket.getTicketFilePath());

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(ticket.getTicketOwnerEmail());
            mimeMessageHelper.setSubject("Ticket for" + ticket.getEventName());

            // Set HTML content
            String htmlContent = "<p style=\"font-size: 16px;\"><strong>Hello " + ticket.getTicketOwnerName() + ",<strong></p>"
                    + "<p>Your ticket has been attached to this email. Please, check attached file.</p>"
                    + "<p>Regards,<br/>Relevant Bangladesh</p>";

            mimeMessageHelper.setText(htmlContent, true);

            mimeMessageHelper.addAttachment(file.getName(), file);

            javaMailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Ticket> getTicketsByTransactionRefNo(String transactionRefNo) {
        return ticketService.getTicketListByTransactionRefNo(transactionRefNo);
    }

    private TransactionItem getTransaction(String email, String transactionRefNo) {
        return transactionService.getTransactionByUserEmailAndTransactionRefNo(email, transactionRefNo);
    }
}
