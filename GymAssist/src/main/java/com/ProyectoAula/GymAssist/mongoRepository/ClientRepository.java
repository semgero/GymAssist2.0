package com.ProyectoAula.GymAssist.mongoRepository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import org.bson.types.ObjectId;

import java.util.Optional;
import java.util.List;

public interface ClientRepository extends MongoRepository<ClientEntity, ObjectId> {
    Optional<ClientEntity> findByCorreo(String correo);

    ClientEntity findUsernameById(Long id);

    List<ClientEntity> findByPlanIdIn(List<ObjectId> planIds);

    Optional<ClientEntity> findByUsername(String username);

    long countByPlanId(ObjectId planId);

    Optional<ClientEntity> findById(ObjectId id);

    List<ClientEntity> findByEstado(ClientEntity.EstadoCliente estado);

}