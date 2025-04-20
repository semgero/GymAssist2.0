package com.ProyectoAula.GymAssist.services;

import com.ProyectoAula.GymAssist.models.GymEntity;
import com.ProyectoAula.GymAssist.repositories.GymRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GymService {

    private final GymRepository gymRepository;

    public GymEntity crearGym(GymEntity gym) {
        return gymRepository.save(gym);
    }

    public List<GymEntity> obtenerTodos() {
        return gymRepository.findAll();
    }

    public Optional<GymEntity> obtenerPorId(Long id) {
        return gymRepository.findById(id);
    }

    public GymEntity actualizarGym(Long id, GymEntity gymActualizado) {
        return gymRepository.findById(id).map(gym -> {
            gym.setNombre_gymnasio(gymActualizado.getNombre_gymnasio());
            gym.setDireccion(gymActualizado.getDireccion());
            gym.setRUT(gymActualizado.getRUT());
            gym.setFotos(gymActualizado.getFotos());
            gym.setDescripcion(gymActualizado.getDescripcion());
            gym.setAdminEntity(gymActualizado.getAdminEntity());
            return gymRepository.save(gym);
        }).orElse(null);
    }

    public void eliminarGym(Long id) {
        gymRepository.deleteById(id);
    }
}
