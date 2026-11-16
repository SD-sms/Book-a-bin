// Package declaration removed to ensure the file compiles when the source
// file is located outside the project's configured source root.

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class InstructionsController {

    /**
     * Show instructions page for a specific role
     * Example: /instructions/user, /instructions/collector, /instructions/bbmp,
     * /instructions/admin
     */
    @GetMapping("/instructions/{role}")
    public String instructions(@PathVariable String role, Model model) {
        model.addAttribute("role", role.toLowerCase());
        // Redirect to a static HTML page and pass the role as a query parameter.
        // This avoids view-resolution problems (circular view paths) by serving
        // the static resource directly from /static.
        return "redirect:/role-instructions.html?role=" + role.toLowerCase();
    }

}
