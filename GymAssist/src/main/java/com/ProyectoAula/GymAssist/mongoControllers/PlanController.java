package com.ProyectoAula.GymAssist.mongoControllers;

import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;
import com.ProyectoAula.GymAssist.mongoServices.PlanService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/planes")
public class PlanController {

    @Autowired
    private PlanService planService;

    @PostMapping
    public ResponseEntity<PlanEntity> createPlan(@RequestBody PlanEntity plan) {
        return ResponseEntity.ok(planService.createPlan(plan));
    }

    @GetMapping("/gimnasio/{gimnasioId}")
    public ResponseEntity<List<PlanEntity>> getPlanesByGimnasioId(@PathVariable Long gimnasioId) {
        return ResponseEntity.ok(planService.getPlanesByGimnasioId(gimnasioId));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PlanEntity>> getPlanesByClienteId(@PathVariable Long clienteId) {
        return ResponseEntity.ok(planService.getPlanesByClienteId(clienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanEntity> getPlanById(@PathVariable String id) {
        return planService.getPlanById(new ObjectId(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanEntity> updatePlan(@PathVariable String id, @RequestBody PlanEntity plan) {
        return ResponseEntity.ok(planService.updatePlan(new ObjectId(id), plan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlanById(@PathVariable String id) {
        planService.deletePlanById(new ObjectId(id));
        return ResponseEntity.noContent().build();
    }
}
