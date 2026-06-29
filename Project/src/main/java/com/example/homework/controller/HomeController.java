package com.example.homework.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    /*
     * Opening http://localhost:8080 now redirects to Swagger, instead of producing an error.
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/swagger-ui/index.html";
    }
}