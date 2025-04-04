package com.ProyectoAula.GymAssist.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ProyectoAula.GymAssist.models.RoleEntity;
import com.ProyectoAula.GymAssist.models.ERole;



@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, Long>{

    Optional<RoleEntity> findByName(ERole name);
}
