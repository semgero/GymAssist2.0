package com.ProyectoAula.GymAssist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;

import com.ProyectoAula.GymAssist.Dto.RegisterDto;
import com.ProyectoAula.GymAssist.models.ERole;
import com.ProyectoAula.GymAssist.services.UserService;

import lombok.RequiredArgsConstructor;



@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                 @RequestParam(value = "logout", required = false) String logout,
                                 Model model) {
        if (error != null) {
            model.addAttribute("error", "Credenciales incorrectas");
        }
        if (logout != null) {
            model.addAttribute("message", "Has cerrado sesión correctamente.");
        }
        return "/login"; // thymeleaf: templates/auth/login.html
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        model.addAttribute("roles", ERole.values());
        return "/register"; // thymeleaf: templates/auth/register.html
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute RegisterDto dto, Model model) {
        try {
            userService.registerUser(dto);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("registerDto", dto);
            model.addAttribute("roles", ERole.values());
            model.addAttribute("error", e.getMessage());
            return "/register";
        }
    }
}
