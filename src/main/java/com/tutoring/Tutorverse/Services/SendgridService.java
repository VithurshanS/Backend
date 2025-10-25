package com.tutoring.Tutorverse.Services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SendgridService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.template.contentUpload}")
    private String contentUploadTemplateId;

    @Value("${sendgrid.template.reminder}")
    private String reminderTemplateId;

    public void sendContentUploadEmail(String toEmail, String firstName, String materialTitle, String materialUrl) throws IOException {
        Email from = new Email("tutorwars236@gmail.com"); // must be verified in SendGrid
        Email to = new Email(toEmail);

        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setSubject("New Learning Material Uploaded!");
        mail.setTemplateId(contentUploadTemplateId); // dynamic template ID from your config

        // Add personalization with dynamic data
        Personalization personalization = new Personalization();
        personalization.addTo(to);
        personalization.addDynamicTemplateData("firstName", firstName);
        personalization.addDynamicTemplateData("materialTitle", materialTitle);
        personalization.addDynamicTemplateData("materialUrl", materialUrl);


        mail.addPersonalization(personalization);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        System.out.println("SendGrid Response Code: " + response.getStatusCode());
        System.out.println("SendGrid Response Body: " + response.getBody());

        if (response.getStatusCode() >= 400) {
            throw new IOException("SendGrid API Error: " + response.getStatusCode() + " - " + response.getBody());
        }
    }






    public void sendReminderEmail(String toEmail, String moduleName, LocalDateTime startTime) throws IOException {
        Email from = new Email("tutorwars236@gmail.com");
        Email to = new Email(toEmail);

        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setTemplateId(reminderTemplateId); // use dynamic template ID

        // Format the date for display
        String formattedTime = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"));

        // Add personalization data (matches SendGrid template variables)
        Personalization personalization = new Personalization();
        personalization.addTo(to);
        personalization.addDynamicTemplateData("moduleName", moduleName);
        personalization.addDynamicTemplateData("startTime", formattedTime);


        mail.addPersonalization(personalization);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        System.out.println("SendGrid Response Code: " + response.getStatusCode());
        System.out.println("SendGrid Response Body: " + response.getBody());

        if (response.getStatusCode() >= 400) {
            throw new IOException("SendGrid API Error: " + response.getStatusCode() + " - " + response.getBody());
        }
    }


}
