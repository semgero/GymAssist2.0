package com.ProyectoAula.GymAssist.mongoControllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/Api/Cliente")
public class ClientController {

    @GetMapping("/ClienteHome")
    public String homeCliente() {
        return "ClienteHome"; // <-- Tu vista para el cliente (puede ser básica)
    }
}
