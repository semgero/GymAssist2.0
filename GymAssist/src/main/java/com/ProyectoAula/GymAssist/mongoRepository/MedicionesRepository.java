package com.ProyectoAula.GymAssist.mongoRepository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.ProyectoAula.GymAssist.mongoModels.MedicionesEntity;

public interface MedicionesRepository extends MongoRepository<MedicionesEntity, ObjectId> {
    Optional<MedicionesEntity> findTopByClienteIdOrderByFechaRegistroDesc(ObjectId clienteId);
}