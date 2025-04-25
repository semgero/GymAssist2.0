package com.ProyectoAula.GymAssist.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ProyectoAula.GymAssist.models.AdminEntity;

@Repository
public interface AdminRepositoryy extends JpaRepository<AdminEntity, Long>{
}
