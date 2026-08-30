package com.medtime.controller;

import com.medtime.entity.Role;
import com.medtime.entity.User;
import com.medtime.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Controller
public class HomeController {

    private final AuthService authService;

    public HomeController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/")
    public String index(Model model) {
        Optional<User> currentUser = authService.getCurrentAuthenticatedUser();
        if (currentUser.isPresent()) {
            User user = currentUser.get();
            if (user.getRole() == Role.DOCTOR) {
                return "redirect:/doctor/dashboard";
            } else if (user.getRole() == Role.PATIENT) {
                return "redirect:/patient/dashboard";
            }
        }
        return "home";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/login")
    public String login() {
        Optional<User> currentUser = authService.getCurrentAuthenticatedUser();
        if (currentUser.isPresent()) {
            return currentUser.get().getRole() == Role.DOCTOR ? "redirect:/doctor/dashboard" : "redirect:/patient/dashboard";
        }
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }
}
