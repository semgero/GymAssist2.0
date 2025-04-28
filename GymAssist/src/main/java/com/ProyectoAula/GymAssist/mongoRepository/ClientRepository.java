package com.ProyectoAula.GymAssist.mongoRepository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;

import java.util.Optional;
import java.util.List;

public interface ClientRepository extends MongoRepository<ClientEntity, String> {
    Optional<ClientEntity> findByCorreo(String correo);
    ClientEntity findByUsername(String username);
    List<ClientEntity> findByAdminId(Long adminId);
}