package com.ProyectoAula.GymAssist.mongoRepository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.ProyectoAula.GymAssist.mongoModels.UserEntity;
import java.util.Optional;

public interface UserRepository extends MongoRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
