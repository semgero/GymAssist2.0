package com.ProyectoAula.GymAssist.mongoServices;

import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;
import com.ProyectoAula.GymAssist.mongoRepository.ClientRepository;
import com.ProyectoAula.GymAssist.mongoRepository.PlanRepository;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Optional;

@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public PlanEntity createPlan(PlanEntity plan) {
        return planRepository.save(plan);
    }

    public List<PlanEntity> getPlanesByGimnasioId(ObjectId gymId) {
        return planRepository.findByGymId(gymId);
    }

    public Optional<PlanEntity> getPlanById(ObjectId id) {
        return planRepository.findById(id);
    }

    public PlanEntity updatePlan(ObjectId id, PlanEntity planDetails) {
        PlanEntity existingPlan = planRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado con id: " + id));

        existingPlan.setNombre(planDetails.getNombre());
        existingPlan.setDescripcion(planDetails.getDescripcion());
        existingPlan.setPrecio(planDetails.getPrecio());
        existingPlan.setDuracion(planDetails.getDuracion());
        existingPlan.setGymId(planDetails.getGymId());

        PlanEntity updatedPlan = planRepository.save(existingPlan);

        Query query = new Query(Criteria.where("planId").is(id));
        Update update = new Update().set("nombrePlan", planDetails.getNombre());
        var result = mongoTemplate.updateMulti(query, update, ClientEntity.class);

        System.out.println("🔎 Clientes actualizados con nuevo nombre de plan: " + result.getModifiedCount());

        return updatedPlan;
    }

    public void deletePlanById(ObjectId id) {
        planRepository.deleteById(id);

        Query query = new Query(Criteria.where("planId").is(id));
        Update update = new Update().unset("planId");
        var result = mongoTemplate.updateMulti(query, update, ClientEntity.class);

        System.out.println("❌ Clientes que quedaron sin plan: " + result.getModifiedCount());
    }

    public String obtenerNombreDelPlan(ObjectId planId) {
        return planRepository.findById(planId)
                .map(PlanEntity::getNombre)
                .orElse("Sin plan asignado");
    }

    public String obtenerPrecioDelPlan(ObjectId planId) {
        return planRepository.findById(planId)
                .map(plan -> String.format("%.2f", plan.getPrecio()))
                .orElse("0");
    }

    public void actualizarNombreEnClientes(ObjectId planId, String nuevoNombre) {
        Query query = new Query(Criteria.where("planId").is(planId));
        Update update = new Update().set("nombrePlan", nuevoNombre);
        mongoTemplate.updateMulti(query, update, ClientEntity.class);
    }

    public void eliminarPlanEnClientes(ObjectId planId) {
        Query query = new Query(Criteria.where("planId").is(planId));
        Update update = new Update().unset("planId");
        mongoTemplate.updateMulti(query, update, ClientEntity.class);
    }
}