package com.ProyectoAula.GymAssist.services;

import org.springframework.stereotype.Service;

import com.ProyectoAula.GymAssist.repositories.AdminRepository;
import com.ProyectoAula.GymAssist.models.AdminEntity;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Optional;



@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    @Transactional
    public AdminEntity guardarAdmin(AdminEntity admin) {
        // Set relaciones bidireccionales si es necesario
        if (admin.getGimnasio() != null) {
            admin.getGimnasio().setAdminEntity(admin);
        }

        if (admin.getClientes() != null) {
            admin.getClientes().forEach(cliente -> cliente.setAdministrador(admin));
        }

        if (admin.getRutinas() != null) {
            admin.getRutinas().forEach(rutina -> rutina.setAdministrador(admin));
        }

        return adminRepository.save(admin);
    }

    public List<AdminEntity> obtenerTodos() {
        return adminRepository.findAll();
    }

    public Optional<AdminEntity> buscarPorId(Long id) {
        return adminRepository.findById(id);
    }
}
