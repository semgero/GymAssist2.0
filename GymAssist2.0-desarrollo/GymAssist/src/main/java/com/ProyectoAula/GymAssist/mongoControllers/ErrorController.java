package com.ProyectoAula.GymAssist.mongoControllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {
    
    @GetMapping("/Error")
    public String Error() {
        return "Error";  // thymeleaf: templates/error/errorPage.html
    }

    @GetMapping("/error403")
    public String error403() {
        return "error403";  // thymeleaf: templates/error/accessDenied.html
    }
}
