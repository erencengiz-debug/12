package com.sase.app.controller.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(@AuthenticationPrincipal Jwt jwt, Model model) {
        if (jwt != null) {
            model.addAttribute("email", jwt.getClaimAsString("email"));
            model.addAttribute("userId", jwt.getSubject());
        } else {
            model.addAttribute("userId", "");
        }
        model.addAttribute("activePage", "dashboard");
        return "index";
    }
}
