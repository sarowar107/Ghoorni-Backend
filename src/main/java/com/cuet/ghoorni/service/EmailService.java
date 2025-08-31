package com.cuet.ghoorni.service;

import com.cuet.ghoorni.model.EmailVerificationToken;
import com.cuet.ghoorni.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(User user, EmailVerificationToken token) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper;
        try {
            helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("ghoorni.cuet@gmail.com");
            helper.setTo(user.getEmail());
            helper.setSubject("Verify Your Email Address - ghoorni CUET Portal");

            String verificationUrl = baseUrl + "/verify-email?token=" + token.getToken();

           String emailBody = String.format(
        "<!DOCTYPE html>" +
        "<html xmlns=\"http://www.w3.org/1999/xhtml\">" +
        "<head>" +
        "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" +
        "    <title>Email Verification</title>" +
        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"/>" +
        "    <style>" +
        "        body { margin: 0; padding: 0; background-color: #f4f6f8; font-family: Arial, sans-serif; }" +
        "        .container { max-width: 600px; margin: 40px auto; background: #ffffff; border-radius: 10px; box-shadow: 0 4px 12px rgba(0,0,0,0.1); overflow: hidden; }" +
        "        .header { background: linear-gradient(90deg,#2c3e50,#34495e); padding: 25px; text-align: center; }" +
        "        .header h1 { margin: 0; color: #fff; font-size: 26px; font-weight: bold; }" +
        "        .content { padding: 35px 30px; color: #333; }" +
        "        .content h2 { color: #2c3e50; margin-top: 0; font-size: 22px; }" +
        "        .content p { font-size: 15px; line-height: 1.6; margin: 12px 0; }" +
        "        .button-container { text-align: center; margin: 35px 0; }" +
        "        .button { " +
        "            background: linear-gradient(90deg, #3498db, #2980b9); " +
        "            color: #ffffff; " +
        "            padding: 14px 40px; " +
        "            font-size: 16px; " +
        "            font-weight: bold; " +
        "            text-decoration: none; " +
        "            border-radius: 50px; " +
        "            display: inline-block; " +
        "            box-shadow: 0 4px 12px rgba(0,0,0,0.15); " +
        "        }" +
        "        .footer { background: #ecf0f1; padding: 20px; text-align: center; font-size: 13px; color: #7f8c8d; }" +
        "    </style>" +
        "</head>" +
        "<body>" +
        "    <div class=\"container\">" +
        "        <div class=\"header\">" +
        "            <h1>ghoorni</h1>" +
        "        </div>" +
        "        <div class=\"content\">" +
        "            <h2>Welcome, %s!</h2>" +
        "            <p>Thanks for joining <strong>ghoorni - CUET Student Portal</strong>. To complete your registration, please verify your email address by clicking the button below:</p>" +
        "            <div class=\"button-container\">" +
        "                <a href=\"%s\" class=\"button\">Verify My Email</a>" +
        "            </div>" +
        "            <p><strong>Note:</strong> This link will expire in <strong>24 hours</strong> for security purposes.</p>" +
        "            <p>If you did not create this account, you can safely ignore this email.</p>" +
        "            <p>Best regards,<br/>" +
        "            <strong>The ghoorni Team</strong><br/>CUET Student Portal</p>" +
        "        </div>" +
        "        <div class=\"footer\">" +
        "            &copy; 2025 ghoorni. All rights reserved." +
        "        </div>" +
        "    </div>" +
        "</body>" +
        "</html>",
        user.getName(),
        verificationUrl);


            helper.setText(emailBody, true); // true flag indicates this is HTML
            mailSender.send(mimeMessage);
            System.out.println("Verification email sent successfully to: " + user.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send verification email to: " + user.getEmail());
            e.printStackTrace();
            // Don't throw exception - log the error but continue with signup process
            System.err.println("Email sending failed, but user registration will continue");
        }
    }

    public void sendWelcomeEmail(User user) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper;

        try {
            helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom("ghoorni.cuet@gmail.com");
            helper.setTo(user.getEmail());
            helper.setSubject("Welcome to ghoorni CUET Portal!");

            String emailBody = String.format(
                    "<!DOCTYPE html>" +
                            "<html>" +
                            "<head>" +
                            "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />" +
                            "    <style>" +
                            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                            "        .header { background-color: #2c3e50; color: white; padding: 20px; text-align: center; }"
                            +
                            "        .content { padding: 20px; background-color: #f9f9f9; }" +
                            "        .button { background-color: #3498db; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; }"
                            +
                            "        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }" +
                            "    </style>" +
                            "</head>" +
                            "<body>" +
                            "    <div class=\"container\">" +
                            "        <div class=\"header\">" +
                            "            <h1>Welcome to ghoorni!</h1>" +
                            "        </div>" +
                            "        <div class=\"content\">" +
                            "            <p>Dear <strong>%s</strong>,</p>" +
                            "            <p>Congratulations! Your email has been successfully verified.</p>" +
                            "            <p>You can now log in to your ghoorni account and access all the features of the CUET Student Portal.</p>"
                            +
                            "            <p style=\"text-align: center;\">" +
                            "                <a href=\"%s/login\" class=\"button\">Login to Your Account</a>" +
                            "            </p>" +
                            "            <p>Thank you for joining the ghoorni community!</p>" +
                            "            <p>Best regards,<br>" +
                            "            <strong>The ghoorni Team</strong><br>" +
                            "            CUET Student Portal</p>" +
                            "        </div>" +
                            "        <div class=\"footer\">" +
                            "            &copy; 2025 ghoorni. All rights reserved." +
                            "        </div>" +
                            "    </div>" +
                            "</body>" +
                            "</html>",
                    user.getName(),
                    baseUrl);

            helper.setText(emailBody, true); // true flag indicates this is HTML
            mailSender.send(mimeMessage);
            System.out.println("Welcome email sent successfully to: " + user.getEmail());
        } catch (MessagingException e) {
            System.err.println("Failed to send welcome email to: " + user.getEmail());
            e.printStackTrace();
            // Don't throw exception for welcome email failure
        }
    }
}
