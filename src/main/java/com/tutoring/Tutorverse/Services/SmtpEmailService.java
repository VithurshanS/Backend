package com.tutoring.Tutorverse.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SmtpEmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendReminderEmail(String toEmail, String moduleName, LocalDateTime startTime) throws Exception {
        String formattedTime = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Reminder: Your class starts in 1 hour!");
        message.setText("Dear Student,\n\nYour class '" + moduleName + "' starts at " + formattedTime + ".\nPlease be prepared!\n\n- Tutorverse Team");
        
        mailSender.send(message);
    }

    public void sendContentUploadEmail(String toEmail, String subject, String contentText) throws Exception {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(contentText);
        
        mailSender.send(message);
    }
}