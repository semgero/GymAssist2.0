package com.ProyectoAula.GymAssist.mongoControllers;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.GimnasiosEntity;
import com.ProyectoAula.GymAssist.mongoModels.MedicionesEntity;
import com.ProyectoAula.GymAssist.mongoServices.ClienteService;
import com.ProyectoAula.GymAssist.mongoServices.GimnasiosServices;

import org.springframework.ui.Model;
import com.ProyectoAula.GymAssist.mongoServices.MedicionesService;
import com.ProyectoAula.GymAssist.mongoServices.PlanService;
import com.ProyectoAula.GymAssist.mongoServices.RutinaService;

@Controller
@RequestMapping("/Api/Cliente")
public class ClientController {

    private final ClienteService clienteService;
    private final MedicionesService medicionesService;
    private final RutinaService rutinaService;
    private final PlanService planService;
    private final GimnasiosServices gimnasiosServices;

    @Autowired
    public ClientController(ClienteService clienteService, MedicionesService medicionesService,
            RutinaService rutinaService, PlanService planService, GimnasiosServices gimnasiosServices) {
        this.clienteService = clienteService;
        this.rutinaService = rutinaService;
        this.medicionesService = medicionesService;
        this.planService = planService;
        this.gimnasiosServices = gimnasiosServices;
    }

    @GetMapping("/ClienteHome")
    public String ClienteHome(Model model, Principal principal) {
        String username = principal.getName();
        ClientEntity cliente = clienteService.buscarPorUsername(username); // tu método para encontrar al cliente

        LocalDate ultimaFecha = cliente.getAsistencias().isEmpty()
                ? null
                : cliente.getAsistencias().get(cliente.getAsistencias().size() - 1).getFecha();

        model.addAttribute("ultimaFechaAsistencia", ultimaFecha != null ? ultimaFecha.toString() : "");

        model.addAttribute("cliente", cliente);
        return "ClienteHome";
    }

    @GetMapping("/ClienteRutinas")
    public String ClienteRutinas() {
        return "ClienteRutinas";
    }

    @GetMapping("/ClienteCuenta")
    public String ClienteCuenta(Model model, Principal principal) {
        String username = principal.getName(); // obtiene el username del usuario logueado
        ClientEntity cliente = clienteService.buscarPorUsername(username); // tu método para encontrar al cliente

        // Obtiene el nombre del plan (si tiene uno)
        String nombrePlan = planService.obtenerNombreDelPlan(cliente.getPlanId());

        model.addAttribute("cliente", cliente);
        model.addAttribute("nombrePlan", nombrePlan);
        return "ClienteCuenta"; // plantilla ClienteCuenta.html
    }

    @GetMapping("/ClientePago")
    public String ClientePago(Model model, Principal principal) {
        String username = principal.getName(); // obtiene el username del usuario logueado
        ClientEntity cliente = clienteService.buscarPorUsername(username); // tu método para encontrar al cliente

        // Obtiene el nombre del plan (si tiene uno)
        String nombrePlan = planService.obtenerNombreDelPlan(cliente.getPlanId());
        String planPrecio = planService.obtenerPrecioDelPlan(cliente.getPlanId());  

        model.addAttribute("cliente", cliente);
        model.addAttribute("nombrePlan", nombrePlan);
        model.addAttribute("planPrecio", planPrecio);
        return "ClientePago"; // plantilla ClienteCuenta.html
    }

    @PostMapping("/actualizar-datos")
    public String actualizarDatosCliente(@RequestParam String correo,
            @RequestParam String username,
            @RequestParam(required = false) String password,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            clienteService.actualizarDatosPersonales(correo, username, password, principal.getName());
            redirectAttributes.addFlashAttribute("exito", "Datos actualizados correctamente.");
            return "redirect:/Api/Auth/Logout"; // Forzar logout después de cambio de username
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/Api/Cliente/ClienteCuenta";
        }
    }

    @GetMapping("/ClienteAsistencia")
    public String asistencia(Model model, Principal principal) {
        String username = principal.getName(); // El usuario logueado
        ClientEntity cliente = clienteService.buscarPorUsername(username);

        ObjectId gymId = cliente.getGymId();
        Optional<GimnasiosEntity> gymOpt = gimnasiosServices.getGymById(gymId);

        String nombreGimnasio = gymOpt.map(GimnasiosEntity::getNombreGymnasio)
                .orElse("Gimnasio no encontrado");

        model.addAttribute("nombreGimnasio", nombreGimnasio);
        model.addAttribute("cliente", cliente);

        // Puedes pasar más atributos si lo deseas
        return "ClienteAsistencia"; // o el nombre correcto de tu vista
    }

    @PostMapping("/registrar-asistencia")
    public String registrarAsistencia(@RequestParam("fecha") String fechaStr,
            @RequestParam("musculos") List<String> musculos,
            Principal principal) {
        ClientEntity cliente = clienteService.buscarPorUsername(principal.getName());
        LocalDate fecha = LocalDate.parse(fechaStr);
        clienteService.registrarAsistencia(cliente.getId(), fecha, musculos);
        return "redirect:/Api/Cliente/ClienteAsistencia";
    }

    @GetMapping("/Clienteimc")
    public String mostrarFormularioIMC(Model model, Principal principal) {
        ClientEntity cliente = clienteService.buscarPorUsername(principal.getName());
        ObjectId clienteId = cliente.getId();

        Optional<MedicionesEntity> ultimaMedicion = medicionesService.obtenerUltimaMedicion(clienteId);

        if (ultimaMedicion.isPresent()) {
            MedicionesEntity medicion = ultimaMedicion.get();
            double imc = medicionesService.calcularIMC(medicion.getPeso(), medicion.getEstatura());
            String resultado = medicionesService.interpretarIMC(imc);
            model.addAttribute("imc", imc);
            model.addAttribute("resultado", resultado);
        } else {
            model.addAttribute("imc", null);
        }

        // Agregar historial de mediciones
        List<MedicionesEntity> historial = medicionesService.obtenerHistorial(clienteId);
        model.addAttribute("mediciones", historial);

        return "Clienteimc";
    }

    @PostMapping("/imc/guardar")
    public String guardarIMC(@RequestParam Double peso,
            @RequestParam Double estatura,
            Principal principal,
            Model model) {
        ClientEntity cliente = clienteService.buscarPorUsername(principal.getName());

        // Asegúrate de pasar el tipo correcto según la definición de guardarMedicion
        MedicionesEntity medicion = medicionesService.guardarMedicion(peso, estatura, cliente.getId());
        double imc = medicionesService.calcularIMC(peso, estatura); // ✅
        String resultado = medicionesService.interpretarIMC(imc); // ✅

        model.addAttribute("imc", imc);
        model.addAttribute("resultado", resultado);
        return "redirect:/Api/Cliente/Clienteimc";
    }
}