package com.cuet.ghoorni.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.net.Socket;

@RestController
public class TestController {

    @GetMapping("/test-smtp-connection")
    public String testSmtpConnection() {
        try {
            Socket socket = new Socket("smtp.gmail.com", 465);
            socket.close();
            return "Successfully connected to SMTP server";
        } catch (Exception e) {
            return "Failed to connect to SMTP server: " + e.getMessage();
        }
    }
}
