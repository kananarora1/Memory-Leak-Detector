package com.example.devopsproj.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * Home controller for root endpoint.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    @ResponseBody
    public Map<String, Object> home() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "Java Memory Leak Detector");
        response.put("status", "running");
        response.put("version", "0.0.1-SNAPSHOT");
        response.put("documentation", "/swagger-ui.html");
        response.put("health", "/actuator/health");
        return response;
    }
}
