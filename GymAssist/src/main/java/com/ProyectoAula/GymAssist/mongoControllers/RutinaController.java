package com.ProyectoAula.GymAssist.mongoControllers;

import com.ProyectoAula.GymAssist.mongoModels.RutinaEntity;
import com.ProyectoAula.GymAssist.mongoServices.RutinaService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Controller
@RequestMapping("/rutinas")
public class RutinaController {

    @Autowired
    private RutinaService rutinaService;

    @GetMapping("/gym/{gymId}")
    public String mostrarRutinasPorGymId(@PathVariable String gymId, Model model) {
        List<RutinaEntity> rutinas = rutinaService.getRutinasByGymId(new ObjectId(gymId));
        model.addAttribute("rutinas", rutinas);
        model.addAttribute("gymId", gymId);
        return "rutinasGimnasio";
    }

    @GetMapping("/gym/{gymId}/nueva")
    public String mostrarFormularioNuevaRutina(@PathVariable String gymId, Model model) {
        model.addAttribute("rutina", new RutinaEntity());
        model.addAttribute("gymId", gymId);
        return "nuevaRutina";
    }

    @PostMapping("/gym/{gymId}/guardar")
public String guardarRutina(
        @PathVariable String gymId,
        @RequestParam String nombreEjercicio,
        @RequestParam String grupoMuscular,
        @RequestParam String repeticiones,
        @RequestParam String series,
        @RequestParam("fotosRutina") List<MultipartFile> archivos,
        @RequestParam("descripciones") List<String> descripciones) {

    RutinaEntity rutina = new RutinaEntity();
    rutina.setGrupoMuscular(grupoMuscular);
    rutina.setRepeticiones(repeticiones);
    rutina.setSeries(series);
    rutina.setGymId(new ObjectId(gymId));

    List<RutinaEntity.FotoRutina> fotos = new ArrayList<>();
    for (int i = 0; i < archivos.size(); i++) {
        MultipartFile archivo = archivos.get(i);
        if (!archivo.isEmpty()) {
            try {
                String imagenBase64 = Base64.getEncoder().encodeToString(archivo.getBytes());
                fotos.add(new RutinaEntity.FotoRutina(
                        nombreEjercicio,
                        archivo.getOriginalFilename(),
                        descripciones.get(i),
                        imagenBase64,
                        archivo.getContentType()
                ));
            } catch (IOException e) {
                throw new RuntimeException("Error al procesar la imagen: " + e.getMessage());
            }
        }
    }

    rutina.setFotosRutina(fotos);
    rutinaService.createRutina(rutina);
    return "redirect:/rutinas/gym/" + gymId;
}

    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditarRutina(@PathVariable String id, Model model) {
        RutinaEntity rutina = rutinaService.getRutinaById(new ObjectId(id))
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada con id: " + id));
        model.addAttribute("rutina", rutina);
        model.addAttribute("gymId", rutina.getGymId().toString());
        return "editarRutina";
    }

    @PostMapping("/{id}/actualizar")
public String actualizarRutina(
        @PathVariable String id,
        @RequestParam String nombreEjercicio,
        @RequestParam String grupoMuscular,
        @RequestParam String repeticiones,
        @RequestParam String series,
        @RequestParam("fotosRutina") List<MultipartFile> archivos,
        @RequestParam("descripciones") List<String> descripciones,
        @RequestParam(value = "fotosAEliminar", required = false) List<Integer> fotosAEliminar) {

    RutinaEntity rutina = rutinaService.getRutinaById(new ObjectId(id))
            .orElseThrow(() -> new RuntimeException("Rutina no encontrada con id: " + id));

    rutina.setGrupoMuscular(grupoMuscular);
    rutina.setRepeticiones(repeticiones);
    rutina.setSeries(series);

    // Eliminar fotos seleccionadas
    if (fotosAEliminar != null && !fotosAEliminar.isEmpty()) {
        List<RutinaEntity.FotoRutina> restantes = new ArrayList<>();
        for (int i = 0; i < rutina.getFotosRutina().size(); i++) {
            if (!fotosAEliminar.contains(i)) {
                restantes.add(rutina.getFotosRutina().get(i));
            }
        }
        rutina.setFotosRutina(restantes);
    }

    // Agregar nuevas fotos
    for (int i = 0; i < archivos.size(); i++) {
        MultipartFile archivo = archivos.get(i);
        if (!archivo.isEmpty()) {
            try {
                String imagenBase64 = Base64.getEncoder().encodeToString(archivo.getBytes());
                rutina.getFotosRutina().add(new RutinaEntity.FotoRutina(
                        nombreEjercicio,
                        archivo.getOriginalFilename(),
                        descripciones.get(i),
                        imagenBase64,
                        archivo.getContentType()
                ));
            } catch (IOException e) {
                throw new RuntimeException("Error al procesar la imagen: " + e.getMessage());
            }
        }
    }

    // Actualizar nombre del ejercicio para todas las fotos si se cambió
    for (RutinaEntity.FotoRutina foto : rutina.getFotosRutina()) {
        foto.setNombreEjercicio(nombreEjercicio);
    }

    rutinaService.updateRutina(new ObjectId(id), rutina);
    return "redirect:/rutinas/gym/" + rutina.getGymId();
}

    @PostMapping("/{id}/eliminar")
    public String eliminarRutina(@PathVariable String id, @RequestParam String gymId) {
        rutinaService.deleteRutinaById(new ObjectId(id));
        return "redirect:/rutinas/gym/" + gymId;
    }
}