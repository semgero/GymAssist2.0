package com.ProyectoAula.GymAssist.controller;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.ProyectoAula.GymAssist.controller.request.CreateUserDTO;
import com.ProyectoAula.GymAssist.models.ERole;
import com.ProyectoAula.GymAssist.models.RoleEntity;
import com.ProyectoAula.GymAssist.models.UserEntity;
import com.ProyectoAula.GymAssist.repositories.UserRepository;

import jakarta.validation.Valid;

@Controller
public class PaginasMapping {

    @Autowired
    private UserRepository userRepository;

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

    @PostMapping("/signup")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) { 
    
        Set<RoleEntity> roles = createUserDTO.getRoles().stream()
                .map(role -> RoleEntity.builder()
                        .name(ERole.valueOf(role))
                        .build())
                .collect(Collectors.toSet());

        UserEntity userEntity = UserEntity.builder()
                .username(createUserDTO.getUsername())
                .password(createUserDTO.getPassword())
                .email(createUserDTO.getEmail())
                .roles(roles)
                .build();

        userRepository.save(userEntity);

        return ResponseEntity.ok(userEntity);
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
