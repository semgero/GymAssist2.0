package com.ProyectoAula.GymAssist.repositories;

import com.ProyectoAula.GymAssist.mongoModels.Medicion;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MedicionRepository extends MongoRepository <Medicion, String>{
    List<Medicion> findByClienteId(Long clienteId);
}
