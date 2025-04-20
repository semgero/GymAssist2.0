package com.ProyectoAula.GymAssist.controller;

import com.ProyectoAula.GymAssist.models.PlanEntity;
import com.ProyectoAula.GymAssist.services.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/planes")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @PostMapping
    public PlanEntity crear(@RequestBody PlanEntity plan) {
        return planService.crearPlan(plan);
    }

    @GetMapping
    public List<PlanEntity> obtenerTodos() {
        return planService.obtenerPlanes();
    }

    @GetMapping("/{id}")
    public PlanEntity obtenerPorId(@PathVariable Long id) {
        return planService.obtenerPlanPorId(id).orElse(null);
    }

    @PutMapping("/{id}")
    public PlanEntity editar(@PathVariable Long id, @RequestBody PlanEntity actualizado) {
        return planService.editarPlan(id, actualizado);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        planService.eliminarPlan(id);
    }
}
