package com.ProyectoAula.GymAssist.mongoControllers;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;
import com.ProyectoAula.GymAssist.mongoModels.AdminEntity;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.ClienteResumenDTO;
import com.ProyectoAula.GymAssist.mongoModels.GimnasiosEntity;
import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;
import com.ProyectoAula.GymAssist.mongoModels.UserEntity;
import com.ProyectoAula.GymAssist.mongoServices.AdminService;
import com.ProyectoAula.GymAssist.mongoServices.ClienteService;
import com.ProyectoAula.GymAssist.mongoRepository.AdminRepository;
import com.ProyectoAula.GymAssist.mongoRepository.GimnasiosRepository;
import com.ProyectoAula.GymAssist.mongoRepository.UserRepository;
import java.security.Principal;
import java.util.List;
import com.ProyectoAula.GymAssist.mongoServices.PlanService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/Api/Admin")
public class AdminController {

    @Autowired
    private HttpSession httpSession;

    private final ClienteService clienteService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final GimnasiosRepository gimnasiosRepository;
    private final PlanService planService;
    private final AdminService adminService;

    // Inyección de dependencias para ClienteService y BCryptPasswordEncoder
    public AdminController  (ClienteService clienteService, 
                            BCryptPasswordEncoder passwordEncoder,
                            UserRepository userRepository, AdminRepository adminRepository,
                            GimnasiosRepository gimnasiosRepository,
                            PlanService planService, AdminService adminService) {
            this.planService = planService;
            this.adminService = adminService;
            this.userRepository = userRepository;
            this.adminRepository = adminRepository;
            this.gimnasiosRepository = gimnasiosRepository;
            this.clienteService = clienteService;
            this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/check-session")
    public String checkSession() {
        return "ID de sesión: " + httpSession.getId();
    }

    @GetMapping("/AdminHome")
    public String mostrarAdminHome(Model model, Principal principal) {

        UserEntity user = userRepository.findByUsername(principal.getName()).orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        AdminEntity admin = adminRepository.findByUserId(user.getId()).orElse(null);

        model.addAttribute("nombreUsuario", user.getUsername());

        GimnasiosEntity gym = gimnasiosRepository.findByAdminId(admin.getId()).orElse(null);
        ClienteResumenDTO resumen = clienteService.obtenerResumenPorGym(gym.getId());
        model.addAttribute("resumen", resumen);
        model.addAttribute("adminId", admin != null ? admin.getId() : null);
        model.addAttribute("gymId", gym != null ? gym.getId() : null);

        if (gym != null) {
            List<PlanEntity> planes = planService.getPlanesByGimnasioId(gym.getId());
            model.addAttribute("planes", planes);
        }

        return "AdminHome";
    }

    @PostMapping("/actualizar-admin")
    public String actualizarCorreoYPasswordAdmin(@RequestParam String correo,
            @RequestParam(required = false) String password,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            adminService.actualizarCorreoYPasswordAdmin(correo, password, principal.getName());
            redirectAttributes.addFlashAttribute("exito", "Datos actualizados correctamente.");
            return "redirect:/Api/Auth/Logout";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/Api/Admin/CuentaAdmin"; // ✅ Volver a la página de cuenta si hay error
        }
    }

    // Mostrar la lista de clientes en AdminHome
    @GetMapping("/AdminRegister")
    public String mostrarAdminRegister(Model model, Principal principal) {
        UserEntity user = userRepository.findByUsername(principal.getName()).orElse(null);
        AdminEntity admin = adminRepository.findByUserId(user.getId()).orElse(null);
        GimnasiosEntity gym = gimnasiosRepository.findByAdminId(admin.getId()).orElse(null);
        ClienteResumenDTO resumen = clienteService.obtenerResumenPorGym(gym.getId());
        model.addAttribute("resumen", resumen);
        model.addAttribute("adminId", admin != null ? admin.getId() : null);
        model.addAttribute("gymId", gym != null ? gym.getId() : null);

        // Pasar la lista de clientes
        model.addAttribute("AdminRegister", clienteService.listarClientesPorGym(gym.getId()));
        if (gym != null) {
            List<PlanEntity> planes = planService.getPlanesByGimnasioId(gym.getId());
            model.addAttribute("planes", planes);
        }
        return "AdminRegister"; // Página de AdminHome
    }

    // Agregar nuevo cliente
    @PostMapping("/Register")
    public String agregarCliente(@RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam String idDocumento,
            @RequestParam Integer telefono,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam ObjectId planId,
            @RequestParam ObjectId gymId) {

        PlanEntity plan = planService.getPlanById(planId).orElse(null);
        String mensualidad = plan != null ? plan.getNombre() : "Desconocido";

        // Crear el cliente con el servicio
        clienteService.crearCliente(nombre, correo, idDocumento, telefono, mensualidad, username, password, gymId,
                planId);
        return "redirect:/Api/Admin/AdminRegister"; // Regresar a AdminHome
    }

    @GetMapping("/CuentaAdmin")
    public String CuentaAdmin() {
        return "CuentaAdmin"; // <-- tu vista de CuentaAdmin
    }

    @GetMapping("/pagar/{id}")
    public String pagarCliente(@PathVariable ObjectId id) {
        ClientEntity cliente = clienteService.buscarClientePorId(id);
        if (cliente.getEstado() == ClientEntity.EstadoCliente.PENDIENTE) {
            clienteService.cambiarEstadoCliente(id, ClientEntity.EstadoCliente.ACTIVO);
        }
        return "redirect:/Api/Admin/AdminRegister";
    }

    @GetMapping("/detener/{id}")
    public String detenerCliente(@PathVariable ObjectId id) {
        ClientEntity cliente = clienteService.buscarClientePorId(id);
        if (cliente.getEstado() == ClientEntity.EstadoCliente.ACTIVO) {
            clienteService.cambiarEstadoCliente(id, ClientEntity.EstadoCliente.PENDIENTE);
        }
        return "redirect:/Api/Admin/AdminRegister";
    }

    // Eliminar cliente
    @GetMapping("/suspender/{id}")
    public String suspenderCliente(@PathVariable ObjectId id) {
        clienteService.cambiarEstadoCliente(id, ClientEntity.EstadoCliente.SUSPENDIDO);
        return "redirect:/Api/Admin/AdminRegister";
    }

    @GetMapping("/activar/{id}")
    public String activarCliente(@PathVariable ObjectId id) {
        clienteService.cambiarEstadoCliente(id, ClientEntity.EstadoCliente.ACTIVO);
        return "redirect:/Api/Admin/AdminRegister";
    }
}
