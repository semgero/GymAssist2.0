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
import com.ProyectoAula.GymAssist.mongoServices.ClienteService;
import org.springframework.ui.Model;


@Controller
@RequestMapping("/Api/Cliente")
public class ClientController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/ClienteHome")
    public String homeCliente(Model model, Principal principal) {
        String username = principal.getName();
        ClientEntity cliente = clienteService.buscarPorUsername(username);
        model.addAttribute("cliente", cliente);
        return "ClienteHome";
    }

    @PostMapping("/registrar-asistencia")
public String registrarAsistencia(@RequestParam("fecha") String fechaStr,
                                  @RequestParam("musculos") List<String> musculos,
                                  Principal principal) {
    ClientEntity cliente = clienteService.buscarPorUsername(principal.getName());
    LocalDate fecha = LocalDate.parse(fechaStr);
    clienteService.registrarAsistencia(cliente.getId(), fecha, musculos);
    return "redirect:/Api/Cliente/ClienteHome";
}
}
