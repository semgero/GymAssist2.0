package com.ProyectoAula.GymAssist.mongoRepository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.ProyectoAula.GymAssist.mongoModels.AdminEntity;
import com.ProyectoAula.GymAssist.mongoModels.UserEntity;

import org.bson.types.ObjectId;

public interface AdminRepository extends MongoRepository<AdminEntity, ObjectId> {
    // Ya no es necesario este método personalizado, puedes usar el findById de MongoRepository
    Optional<AdminEntity> findById(ObjectId id);
    Optional<AdminEntity> findByUserId(ObjectId userId);
}