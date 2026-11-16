package com.example.bookabin.controller;

import org.springframework.stereotype.Controller;
// Example for controller files
import org.springframework.web.bind.annotation.*;

@Controller
public class WelcomeController {

    @GetMapping("/")
    public String home() {
        return "welcome"; // maps to templates/welcome.html
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // maps to templates/login.html
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register"; // maps to templates/register.html
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied"; // maps to templates/access-denied.html
    }
}
