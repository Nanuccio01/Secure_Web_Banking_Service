package com.example.secure_web_banking_service.controller;

import com.example.secure_web_banking_service.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final JwtService jwtService;

    public HomeController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/home")
    public String home(HttpServletRequest request) {

        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("jwt".equals(c.getName())) {
                    token = c.getValue();
                    break;
                }
            }
        }

        if (token == null || !jwtService.isTokenValid(token)) {
            return "redirect:/login.html";
        }

        return "home"; // templates/home.html
    }
}

