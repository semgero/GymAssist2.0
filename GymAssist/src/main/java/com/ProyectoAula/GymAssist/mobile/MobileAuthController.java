package com.ProyectoAula.GymAssist.mobile;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ProyectoAula.GymAssist.mongoModels.AdminEntity;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.UserEntity;
import com.ProyectoAula.GymAssist.mongoRepository.AdminRepository;
import com.ProyectoAula.GymAssist.mongoRepository.ClientRepository;
import com.ProyectoAula.GymAssist.mongoRepository.GimnasiosRepository;
import com.ProyectoAula.GymAssist.mongoRepository.UserRepository;

/**
 * Login de la app Flutter.
 *   POST /api/m/auth/login     {username, password}          -> {token, ...sesión}
 *   POST /api/m/auth/register  {username, email, password}   -> registra un ADMIN (igual que la web)
 *   GET  /api/m/auth/me                                       -> datos de la sesión actual
 */
@RestController
@RequestMapping("/api/m/auth")
public class MobileAuthController {

    private final UserRepository users;
    private final AdminRepository admins;
    private final ClientRepository clients;
    private final GimnasiosRepository gyms;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final MobileContext ctx;

    public MobileAuthController(UserRepository users, AdminRepository admins, ClientRepository clients,
            GimnasiosRepository gyms, PasswordEncoder encoder, JwtService jwt, MobileContext ctx) {
        this.users = users;
        this.admins = admins;
        this.clients = clients;
        this.gyms = gyms;
        this.encoder = encoder;
        this.jwt = jwt;
        this.ctx = ctx;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "").trim();
        String password = body.getOrDefault("password", "");
        UserEntity u = users.findByUsername(username).orElse(null);
        if (u == null || u.getPassword() == null || !encoder.matches(password, u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
        }
        Map<String, Object> s = sesion(u);
        s.put("token", jwt.generar(u.getUsername(), u.getRole()));
        return s;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "").trim();
        String email = body.getOrDefault("email", "").trim().toLowerCase();
        String password = body.getOrDefault("password", "");
        if (username.isEmpty() || email.isEmpty() || password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Completa usuario, correo y una contraseña de al menos 6 caracteres");
        }
        if (users.existsByUsername(username)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese usuario ya existe");
        if (users.existsByEmail(email)) throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado");

        UserEntity u = new UserEntity();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(encoder.encode(password));
        u.setRole("ADMIN");
        u = users.save(u);

        AdminEntity a = new AdminEntity();
        a.setId(new ObjectId());
        a.setUserId(u.getId());
        admins.save(a);

        Map<String, Object> s = sesion(u);
        s.put("token", jwt.generar(u.getUsername(), u.getRole()));
        return s;
    }

    @GetMapping("/me")
    public Map<String, Object> me() {
        return sesion(ctx.usuario());
    }

    private Map<String, Object> sesion(UserEntity u) {
        Map<String, Object> s = new LinkedHashMap<>();
        s.put("username", u.getUsername());
        s.put("email", u.getEmail());
        s.put("role", u.getRole());
        if ("ADMIN".equals(u.getRole())) {
            var gym = ctx.gymDe(u);
            s.put("tieneGimnasio", gym.isPresent());
            s.put("gimnasio", gym.map(MobileJson::gym).orElse(null));
        } else if ("CLIENTE".equals(u.getRole())) {
            ClientEntity c = clients.findByUsername(u.getUsername()).orElse(null);
            if (c != null) {
                s.put("cliente", MobileJson.cliente(c, ctx.planDe(c).orElse(null)));
                var gymId = ctx.gymIdDe(c);
                s.put("gimnasio", gymId == null ? null : gyms.findById(gymId).map(MobileJson::gym).orElse(null));
            }
        }
        return s;
    }
}
