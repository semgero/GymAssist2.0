package com.ProyectoAula.GymAssist.mongoControllers;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

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

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, AdminService adminService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminService = adminService;
    }

    @GetMapping("/login")
    public String mostrarLoginForm() {
        return "login"; // <-- tu vista de login
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            // Redirige según el rol del usuario
            if (authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
                return "redirect:/Api/Admin/AdminHome";  // Admin es redirigido a su panel
            } else if (authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_CLIENTE"))) {
                return "redirect:/Api/Cliente/ClienteHome";  // Cliente es redirigido a su panel
            }
        }

        // Si no está autenticado correctamente, vuelve a la página de login
        return "redirect:/Api/Auth/login";
    }

    @GetMapping("/register")
    public String mostrarRegistroForm() {
        return "register"; // <-- tu vista de registro
    }

    @PostMapping("/register")
    public String registrarUsuario(@RequestParam String username,
                                   @RequestParam String email,
                                   @RequestParam String password) {
        // Verifica que el usuario no exista previamente
        if (userRepository.findByUsername(username).isPresent()) {
            return "redirect:/Api/Auth/register?error=usuarioYaExiste"; // Redirige si el usuario ya existe
        }
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ADMIN"); // Al registrarse es administrador
        userRepository.save(user);

        AdminEntity admin = new AdminEntity();
        admin.setId(new org.bson.types.ObjectId());
        adminService.save(admin); // Guarda el nuevo admin en la base de datos

        return "redirect:/Api/Auth/login";
    }
}