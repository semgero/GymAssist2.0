package com.ProyectoAula.GymAssist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PaginasMapping {

    @GetMapping("/index")
    public String index() {
        return "index"; 
    }

    @GetMapping("/planes")
    public String planes() {
        return "planes"; 
    }

    @GetMapping("/login")
    public String login() {
        return "login"; 
    }

    @GetMapping("/signup")
    public String signup() {
        return "signup"; 
    }

    @GetMapping("/rutinas")
    public String rutinas() {
        return "rutinas";
    }

    @GetMapping("/pecho")
    public String pecho() {
        return "pecho"; 
    }

    @GetMapping("/espalda")
    public String espalda() {
        return "espalda"; 
    }

    @GetMapping("/bicep")
    public String bicep() {
        return "bicep"; 
    }

    @GetMapping("/tricep")
    public String tricep() {
        return "tricep"; 
    }

    @GetMapping("/femoral")
    public String femoral() {
        return "femoral"; 
    }

    @GetMapping("/cuadricep")
    public String cuadricep() {
        return "cuadricep"; 
    }

    @GetMapping("/crear-gimnasio")
    public String creargimnasio() {
        return "crear-gimnasio"; 
    }

    @GetMapping("/server")
    public String server() {
        return "server"; 
    }

    @GetMapping("/pagospagina")
    public String pagos() {
        return "pagos"; 
    }

    @GetMapping("/plan1")
    public String plan1() {
        return "plan1"; 
    }

    @GetMapping("/plan2")
    public String plan2() {
        return "plan2"; 
    }

    @GetMapping("/plan3")
    public String plan3() {
        return "plan3"; 
    }

    @GetMapping("/configuracion")
    public String configuracion() {
        return "configuracion"; 
    }

    @GetMapping("/crearserver")
    public String crearserver() {
        return "crearserver"; 
    }
}
