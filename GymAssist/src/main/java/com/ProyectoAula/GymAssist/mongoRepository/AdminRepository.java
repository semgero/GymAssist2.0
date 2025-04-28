package com.ProyectoAula.GymAssist.mongoRepository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.ProyectoAula.GymAssist.mongoModels.AdminEntity;

public interface AdminRepository extends MongoRepository<AdminEntity, String> {
    Optional<AdminEntity> findByUsername(String username);
}