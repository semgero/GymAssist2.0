package com.ProyectoAula.GymAssist.mongoServices;

import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;
import com.ProyectoAula.GymAssist.mongoRepository.PlanRepository;
import org.springframework.stereotype.Service;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    public PlanEntity createPlan(PlanEntity plan) {
        return planRepository.save(plan);
    }

    public List<PlanEntity> getPlanesByGimnasioId(ObjectId gymId) {
        return planRepository.findByGymId(gymId);
    }

    public Optional<PlanEntity> getPlanById(ObjectId id) {
        return planRepository.findById(id);
    }

    public PlanEntity updatePlan(ObjectId id, PlanEntity planDetails) {
        PlanEntity existingPlan = planRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Plan no encontrado con id: " + id));
        existingPlan.setNombre(planDetails.getNombre());
        existingPlan.setDescripcion(planDetails.getDescripcion());
        existingPlan.setPrecio(planDetails.getPrecio());
        existingPlan.setDuracion(planDetails.getDuracion());
        existingPlan.setBeneficios(planDetails.getBeneficios());
        existingPlan.setGymId(planDetails.getGymId());
        return planRepository.save(existingPlan);
    }

    public void deletePlanById(ObjectId id) {
        planRepository.deleteById(id);
    }
}