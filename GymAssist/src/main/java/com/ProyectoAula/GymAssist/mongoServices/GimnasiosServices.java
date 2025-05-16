package com.ProyectoAula.GymAssist.mongoServices;

import com.ProyectoAula.GymAssist.mongoModels.GimnasiosEntity;
import com.ProyectoAula.GymAssist.mongoRepository.GimnasiosRepository;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class GimnasiosServices {
    
    @Autowired
    private GimnasiosRepository gimnasiosRepository;

    public GimnasiosEntity createGym(GimnasiosEntity gimnasio) {
        if(gimnasiosRepository.findByAdminId(gimnasio.getAdminId()).isPresent()){
            throw new RuntimeException("Ya existe un gimnasio con ese ID de administrador.");
        }
        return gimnasiosRepository.save(gimnasio);
    }

    public Optional<GimnasiosEntity> getGymByAdminId(ObjectId adminId) {
        return gimnasiosRepository.findByAdminId(adminId);
    }

    public GimnasiosEntity updateGym(ObjectId adminId, GimnasiosEntity gimnasio) {
        GimnasiosEntity existingGimnasio = gimnasiosRepository.findByAdminId(adminId)
                .orElseThrow(() -> new RuntimeException("Gimnasio no encontrado."));
                existingGimnasio.setNombreGymnasio(gimnasio.getNombreGymnasio());
                existingGimnasio.setDireccion(gimnasio.getDireccion());
                existingGimnasio.setDescripcion(gimnasio.getDescripcion());
                existingGimnasio.setFotos(gimnasio.getFotos());
                existingGimnasio.setRut(gimnasio.getRut());
        return gimnasiosRepository.save(existingGimnasio);
    }

    public void deleteGymByAdminId(ObjectId adminId) {
        gimnasiosRepository.deleteByAdminId(adminId);
    }

    public boolean adminHasGym(ObjectId adminId) {
        return gimnasiosRepository.findByAdminId(adminId).isPresent();
    }

    public Optional<GimnasiosEntity> getGymById(ObjectId gymId) {
        return gimnasiosRepository.findById(gymId);
    }
}
