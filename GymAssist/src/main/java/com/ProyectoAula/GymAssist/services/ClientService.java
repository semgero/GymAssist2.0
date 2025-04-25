package com.ProyectoAula.GymAssist.services;

import com.ProyectoAula.GymAssist.models.ClientEntity;
import com.ProyectoAula.GymAssist.models.PlanEntity;
import com.ProyectoAula.GymAssist.repositories.ClientRepositoryy;
import com.ProyectoAula.GymAssist.repositories.PlanRepositoryy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClientService {

    private final ClientRepositoryy clientRepository;
    private final PlanRepositoryy planRepository;

    public ClientEntity crearCliente(ClientEntity cliente) {
        if (cliente.getPlan() != null) {
            PlanEntity plan = planRepository.findById(cliente.getPlan().getId())
                .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));
            
            if (plan.getGimnasio().getId() != cliente.getGimnasio().getId()) {
                throw new IllegalArgumentException("El plan no pertenece al mismo gimnasio que el cliente");
            }
            
            cliente.setPlan(plan);
            plan.setCliente(cliente);
        }
        return clientRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClientEntity> obtenerClientes() {
        return clientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<ClientEntity> obtenerClientePorId(Long id) {
        return clientRepository.findById(id);
    }

    public ClientEntity editarCliente(Long id, ClientEntity actualizado) {
        return clientRepository.findById(id)
            .map(clienteExistente -> {
                actualizado.setId(id);
                
                if (actualizado.getPlan() != null) {
                    PlanEntity plan = planRepository.findById(actualizado.getPlan().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Plan no encontrado"));
                    
                    if (plan.getGimnasio().getId() != actualizado.getGimnasio().getId()) {
                        throw new IllegalArgumentException("El plan no pertenece al mismo gimnasio que el cliente");
                    }
                    
                    actualizado.setPlan(plan);
                    plan.setCliente(actualizado);
                }
                
                return clientRepository.save(actualizado);
            })
            .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
    }

    public void eliminarCliente(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new IllegalArgumentException("Cliente no encontrado");
        }
        clientRepository.deleteById(id);
    }
}