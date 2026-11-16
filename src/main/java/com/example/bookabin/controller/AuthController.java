// src/main/java/com/example/bookabin/controller/AuthController.java
package com.example.bookabin.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    public AuthController() {
    }

    @GetMapping("/redirect")
    public String redirectAfterLogin(Authentication authentication) {
        if (authentication == null)
            return "redirect:/login";
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        } else if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BBMP_COLLECTOR"))) {
            return "redirect:/bbmp/dashboard";
        } else if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_COLLECTOR"))) {
            return "redirect:/collector/dashboard";
        } else {
            return "redirect:/user/dashboard";
        }
    }
}