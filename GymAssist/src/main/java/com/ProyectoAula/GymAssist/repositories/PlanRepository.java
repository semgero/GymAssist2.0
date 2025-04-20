package com.ProyectoAula.GymAssist.repositories;

import com.ProyectoAula.GymAssist.models.PlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<PlanEntity, Long> {
}
