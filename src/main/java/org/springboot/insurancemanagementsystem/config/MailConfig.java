package org.springboot.insurancemanagementsystem.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    @ConditionalOnProperty(prefix = "spring.mail", name = "host")
    public JavaMailSender javaMailSender(
            @Value("${spring.mail.host}") String host,
            @Value("${spring.mail.port:587}") int port,
            @Value("${spring.mail.username}") String username,
            @Value("${spring.mail.password}") String password,
            @Value("${spring.mail.protocol:smtp}") String protocol,
            @Value("${spring.mail.default-encoding:UTF-8}") String encoding,
            @Value("${spring.mail.properties.mail.smtp.auth:true}") boolean auth,
            @Value("${spring.mail.properties.mail.smtp.starttls.enable:true}") boolean startTls,
            @Value("${spring.mail.properties.mail.smtp.starttls.required:false}") boolean startTlsRequired,
            @Value("${spring.mail.properties.mail.smtp.ssl.trust:}") String sslTrust,
            @Value("${spring.mail.properties.mail.smtp.connectiontimeout:10000}") int connectionTimeout,
            @Value("${spring.mail.properties.mail.smtp.timeout:10000}") int timeout,
            @Value("${spring.mail.properties.mail.smtp.writetimeout:10000}") int writeTimeout
    ) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        mailSender.setProtocol(protocol);
        mailSender.setDefaultEncoding(encoding);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.smtp.auth", auth);
        props.put("mail.smtp.starttls.enable", startTls);
        props.put("mail.smtp.starttls.required", startTlsRequired);
        if (!sslTrust.isBlank()) {
            props.put("mail.smtp.ssl.trust", sslTrust);
        }
        props.put("mail.smtp.connectiontimeout", connectionTimeout);
        props.put("mail.smtp.timeout", timeout);
        props.put("mail.smtp.writetimeout", writeTimeout);

        return mailSender;
    }
}


