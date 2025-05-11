package com.ProyectoAula.GymAssist.mongoControllers;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
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

@Controller
@RequestMapping("/Api/Cliente")
public class ClientController {

    private final ClienteService clienteService;
    private final MedicionesService medicionesService;

    @Autowired
    public ClientController(ClienteService clienteService, MedicionesService medicionesService) {
        this.clienteService = clienteService;
        this.medicionesService = medicionesService;
    }

    @GetMapping("/ClienteHome")
    public String homeCliente(Model model, Principal principal) {
        String username = principal.getName();
        ClientEntity cliente = clienteService.buscarPorUsername(username);
        model.addAttribute("cliente", cliente);
        return "ClienteHome";
    }
    
    @PostMapping("/registrar-asistencia")
    public String registrarAsistencia(@ RequestParam("fecha") String fechaStr,
                                        @RequestParam("musculos") List<String> musculos,
                                        Principal principal) {
    ClientEntity cliente = clienteService.buscarPorUsername(principal.getName());
    LocalDate fecha = LocalDate.parse(fechaStr);
    clienteService.registrarAsistencia(cliente.getId(), fecha, musculos);
    return "redirect:/Api/Cliente/ClienteHome";
    }

    @GetMapping("/imc")
    public String mostrarFormularioIMC(Model model) {
        model.addAttribute("imc", null); // para evitar errores si accedes directo despues de terminar las pruebas colocar  ", null"
        return "Imc";
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
    return "Imc";
}
}