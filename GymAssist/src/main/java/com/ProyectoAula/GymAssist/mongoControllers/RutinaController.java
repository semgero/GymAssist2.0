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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/rutinas")
public class RutinaController {

    @Autowired
    private RutinaService rutinaService;

    private static final String RUTA_IMAGENES = "uploads/";

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
public String guardarRutina(@PathVariable String gymId,
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
    rutina.setCreatedAt(LocalDateTime.now());
    rutina.setUpdatedAt(LocalDateTime.now());

    rutinaService.createRutina(rutina, archivos, descripciones);
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
        rutina.setUpdatedAt(LocalDateTime.now());

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
                    // Generar nombre único para el archivo
                    String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
                    Path rutaGuardado = Paths.get(RUTA_IMAGENES + nombreArchivo);

                    // Crear directorios si no existen
                    Files.createDirectories(rutaGuardado.getParent());

                    // Guardar archivo en el sistema
                    Files.copy(archivo.getInputStream(), rutaGuardado, StandardCopyOption.REPLACE_EXISTING);

                    // Agregar nueva foto a la rutina
                    rutina.getFotosRutina().add(new RutinaEntity.FotoRutina(
                            nombreEjercicio,
                            nombreArchivo,
                            descripciones.get(i),
                            "/content/rutinas/" + nombreArchivo,
                            archivo.getContentType()));
                } catch (IOException e) {
                    throw new RuntimeException("Error al guardar la imagen: " + e.getMessage());
                }
            }
        }

        // Actualizar nombre del ejercicio para todas las fotos
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