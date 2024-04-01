package com.example.showtime.common.email;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailSendService {
    private JavaMailSender javaMailSender;

    public void sendEmail(String to, String subject, String text) {
        // send email
    }
}
