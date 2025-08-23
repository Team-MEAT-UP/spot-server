package com.meetup.server.global.email;

import com.meetup.server.global.email.dto.EmailSendRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender mailSender;

    public void send(EmailSendRequest emailSendRequest) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(emailSendRequest.emailAddress());
        helper.setSubject(emailSendRequest.subject());
        helper.setText(emailSendRequest.content(), true);

        mailSender.send(message);
    }
}
