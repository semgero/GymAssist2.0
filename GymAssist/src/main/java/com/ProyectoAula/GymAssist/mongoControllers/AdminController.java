package com.ProyectoAula.GymAssist.mongoControllers;

import org.bson.types.ObjectId;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import com.ProyectoAula.GymAssist.mongoModels.AdminEntity;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.GimnasiosEntity;
import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;
import com.ProyectoAula.GymAssist.mongoModels.UserEntity;
import com.ProyectoAula.GymAssist.mongoServices.ClienteService;
import com.ProyectoAula.GymAssist.mongoRepository.AdminRepository;
import com.ProyectoAula.GymAssist.mongoRepository.GimnasiosRepository;
import com.ProyectoAula.GymAssist.mongoRepository.UserRepository;
import java.security.Principal;
import java.util.List;
import com.ProyectoAula.GymAssist.mongoServices.PlanService;

@Controller
@RequestMapping("/Api/Admin")
public class AdminController {

    private final ClienteService clienteService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final GimnasiosRepository gimnasiosRepository;
    private final PlanService planService;

    // Inyección de dependencias para ClienteService y BCryptPasswordEncoder
    public AdminController(ClienteService clienteService, BCryptPasswordEncoder passwordEncoder,
            UserRepository userRepository, AdminRepository adminRepository, GimnasiosRepository gimnasiosRepository,
            PlanService planService) {
        this.planService = planService;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
        this.gimnasiosRepository = gimnasiosRepository;
        this.clienteService = clienteService;
        this.passwordEncoder = passwordEncoder;
    }

    // Mostrar la lista de clientes en AdminHome
    @GetMapping("/AdminHome")
    public String mostrarAdminHome(Model model, Principal principal) {
        UserEntity user = userRepository.findByUsername(principal.getName()).orElse(null);
        AdminEntity admin = adminRepository.findByUserId(user.getId()).orElse(null);
        GimnasiosEntity gym = gimnasiosRepository.findByAdminId(admin.getId()).orElse(null);
        model.addAttribute("adminId", admin != null ? admin.getId() : null);
        model.addAttribute("gymId", gym != null ? gym.getId() : null);

        // Pasar la lista de clientes
        model.addAttribute("AdminHome", clienteService.listarClientesPorGym(gym.getId()));
        if (gym != null) {
            List<PlanEntity> planes = planService.getPlanesByGimnasioId(gym.getId());
            model.addAttribute("planes", planes);
        }
        return "AdminHome"; // Página de AdminHome
    }

    // Agregar nuevo cliente
    @PostMapping("/Home")
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
        return "redirect:/Api/Admin/AdminHome"; // Regresar a AdminHome
    }

    // Mostrar formulario de edición de cliente
    @GetMapping("/edit/{id}")
    public String mostrarEditarCliente(@PathVariable ObjectId id, Model model) {
        ClientEntity cliente = clienteService.buscarClientePorId(id);
        model.addAttribute("cliente", cliente);
        return "EditarCliente"; // Página de edición de cliente
    }

    // Actualizar cliente
    @PostMapping("/update/{id}")
    public String actualizarCliente(@PathVariable ObjectId id,
            @RequestParam String nombre,
            @RequestParam String correo,
            @RequestParam String telefono,
            @RequestParam String mensualidad,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password) {

        // Si la contraseña fue proporcionada, cifrarla
        String encodedPassword = (password != null && !password.isEmpty()) ? passwordEncoder.encode(password) : null;

        clienteService.actualizarCliente(id, nombre, correo, telefono, mensualidad, username, encodedPassword);
        return "redirect:/Api/Admin/AdminHome"; // Regresar a AdminHome
    }

    // Eliminar cliente
    @GetMapping("/suspender/{id}")
    public String suspenderCliente(@PathVariable ObjectId id) {
        clienteService.cambiarEstadoCliente(id, ClientEntity.EstadoCliente.SUSPENDIDO);
        return "redirect:/Api/Admin/AdminHome";
    }

    @GetMapping("/activar/{id}")
    public String activarCliente(@PathVariable ObjectId id) {
        clienteService.cambiarEstadoCliente(id, ClientEntity.EstadoCliente.ACTIVO);
        return "redirect:/Api/Admin/AdminHome";
    }
}
