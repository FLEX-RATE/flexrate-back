package com.flexrate.flexrate_back.common.config;

import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;


@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class EmailConfig {

    @Bean
    public JavaMailSender javaMailSender(MailProperties props) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(props.getHost());
        sender.setPort(props.getPort());
        sender.setUsername(props.getUsername());
        sender.setPassword(props.getPassword());

        // 기존 Properties 객체에 Map 값 일괄 추가
        sender.getJavaMailProperties().putAll(props.getProperties());
        return sender;
    }
}