package com.ProyectoAula.GymAssist.mongoControllers;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ProyectoAula.GymAssist.mongoModels.AdminEntity;
import com.ProyectoAula.GymAssist.mongoModels.UserEntity;
import com.ProyectoAula.GymAssist.mongoRepository.ClientRepository;
import com.ProyectoAula.GymAssist.mongoRepository.UserRepository;
import com.ProyectoAula.GymAssist.mongoServices.AdminService;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;

@Controller
@RequestMapping("/Api/Auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminService adminService;
    private final ClientRepository clientRepository;

    public AuthController(UserRepository userRepository, ClientRepository clientRepository,
            PasswordEncoder passwordEncoder, AdminService adminService) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminService = adminService;
    }

    @GetMapping("/index")
    public String index() {
        return "redirect:index"; // <-- tu vista de index
    }

    @GetMapping("/login")
    public String mostrarLoginForm() {
        return "login"; // <-- tu vista de login
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        // Buscar usuario en la BD
        Optional<UserEntity> userOpt = userRepository.findByUsername(username);

        if (userOpt.isEmpty()) {
            return "redirect:/Api/Auth/login?error=usuarioNoEncontrado";
        }

        UserEntity user = userOpt.get();

        Optional<ClientEntity> clientOpt = clientRepository.findByUsername(username);
        if (clientOpt.isPresent()) {
            ClientEntity client = clientOpt.get();

            switch (client.getEstado()) {
                case SUSPENDIDO:
                    return "redirect:/Api/Auth/login?error=accesoDenegado";

                case PENDIENTE:
                    return "redirect:/Api/Cliente/ClientePago";

                case ACTIVO:
                    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                    if (authentication != null && authentication.isAuthenticated()) {
                        if (authentication.getAuthorities().stream()
                                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))) {
                            return "redirect:/Api/Admin/AdminHome";
                        } else if (authentication.getAuthorities().stream()
                                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_CLIENTE"))) {
                            return "redirect:/Api/Cliente/ClienteHome";
                        }
                    }
                    break;
            }
        }

        return "redirect:/Api/Auth/login?error=autenticacionFallida";
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
        admin.setUserId(user.getId()); // Asocia el ID del usuario al admin
        adminService.save(admin); // Guarda el nuevo admin en la base de datos

        return "redirect:/Api/Auth/login";
    }

}