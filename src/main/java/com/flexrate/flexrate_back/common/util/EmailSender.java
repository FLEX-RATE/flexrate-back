package com.flexrate.flexrate_back.common.util;

import com.flexrate.flexrate_back.common.exception.ErrorCode;
import com.flexrate.flexrate_back.common.exception.FlexrateException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;

@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender javaMailSender;

    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String sender;

    public void send(EmailMessage emailMessage) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();

            MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");

            messageHelper.setFrom(sender);

            messageHelper.setTo(emailMessage.getTo());
            messageHelper.setSubject(emailMessage.getSubject());

            String content = templateEngine.process(emailMessage.getTemplate(), emailMessage.getContext());

            messageHelper.setText(content, true);

            javaMailSender.send(message);

        } catch (MessagingException e) {
            throw new FlexrateException(ErrorCode.MESSAGING_EXCEPTION, e);
        }
    }
}
