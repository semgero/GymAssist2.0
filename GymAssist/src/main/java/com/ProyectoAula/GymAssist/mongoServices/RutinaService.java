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
        return rutinaRepository.save(rutina);
    }

    public List<RutinaEntity> getRutinasByGymId(ObjectId gymId) {
        return rutinaRepository.findByGymId(gymId);
    }
    
    public Optional<RutinaEntity> getRutinaById(ObjectId id) {
        return rutinaRepository.findById(id);
    }

    public RutinaEntity updateRutina(ObjectId id, RutinaEntity rutinaEntity){
        RutinaEntity existingRutina = rutinaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Rutina not found with id: " + id));
        existingRutina.setGrupoMuscular(rutinaEntity.getGrupoMuscular());
        existingRutina.setRepeticiones(rutinaEntity.getRepeticiones());
        existingRutina.setSeries(rutinaEntity.getSeries());
        existingRutina.setFotosRutina(rutinaEntity.getFotosRutina()); // Cambiado aquí
        existingRutina.setGymId(rutinaEntity.getGymId());
        return rutinaRepository.save(existingRutina);
    }

    public void deleteRutinaById(ObjectId id) {
        rutinaRepository.deleteById(id);
    }
}
