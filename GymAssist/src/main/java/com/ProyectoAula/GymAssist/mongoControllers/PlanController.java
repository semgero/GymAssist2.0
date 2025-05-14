package com.ProyectoAula.GymAssist.mongoControllers;

import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;
import com.ProyectoAula.GymAssist.mongoServices.PlanService;
import com.ProyectoAula.GymAssist.mongoRepository.ClientRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/planes")
public class PlanController {

    @Autowired
    private PlanService planService;

    @Autowired
    private ClientRepository clientRepository;

    // Mostrar todos los planes de un gimnasio en una vista HTML
    @GetMapping("/gimnasio/{gymId}")
    public String mostrarPlanesPorGimnasio(@PathVariable String gymId, Model model) {
        List<PlanEntity> planes = planService.getPlanesByGimnasioId(new ObjectId(gymId));
        Map<ObjectId, Long> suscriptoresPorPlan = new HashMap<>();
        for (PlanEntity plan : planes) {
            long count = clientRepository.countByPlanId(plan.getId());
            suscriptoresPorPlan.put(plan.getId(), count);
        }
        model.addAttribute("planes", planes);
        model.addAttribute("suscriptoresPorPlan", suscriptoresPorPlan);
        model.addAttribute("gymId", gymId);
        return "planesGimnasio";
    }

    // Mostrar formulario para crear un nuevo plan para un gimnasio
    @GetMapping("/gimnasio/{gymId}/nuevo")
    public String mostrarFormularioNuevoPlan(@PathVariable String gymId, Model model) {
        model.addAttribute("plan", new PlanEntity());
        model.addAttribute("gymId", gymId);
        return "nuevoPlan";
    }

    // Guardar un nuevo plan para un gimnasio
    @PostMapping("/gimnasio/{gymId}/guardar")
    public String guardarNuevoPlan(
            @PathVariable String gymId,
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam Double precio,
            @RequestParam Integer duracion, // Número de meses
            Model model) {
        PlanEntity plan = new PlanEntity();
        plan.setNombre(nombre);
        plan.setDescripcion(descripcion);
        plan.setPrecio(precio);
        plan.setDuracion(duracion);
        plan.setGymId(new ObjectId(gymId));
        planService.createPlan(plan);
        return "redirect:/planes/gimnasio/" + gymId;
    }

    // Mostrar formulario para editar un plan
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditarPlan(@PathVariable String id, Model model) {
        PlanEntity plan = planService.getPlanById(new ObjectId(id)).orElse(null);
        model.addAttribute("plan", plan);
        return "editarPlan";
    }

    // Actualizar un plan
    @PostMapping("/{id}/actualizar")
    public String actualizarPlan(
            @PathVariable String id,
            @RequestParam String nombre,
            @RequestParam String descripcion,
            @RequestParam Double precio,
            @RequestParam Integer duracion,
            @RequestParam String gymId) {
        PlanEntity plan = new PlanEntity();
        plan.setNombre(nombre);
        plan.setDescripcion(descripcion);
        plan.setPrecio(precio);
        plan.setDuracion(duracion);
        plan.setGymId(new ObjectId(gymId));
        planService.updatePlan(new ObjectId(id), plan);
        return "redirect:/planes/gimnasio/" + gymId;
    }

    // Eliminar un plan
    @PostMapping("/{id}/eliminar")
    public String eliminarPlan(@PathVariable String id, @RequestParam String gymId) {
        planService.deletePlanById(new ObjectId(id));
        return "redirect:/planes/gimnasio/" + gymId;
    }
}