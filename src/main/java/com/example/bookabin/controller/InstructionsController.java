package com.example.bookabin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
// Example for controller files
import org.springframework.web.bind.annotation.*;

@Controller
public class InstructionsController {

    /**
     * Show instructions page for a specific role
     * Example: /instructions/user, /instructions/collector, /instructions/bbmp,
     * /instructions/admin
     */
    @GetMapping("/instructions/{role}")
    public String instructions(@PathVariable String role, Model model) {
        // Pass the role to the template
        model.addAttribute("role", role.toLowerCase());
        // templates/role-instructions.html exists in the project; return that template
        return "role-instructions"; // role-instructions.html
    }

}
