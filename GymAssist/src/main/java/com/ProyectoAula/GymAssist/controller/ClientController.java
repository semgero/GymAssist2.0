package com.ProyectoAula.GymAssist.controller;

import com.ProyectoAula.GymAssist.models.ClientEntity;
import com.ProyectoAula.GymAssist.services.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ClientEntity crear(@RequestBody ClientEntity cliente) {
        return clientService.crearCliente(cliente);
    }

    @GetMapping
    public List<ClientEntity> obtenerTodos() {
        return clientService.obtenerClientes();
    }

    @GetMapping("/{id}")
    public ClientEntity obtenerPorId(@PathVariable Long id) {
        return clientService.obtenerClientePorId(id).orElse(null);
    }

    @PutMapping("/{id}")
    public ClientEntity editar(@PathVariable Long id, @RequestBody ClientEntity actualizado) {
        return clientService.editarCliente(id, actualizado);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        clientService.eliminarCliente(id);
    }
}
