package com.ProyectoAula.GymAssist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.ProyectoAula.GymAssist.models.GymEntity;

public interface GymRepository extends JpaRepository<GymEntity, Long> {

}