package com.ProyectoAula.GymAssist.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ProyectoAula.GymAssist.models.UserEntity;


@Repository
public interface UserRepositoryy extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
}
