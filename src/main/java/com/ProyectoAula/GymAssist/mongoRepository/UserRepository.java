package com.ProyectoAula.GymAssist.mongoRepository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.ProyectoAula.GymAssist.mongoModels.UserEntity;
import java.util.Optional;
import org.bson.types.ObjectId;

public interface UserRepository extends MongoRepository<UserEntity, ObjectId> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<UserEntity> findById(Long id);
}
