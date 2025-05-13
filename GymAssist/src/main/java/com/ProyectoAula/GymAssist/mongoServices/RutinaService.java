package com.ProyectoAula.GymAssist.mongoServices;

import com.ProyectoAula.GymAssist.mongoModels.RutinaEntity;
import com.ProyectoAula.GymAssist.mongoRepository.RutinaRepository;
import org.springframework.stereotype.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

@Service
public class RutinaService {

    @Autowired
    private RutinaRepository rutinaRepository;

    public RutinaEntity createRutina(RutinaEntity rutina) {
        if (rutina.getFotosRutina() != null) {
            rutina.getFotosRutina().forEach(foto -> {
                if (foto.getImagenBase64() == null || foto.getImagenBase64().isEmpty()) {
                    throw new IllegalArgumentException("La imagen Base64 no puede estar vacía");
                }
            });
        }
        return rutinaRepository.save(rutina);
    }

    public List<RutinaEntity> getRutinasByGymId(ObjectId gymId) {
        return rutinaRepository.findByGymId(gymId);
    }
    
    public Optional<RutinaEntity> getRutinaById(ObjectId id) {
        return rutinaRepository.findById(id);
    }

    public RutinaEntity updateRutina(ObjectId id, RutinaEntity rutinaEntity) {
        RutinaEntity existingRutina = rutinaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Rutina no encontrada con id: " + id));

        if (rutinaEntity.getGrupoMuscular() != null) {
            existingRutina.setGrupoMuscular(rutinaEntity.getGrupoMuscular());
        }
        if (rutinaEntity.getRepeticiones() != null) {
            existingRutina.setRepeticiones(rutinaEntity.getRepeticiones());
        }
        if (rutinaEntity.getSeries() != null) {
            existingRutina.setSeries(rutinaEntity.getSeries());
        }
        if (rutinaEntity.getFotosRutina() != null) {
            existingRutina.setFotosRutina(rutinaEntity.getFotosRutina());
        }
        if (rutinaEntity.getGymId() != null) {
            existingRutina.setGymId(rutinaEntity.getGymId());
        }

        return rutinaRepository.save(existingRutina);
    }

    public void deleteRutinaById(ObjectId id) {
        if (!rutinaRepository.existsById(id)) {
            throw new RuntimeException("No se puede eliminar: Rutina no encontrada con id: " + id);
        }
        rutinaRepository.deleteById(id);
    }

    public List<RutinaEntity> findByNombreEjercicio(String nombreEjercicio) {
        return rutinaRepository.findByFotosRutina_NombreEjercicioIgnoreCase(nombreEjercicio);
    }

    public boolean hasValidImages(ObjectId id) {
        return rutinaRepository.findById(id)
            .map(rutina -> rutina.getFotosRutina().stream()
                .allMatch(foto -> foto.getImagenBase64() != null && !foto.getImagenBase64().isEmpty()))
            .orElse(false);
    }
}