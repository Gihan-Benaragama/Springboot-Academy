package com.springbootacademy.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @GetMapping("/")
    public String home() {
        return "redirect:/swagger-ui.html";
    }

    @GetMapping("/favicon.ico")
    public String favicon() {
        return "redirect:/swagger-ui/favicon-32x32.png";
    }
}
