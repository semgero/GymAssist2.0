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
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/rutinas")
public class RutinaController {

    @Autowired
    private RutinaService rutinaService;

    // Mostrar todas las rutinas de un gimnasio en una vista HTML
    @GetMapping("/gym/{gymId}")
    public String mostrarRutinasPorGymId(@PathVariable String gymId, Model model) {
        List<RutinaEntity> rutinas = rutinaService.getRutinasByGymId(new ObjectId(gymId));
        model.addAttribute("rutinas", rutinas);
        model.addAttribute("gymId", gymId);
        return "rutinasGimnasio"; // Nombre de la vista HTML
    }

    // Mostrar formulario para crear una nueva rutina
    @GetMapping("/gym/{gymId}/nueva")
    public String mostrarFormularioNuevaRutina(@PathVariable String gymId, Model model) {
        model.addAttribute("rutina", new RutinaEntity());
        model.addAttribute("gymId", gymId);
        return "nuevaRutina"; // Nombre de la vista HTML
    }

    // Guardar una nueva rutina
    @PostMapping("/gym/{gymId}/guardar")
public String guardarRutina(@PathVariable String gymId,
                            @ModelAttribute RutinaEntity rutina,
                            @RequestParam("fotosRutina") List<MultipartFile> archivos) {
    List<RutinaEntity.FotoRutina> fotos = new ArrayList<>();

    for (int i = 0; i < archivos.size(); i++) {
        MultipartFile archivo = archivos.get(i);
        if (!archivo.isEmpty()) {
            try {
                // Guarda el archivo en el servidor
                String nombreArchivo = archivo.getOriginalFilename();
                Path rutaArchivo = Paths.get("uploads/" + nombreArchivo);
                Files.copy(archivo.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

                // Agrega la foto a la lista
                String descripcion = rutina.getFotosRutina().get(i).getDescripcion();
                fotos.add(new RutinaEntity.FotoRutina(nombreArchivo, descripcion));
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error al guardar el archivo: " + archivo.getOriginalFilename());
            }
        }
    }

    rutina.setFotosRutina(fotos);
    rutina.setGymId(new ObjectId(gymId));
    rutinaService.createRutina(rutina);
    return "redirect:/rutinas/gym/" + gymId;
}

    // Mostrar formulario para editar una rutina
    @GetMapping("/{id}/editar")
    public String mostrarFormularioEditarRutina(@PathVariable String id, Model model) {
        RutinaEntity rutina = rutinaService.getRutinaById(new ObjectId(id))
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada con id: " + id));
        model.addAttribute("rutina", rutina);
        model.addAttribute("gymId", rutina.getGymId().toString());
        return "editarRutina"; // Nombre de la vista HTML
    }

    // Actualizar una rutina
    @PostMapping("/{id}/actualizar")
    public String actualizarRutina(@PathVariable String id, @ModelAttribute RutinaEntity rutina) {
        rutinaService.updateRutina(new ObjectId(id), rutina);
        return "redirect:/rutinas/gym/" + rutina.getGymId();
    }

    // Eliminar una rutina
    @PostMapping("/{id}/eliminar")
    public String eliminarRutina(@PathVariable String id, @RequestParam String gymId) {
        rutinaService.deleteRutinaById(new ObjectId(id));
        return "redirect:/rutinas/gym/" + gymId;
    }
}