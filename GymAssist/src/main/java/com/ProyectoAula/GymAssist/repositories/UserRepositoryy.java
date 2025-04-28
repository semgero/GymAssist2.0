package com.ProyectoAula.GymAssist.repositories;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ProyectoAula.GymAssist.models.UserEntity;


@Repository
public interface UserRepositoryy extends JpaRepository<UserEntity, ObjectId> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

}
