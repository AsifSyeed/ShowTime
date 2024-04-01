package com.example.showtime.common.service.impl;

import com.example.showtime.common.service.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmailService implements IEmailService {

    @Value("${spring.mail.username}")
    private String from;

    private JavaMailSender javaMailSender;

    @Override
    public String sendMail(MultipartFile[] file, String to, String[] cc, String subject, String body) {
        try {

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
            mimeMessageHelper.setFrom(from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setCc(cc);
            mimeMessageHelper.setSubject(subject);
            mimeMessageHelper.setText(body);

            for (MultipartFile multipartFile : file) {
                mimeMessageHelper.addAttachment(
                        Objects.requireNonNull(multipartFile.getOriginalFilename()),
                        new ByteArrayDataSource(multipartFile.getBytes(), Objects.requireNonNull(multipartFile.getContentType())));
            }

            javaMailSender.send(mimeMessage);

            return "mail sent";

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
