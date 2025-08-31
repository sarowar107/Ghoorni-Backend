package com.cuet.ghoorni.service;

import com.cuet.ghoorni.model.EmailVerificationToken;
import com.cuet.ghoorni.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendVerificationEmail(User user, EmailVerificationToken token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ghoorni.cuet@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject("Verify Your Email Address - ghoorni CUET Portal");

        String verificationUrl = baseUrl + "/verify-email?token=" + token.getToken();

        String emailBody = String.format(
        "<!DOCTYPE html>" +
        "<html>" +
        "<head>" +
        "    <meta charset='UTF-8'>" +
        "    <style>" +
        "        body { font-family: Arial, sans-serif; background-color: #f9f9f9; padding: 0; margin: 0; }" +
        "        .container { max-width: 600px; margin: 40px auto; background: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); }" +
        "        h1 { color: #2c3e50; text-align: center; margin-bottom: 20px; }" +
        "        p { font-size: 15px; color: #555555; line-height: 1.6; }" +
        "        .button { display: block; width: 220px; margin: 25px auto; padding: 12px 20px; text-align: center; background-color: #4CAF50; color: #ffffff; text-decoration: none; font-size: 16px; font-weight: bold; border-radius: 5px; }" +
        "        .footer { margin-top: 30px; text-align: center; font-size: 13px; color: #888888; }" +
        "    </style>" +
        "</head>" +
        "<body>" +
        "    <div class='container'>" +
        "        <h1>Welcome to ghoorni!</h1>" +
        "        <p>Dear <b>%s</b>,</p>" +
        "        <p>Welcome to <b>ghoorni - CUET Student Portal</b>! We're excited to have you on board. Please click the button below to verify your email address and activate your account:</p>" +
        "        <a href='%s' class='button'>Verify My Email</a>" +
        "        <p><b>Note:</b> This link will expire in <b>24 hours</b> for security purposes.</p>" +
        "        <p>If you didn’t create an account with us, please ignore this email.</p>" +
        "        <p>Best regards,<br>" +
        "        <b>The ghoorni Team</b><br>" +
        "        CUET Student Portal</p>" +
        "        <div class='footer'>© 2025 ghoorni. All rights reserved.</div>" +
        "    </div>" +
        "</body>" +
        "</html>",
        user.getName(),
        verificationUrl
);


        message.setText(emailBody);

        try {
            mailSender.send(message);
            System.out.println("Verification email sent successfully to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send verification email to: " + user.getEmail());
            e.printStackTrace();
            // Don't throw exception - log the error but continue with signup process
            System.err.println("Email sending failed, but user registration will continue");
        }
    }

    public void sendWelcomeEmail(User user) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ghoorni.cuet@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject("Welcome to ghoorni CUET Portal!");

        String emailBody = String.format(
                "Dear %s,\n\n" +
                        "Congratulations! Your email has been successfully verified.\n\n" +
                        "You can now log in to your ghoorni account and access all the features of the CUET Student Portal.\n\n"
                        +
                        "Visit: %s/login\n\n" +
                        "Thank you for joining the ghoorni community!\n\n" +
                        "Best regards,\n" +
                        "The ghoorni Team\n" +
                        "CUET Student Portal",
                user.getName(),
                baseUrl);

        message.setText(emailBody);

        try {
            mailSender.send(message);
            System.out.println("Welcome email sent successfully to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send welcome email to: " + user.getEmail());
            e.printStackTrace();
            // Don't throw exception for welcome email failure
        }
    }
}
