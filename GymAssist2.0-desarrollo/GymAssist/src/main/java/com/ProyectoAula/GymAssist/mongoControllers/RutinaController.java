package com.ProyectoAula.GymAssist.mongoControllers;

import com.ProyectoAula.GymAssist.mongoModels.RutinaEntity;
import com.ProyectoAula.GymAssist.mongoServices.RutinaService;
import com.ProyectoAula.GymAssist.mongoServices.S3Service;

import jakarta.servlet.http.HttpSession;

import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/rutinas")
public class RutinaController {

    private final RutinaService rutinaService;
    private final S3Service s3Service;

    public RutinaController(RutinaService rutinaService, S3Service s3Service) {
        this.rutinaService = rutinaService;
        this.s3Service = s3Service;
    }

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

    @GetMapping("/ClienteRutinas/grupo/{grupoMuscular}")
    public String mostrarRutinasPorGrupo(@PathVariable String grupoMuscular, Model model, HttpSession session) {
        System.out.println("🟢 Grupo muscular recibido: " + grupoMuscular); // Verifica en consola

        ObjectId gymId = (ObjectId) session.getAttribute("gymId");
        if (gymId == null) {
            return "redirect:/Api/Auth/login";
        }

        List<RutinaEntity> rutinas = rutinaService.findByGrupoMuscularAndGymId(grupoMuscular.toUpperCase(), gymId);
        System.out.println("🔎 Rutinas encontradas: " + rutinas.size());
        model.addAttribute("rutinas", rutinas);
        model.addAttribute("grupoMuscular", grupoMuscular);
        return "rutinasGrupo";
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
        rutina.setNombreEjercicio(nombreEjercicio); // ✅ Se establece correctamente
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
            @RequestParam String nombreEjercicio, // Ahora lista
            @RequestParam String grupoMuscular,
            @RequestParam String repeticiones,
            @RequestParam String series,
            @RequestParam("fotosRutina") List<MultipartFile> archivos,
            @RequestParam("descripciones") List<String> descripciones,
            @RequestParam(value = "fotosAEliminar", required = false) List<Integer> fotosAEliminar) {

        RutinaEntity rutina = rutinaService.getRutinaById(new ObjectId(id))
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada con id: " + id));

        rutina.setNombreEjercicio(nombreEjercicio);
        rutina.setGrupoMuscular(grupoMuscular);
        rutina.setRepeticiones(repeticiones);
        rutina.setSeries(series);
        rutina.setUpdatedAt(LocalDateTime.now());

        // Eliminar fotos seleccionadas
        if (fotosAEliminar != null && !fotosAEliminar.isEmpty()) {
            List<RutinaEntity.FotoRutina> fotosActuales = rutina.getFotosRutina();
            List<RutinaEntity.FotoRutina> fotosActualizadas = new ArrayList<>();

            for (int i = 0; i < fotosActuales.size(); i++) {
                if (!fotosAEliminar.contains(i)) {
                    fotosActualizadas.add(fotosActuales.get(i));
                } else {
                    try {
                        s3Service.eliminarImagen(fotosActuales.get(i).getNombreArchivo());
                    } catch (Exception e) {
                        System.err.println("Error al eliminar imagen de S3: " + e.getMessage());
                    }
                }
            }
            rutina.setFotosRutina(fotosActualizadas);
        }

        // Agregar nuevas fotos con los nombres correctos
        for (int i = 0; i < archivos.size(); i++) {
            MultipartFile archivo = archivos.get(i);
            if (!archivo.isEmpty()) {
                try {
                    String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
                    Path rutaTemp = Path.of(System.getProperty("java.io.tmpdir"), nombreArchivo);
                    archivo.transferTo(rutaTemp.toFile());

                    String urlImagen = s3Service.subirImagen(nombreArchivo, rutaTemp);

                    rutina.getFotosRutina().add(new RutinaEntity.FotoRutina(
                            nombreArchivo,
                            descripciones.get(i),
                            urlImagen,
                            archivo.getContentType()));
                } catch (IOException e) {
                    throw new RuntimeException("Error al subir la imagen a S3: " + e.getMessage());
                }
            }
        }

        rutinaService.updateRutina(new ObjectId(id), rutina);
        return "redirect:/rutinas/gym/" + rutina.getGymId();
    }

    @GetMapping("/cliente/ver/{gymId}")
    public String verRutinasCliente(
            @PathVariable String gymId,
            Model model,
            RedirectAttributes redirectAttributes) {

        // 1. Verificar si el parámetro contiene {gymId} literal
        if (gymId.startsWith("{") && gymId.endsWith("}")) {
            redirectAttributes.addFlashAttribute("error", "Debe proporcionar un ID de gimnasio válido");
            return "redirect:/error-page";
        }

        // 2. Validación básica
        if (gymId == null || gymId.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "El ID del gimnasio es requerido");
            return "redirect:/error-page";
        }

        try {
            // 3. Conversión a ObjectId
            ObjectId gymObjectId = new ObjectId(gymId);

            // 4. Obtención de rutinas
            List<RutinaEntity> rutinas = rutinaService.getRutinasByGymId(gymObjectId);

            model.addAttribute("rutinas", rutinas);
            return "rutinasCliente";

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "ID de gimnasio no válido: " + gymId);
            return "redirect:/error-page";
        }
    }

    @GetMapping("/{grupoMuscular}")
    public ResponseEntity<List<RutinaEntity>> obtenerRutinas(@PathVariable String grupoMuscular) {
        List<RutinaEntity> rutinas = rutinaService.getRutinasPorGrupoMuscular(grupoMuscular);
        return ResponseEntity.ok(rutinas);
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarRutina(@PathVariable String id, @RequestParam String gymId) {
        rutinaService.deleteRutinaById(new ObjectId(id));
        return "redirect:/rutinas/gym/" + gymId;
    }
}