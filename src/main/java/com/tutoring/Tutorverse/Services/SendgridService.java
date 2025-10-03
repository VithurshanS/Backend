package com.tutoring.Tutorverse.Services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SendgridService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    public void sendContentUploadEmail(String toEmail, String subject, String contentText) throws IOException {
        Email from = new Email("tiran2018v@gmail.com"); // must be verified in SendGrid
        Email to = new Email(toEmail);
        Content content = new Content("text/plain", contentText);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());
        sg.api(request);
    }


    public void sendReminderEmail(String toEmail, String moduleName, LocalDateTime startTime) {
        Email from = new Email("tiran2018v@gmail.com");
        Email to = new Email(toEmail);
        String subject = "Reminder: Your class starts in 1 hour!";

        // Format the start time to a readable format (e.g., "2025-07-26 09:00 AM")
        String formattedTime = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));

        Content content = new Content("text/plain",
                "Dear Student,\n\nYour class '" + moduleName + "' starts at " + formattedTime + ".\nPlease be prepared!\n\n- Tutorex Team");

        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
