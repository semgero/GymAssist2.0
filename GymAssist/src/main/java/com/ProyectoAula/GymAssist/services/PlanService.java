package com.ProyectoAula.GymAssist.services;

import com.ProyectoAula.GymAssist.models.GymEntity;
import com.ProyectoAula.GymAssist.models.PlanEntity;
import com.ProyectoAula.GymAssist.repositories.GymRepository;
import com.ProyectoAula.GymAssist.repositories.PlanRepositoryy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepositoryy planRepository;
    private final GymRepository gymRepository;

    public PlanEntity crearPlan(PlanEntity plan) {
        if (plan.getGimnasio() != null) {
            Optional<GymEntity> gimnasio = gymRepository.findById(plan.getGimnasio().getId());
            gimnasio.ifPresent(plan::setGimnasio);
        }
        return planRepository.save(plan);
    }

    public List<PlanEntity> obtenerPlanes() {
        return planRepository.findAll();
    }

    public Optional<PlanEntity> obtenerPlanPorId(Long id) {
        return planRepository.findById(id);
    }

    public PlanEntity editarPlan(Long id, PlanEntity actualizado) {
        return planRepository.findById(id).map(plan -> {
            actualizado.setId(id);
            return planRepository.save(actualizado);
        }).orElse(null);
    }

    public void eliminarPlan(Long id) {
        planRepository.deleteById(id);
    }
}
