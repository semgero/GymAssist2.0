package com.ProyectoAula.GymAssist.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HealthController.class);

    @GetMapping("/")
    public String healthCheck() {
        logger.info("Health check request received at root /");
        return "GymAssist API is running";
    }
}
