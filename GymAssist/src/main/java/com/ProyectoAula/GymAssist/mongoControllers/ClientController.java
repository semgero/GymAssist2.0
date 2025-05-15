package com.ProyectoAula.GymAssist.mongoControllers;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.MedicionesEntity;
import com.ProyectoAula.GymAssist.mongoServices.ClienteService;
import org.springframework.ui.Model;
import com.ProyectoAula.GymAssist.mongoServices.MedicionesService;
import com.ProyectoAula.GymAssist.mongoServices.RutinaService;

@Controller
@RequestMapping("/Api/Cliente")
public class ClientController {

    private final ClienteService clienteService;
    private final MedicionesService medicionesService;
    private final RutinaService rutinaService;

    @Autowired
    public ClientController(ClienteService clienteService, MedicionesService medicionesService, RutinaService rutinaService) {
        this.clienteService = clienteService;
        this.rutinaService = rutinaService;
        this.medicionesService = medicionesService;
    }

    @GetMapping("/ClienteHome")
    public String ClienteHome(){
        return "ClienteHome";
    }

    @GetMapping("/ClienteRutinas")
    public String ClienteRutinas(){
        return "ClienteRutinas";
    }

    @GetMapping("/ClienteDashboard")
    public String ClienteDashboard(){
        return "ClienteDashboard";
    }

    @GetMapping("/Asistencia")
    public String asistencia(Model model, Principal principal) {
        String username = principal.getName();
        ClientEntity cliente = clienteService.buscarPorUsername(username);
        model.addAttribute("cliente", cliente);
        return "Asistencia";
    }
    
    @PostMapping("/registrar-asistencia")
    public String registrarAsistencia(@ RequestParam("fecha") String fechaStr,
                                        @RequestParam("musculos") List<String> musculos,
                                        Principal principal) {
    ClientEntity cliente = clienteService.buscarPorUsername(principal.getName());
    LocalDate fecha = LocalDate.parse(fechaStr);
    clienteService.registrarAsistencia(cliente.getId(), fecha, musculos);
    return "redirect:/Api/Cliente/Asistencia";
    }

    @GetMapping("/imc")
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

        return "imc";
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
    String resultado = medicionesService.interpretarIMC(imc);   // ✅

    model.addAttribute("imc", imc);
    model.addAttribute("resultado", resultado);
    return "redirect:/Api/Cliente/imc";
    }
}