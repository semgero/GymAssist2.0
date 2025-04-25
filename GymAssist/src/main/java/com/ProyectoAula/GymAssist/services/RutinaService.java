package com.ProyectoAula.GymAssist.services;

import com.ProyectoAula.GymAssist.models.RutinaEntity;
import com.ProyectoAula.GymAssist.repositories.RutinaRepositoryy;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RutinaService {

    private final RutinaRepositoryy rutinaRepository;

    // Crea una nueva rutina y la guarda en la base de datos.
    public RutinaEntity crearRutina(RutinaEntity rutina) {
        return rutinaRepository.save(rutina);
    }

    // Retorna todas las rutinas almacenadas.
    public List<RutinaEntity> obtenerRutinas() {
        return rutinaRepository.findAll();
    }

    // Retorna una rutina por su ID o lanza un 404 Not Found.
    public RutinaEntity obtenerRutinaPorId(Long id) {
        return rutinaRepository.findById(id)
              .orElseThrow(() -> new ResponseStatusException(
                  HttpStatus.NOT_FOUND, "Rutina no encontrada para el ID: " + id));
    }

    // Actualiza una rutina existente y retorna la rutina actualizada.
    public RutinaEntity actualizarRutina(Long id, RutinaEntity rutinaActualizada) {
        return rutinaRepository.findById(id)
            .map(rutina -> {
                rutina.setGrupo_muscular(rutinaActualizada.getGrupo_muscular());
                rutina.setFotos(rutinaActualizada.getFotos());
                rutina.setSeries(rutinaActualizada.getSeries());
                rutina.setRepeticiones(rutinaActualizada.getRepeticiones());
                return rutinaRepository.save(rutina);
            })
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Rutina no encontrada para el ID: " + id));
    }

    // Elimina una rutina según su ID o lanza un 404 si no existe.
    public void eliminarRutina(Long id) {
        if (!rutinaRepository.existsById(id)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Rutina no encontrada para el ID: " + id);
        }
        rutinaRepository.deleteById(id);
    }
}