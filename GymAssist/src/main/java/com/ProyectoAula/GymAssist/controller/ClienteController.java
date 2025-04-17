package com.ProyectoAula.GymAssist.controller;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequiredArgsConstructor
public class ClienteController {
    
    @GetMapping("/ClienteHome")
    public String clientHome() {
        return "ClienteHome";  // thymeleaf: templates/client/home.html
    }

    // Otras rutas privadas solo para CLIENTE pueden ir aquí
}
