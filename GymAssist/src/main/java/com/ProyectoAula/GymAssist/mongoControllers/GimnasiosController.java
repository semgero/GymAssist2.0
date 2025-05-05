package com.ProyectoAula.GymAssist.mongoControllers;

import com.ProyectoAula.GymAssist.mongoModels.GimnasiosEntity;
import com.ProyectoAula.GymAssist.mongoModels.AdminEntity;
import com.ProyectoAula.GymAssist.mongoModels.UserEntity;
import com.ProyectoAula.GymAssist.mongoServices.GimnasiosServices;
import com.ProyectoAula.GymAssist.mongoRepository.AdminRepository;
import com.ProyectoAula.GymAssist.mongoRepository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@Controller
@RequestMapping("/gimnasios")
public class GimnasiosController {

    @Autowired
    private GimnasiosServices gimnasiosServices;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("fotos");
    }

    // Mostrar formulario de registro de gimnasio
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("gimnasio", new GimnasiosEntity());
        return "gymRegister";
    }

    // Procesar registro de gimnasio
    @PostMapping("/register")
    public String registerGym(@ModelAttribute GimnasiosEntity gimnasio,
            @RequestParam("fotos") MultipartFile fotos,
            Principal principal,
            Model model) {
        try {
            String username = principal.getName();
            UserEntity user = userRepository.findByUsername(username).orElseThrow();
            AdminEntity admin = adminRepository.findByUserId(user.getId()).orElseThrow();

            gimnasio.setAdminId(admin.getId());
            gimnasio.setFotos(fotos.getBytes());

            gimnasiosServices.createGym(gimnasio);

            return "redirect:/Api/Admin/AdminHome";
        } catch (Exception e) {
            e.printStackTrace(); // <-- Esto mostrará el error en la consola
            model.addAttribute("error", true);
            return "gymRegister";
        }
    }
}