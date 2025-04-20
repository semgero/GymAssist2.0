package com.ProyectoAula.GymAssist.controller;

import com.ProyectoAula.GymAssist.models.GymEntity;
import com.ProyectoAula.GymAssist.repositories.GymRepository;
import com.ProyectoAula.GymAssist.services.GymService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/gyms")
@RequiredArgsConstructor
public class GymController {

    private final GymRepository gymRepository;
    private final GymService gymService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GymEntity crear(@RequestBody GymEntity gym) {
        return gymService.crearGym(gym);
    }

    @GetMapping
    public List<GymEntity> obtenerTodos() {
        return gymService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public GymEntity obtenerPorId(@PathVariable Long id) {
        return gymService.obtenerPorId(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Gimnasio con ID " + id + " no encontrado"));
    }

    @PutMapping("/{id}")
    public GymEntity actualizarGym(Long id, GymEntity gymActualizado) {
        return gymRepository.findById(id)
                .map(gym -> {
                    gym.setNombre_gymnasio(gymActualizado.getNombre_gymnasio());
                    gym.setDireccion(gymActualizado.getDireccion());
                    gym.setRUT(gymActualizado.getRUT());
                    gym.setFotos(gymActualizado.getFotos());
                    gym.setDescripcion(gymActualizado.getDescripcion());
                    gym.setAdminEntity(gymActualizado.getAdminEntity());
                    return gymRepository.save(gym);
                })
                .orElseThrow(() -> new RuntimeException(
                        "No se puede actualizar - Gimnasio con ID " + id + " no encontrado"));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        gymService.eliminarGym(id);
    }
}