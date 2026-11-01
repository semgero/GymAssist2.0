package com.ProyectoAula.GymAssist.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ProyectoAula.GymAssist.models.ModeloRequest;
import com.ProyectoAula.GymAssist.models.ModeloResponse;
import com.ProyectoAula.GymAssist.mongoModels.AdminEntity;
import com.ProyectoAula.GymAssist.mongoModels.GimnasiosEntity;
import com.ProyectoAula.GymAssist.mongoModels.UserEntity;
import com.ProyectoAula.GymAssist.mongoRepository.AdminRepository;
import com.ProyectoAula.GymAssist.mongoRepository.GimnasiosRepository;
import com.ProyectoAula.GymAssist.mongoRepository.UserRepository;
import com.ProyectoAula.GymAssist.mongoServices.PythonOptimizationService;
import org.springframework.ui.Model;
import java.security.Principal;

@Controller
@RequestMapping("/modelo")
public class ModeloController {
    
    @Autowired
    private PythonOptimizationService service;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private GimnasiosRepository gimnasiosRepository;

    @GetMapping
    public String vistaModelo(Model model, Principal principal) {

        UserEntity user = userRepository
                .findByUsername(principal.getName())
                .orElse(null);

        if(user == null){
            return "redirect:/login";
        }

        AdminEntity admin = adminRepository
                .findByUserId(user.getId())
                .orElse(null);

        GimnasiosEntity gym = gimnasiosRepository
                .findByAdminId(admin.getId())
                .orElse(null);

        model.addAttribute("gymId", gym.getId());
        model.addAttribute("adminId", admin.getId());

        model.addAttribute("request", new ModeloRequest());

        return "modelo";
    }

    @PostMapping("/calcular")
    public String calcular(
            @ModelAttribute("request") ModeloRequest request,
            Model model,
            Principal principal
    ) {

        ModeloResponse resultado = service.resolver(request);

        model.addAttribute("resultado", resultado);
        model.addAttribute("request", request);

        return "modelo";
    }
}
