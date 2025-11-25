package com.ProyectoAula.GymAssist.mongoControllers;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ProyectoAula.GymAssist.mongoModels.AdminEntity;
import com.ProyectoAula.GymAssist.mongoModels.UserEntity;
import com.ProyectoAula.GymAssist.mongoRepository.UserRepository;
import com.ProyectoAula.GymAssist.mongoServices.AdminService;

@Controller
@RequestMapping("/Api/Auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminService adminService;

    public AuthController(UserRepository userRepository,
            PasswordEncoder passwordEncoder, AdminService adminService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminService = adminService;
    }

    @GetMapping("/index")
    public String index() {
        return "redirect:index";
    }

    @GetMapping("/login")
    public String mostrarLoginForm() {
        return "login";
    }

    // ==========================================
    // ❌ ELIMINADO @PostMapping("/login")
    // ==========================================
    // Spring Security maneja el login automáticamente
    // con los handlers configurados en SecurityConfig:
    // - successHandler() → redirige con ?success=true
    // - failureHandler() → redirige con ?error=true
    // ==========================================

    @PostMapping("/register")
    public String registrarUsuario(@RequestParam String username,
            @RequestParam String email,
            @RequestParam String password) {

        // Verifica que el usuario no exista previamente
        if (userRepository.findByUsername(username).isPresent()) {
            return "redirect:/Api/Auth/login?usernameExists=true";
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return "redirect:/Api/Auth/login?emailExists=true";
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return "redirect:/Api/Auth/login?emailExists=true";
        }

        // Crear nuevo usuario
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ADMIN");
        userRepository.save(user);

        AdminEntity admin = new AdminEntity();
        admin.setId(new org.bson.types.ObjectId());
        admin.setUserId(user.getId());
        adminService.save(admin);
        admin.setUserId(user.getId());
        adminService.save(admin);

        return "redirect:/Api/Auth/login?registered=true";
    }
}