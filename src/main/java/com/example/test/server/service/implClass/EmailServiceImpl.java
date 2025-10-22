package com.example.test.server.service.implClass;

import com.example.test.common.constant.EmailConstant;
import com.example.test.server.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationCode(String toEmail, String code) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
      //  System.out.println(fromEmail+"=========================================");
        message.setTo(toEmail);
        message.setSubject(EmailConstant.EMAIL_PART1);
        message.setText(EmailConstant.EMAIL_PART2 + code + EmailConstant.EMAIL_PART3);
        mailSender.send(message);
    }
}
