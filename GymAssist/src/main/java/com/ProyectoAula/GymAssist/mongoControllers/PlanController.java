package com.ProyectoAula.GymAssist.mongoControllers;

import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;
import com.ProyectoAula.GymAssist.mongoServices.PlanService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/planes")
public class PlanController {

    @Autowired
    private PlanService planService;

    // Mostrar todos los planes de un gimnasio en una vista HTML
    @GetMapping("/gimnasio/{gymId}")
    public String mostrarPlanesPorGimnasio(@PathVariable String gymId, Model model) {
        List<PlanEntity> planes = planService.getPlanesByGimnasioId(new ObjectId(gymId));
        model.addAttribute("planes", planes);
        model.addAttribute("gymId", gymId);
        return "planesGimnasio"; // Nombre de tu plantilla HTML
    }

    // Mostrar formulario para crear un nuevo plan para un gimnasio
    @GetMapping("/gimnasio/{gymId}/nuevo")
    public String mostrarFormularioNuevoPlan(@PathVariable Long gymId, Model model) {
        model.addAttribute("plan", new PlanEntity());
        model.addAttribute("gymId", gymId);
        return "nuevoPlan";
    }

    // Guardar un nuevo plan para un gimnasio
    @PostMapping("/gimnasio/{gymId}/guardar")
    public String guardarNuevoPlan(@PathVariable String gymId, @ModelAttribute PlanEntity plan) {
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
    public String actualizarPlan(@PathVariable String id, @ModelAttribute PlanEntity plan) {
        planService.updatePlan(new ObjectId(id), plan);
        // Redirigir a la lista de planes del gimnasio correspondiente
        return "redirect:/planes/gimnasio/" + plan.getGymId().toHexString();
    }

    // Eliminar un plan
    @PostMapping("/{id}/eliminar")
    public String eliminarPlan(@PathVariable String id, @RequestParam Long gymId) {
        planService.deletePlanById(new ObjectId(id));
        return "redirect:/planes/gimnasio/" + gymId;
    }
}