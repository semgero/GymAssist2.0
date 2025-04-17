package com.ProyectoAula.GymAssist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AdminController {

    @GetMapping("/AdminHome")
    public String adminHome() {
        return "AdminHome";  // thymeleaf: templates/admin/home.html
    }

    // Otras rutas privadas solo para ADMIN pueden ir aquí
}
