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
import org.springframework.ui.Model;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoServices.ClienteService;

@Controller
@RequestMapping("/Api/Admin")
public class AdminController {

    private final ClienteService clienteService;
    private final BCryptPasswordEncoder passwordEncoder;

    // Inyección de dependencias para ClienteService y BCryptPasswordEncoder
    @Autowired
    public AdminController(ClienteService clienteService, BCryptPasswordEncoder passwordEncoder) {
        this.clienteService = clienteService;
        this.passwordEncoder = passwordEncoder;
    }

    // Mostrar la lista de clientes en AdminHome
    @GetMapping("/AdminHome")
    public String mostrarAdminHome(Model model) {
        model.addAttribute("AdminHome", clienteService.listarClientes());
        return "AdminHome"; // Página de AdminHome
    }
    
    // Agregar nuevo cliente
    @PostMapping("/Home")
    public String agregarCliente(@RequestParam String nombre,
                                 @RequestParam String correo,
                                 @RequestParam String telefono,
                                 @RequestParam String mensualidad,
                                 @RequestParam String username,
                                 @RequestParam String password) {

        // Crear el cliente con el servicio
        clienteService.crearCliente(nombre, correo, telefono, mensualidad, username, password);
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
    @GetMapping("/delete/{id}")
    public String eliminarCliente(@PathVariable ObjectId id) {
        clienteService.eliminarCliente(id);
        return "redirect:/Api/Admin/AdminHome"; // Regresar a AdminHome
    }
}
