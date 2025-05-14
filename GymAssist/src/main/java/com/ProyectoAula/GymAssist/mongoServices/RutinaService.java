package com.ProyectoAula.GymAssist.mongoServices;

import com.ProyectoAula.GymAssist.mongoModels.RutinaEntity;
import com.ProyectoAula.GymAssist.mongoRepository.RutinaRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RutinaService {

    @Autowired
    private RutinaRepository rutinaRepository;

    @Autowired
    private S3Service s3Service;

    public RutinaEntity createRutina(RutinaEntity rutina, List<MultipartFile> archivos, List<String> descripciones) {
        List<RutinaEntity.FotoRutina> fotos = new ArrayList<>();

        for (int i = 0; i < archivos.size(); i++) {
            MultipartFile archivo = archivos.get(i);
            if (!archivo.isEmpty()) {
                try {
                    String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
                    Path rutaTemp = Path.of(System.getProperty("java.io.tmpdir"), nombreArchivo);
                    archivo.transferTo(rutaTemp.toFile());

                    String urlImagen = s3Service.subirImagen(nombreArchivo, rutaTemp);

                    fotos.add(new RutinaEntity.FotoRutina(
                            rutina.getGrupoMuscular(),
                            nombreArchivo,
                            descripciones.get(i),
                            urlImagen, 
                            archivo.getContentType()));
                } catch (IOException e) {
                    throw new RuntimeException("Error al subir la imagen a S3: " + e.getMessage());
                }
            }
        }

        rutina.setFotosRutina(fotos);
        return rutinaRepository.save(rutina);
    }


    public List<RutinaEntity> getRutinasByGymId(ObjectId gymId) {
        return rutinaRepository.findByGymId(gymId);
    }

    public Optional<RutinaEntity> getRutinaById(ObjectId id) {
        return rutinaRepository.findById(id);
    }

    public RutinaEntity updateRutina(ObjectId id, RutinaEntity rutinaEntity) {
        RutinaEntity existingRutina = rutinaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada con id: " + id));

        if (rutinaEntity.getGrupoMuscular() != null) {
            existingRutina.setGrupoMuscular(rutinaEntity.getGrupoMuscular());
        }
        if (rutinaEntity.getRepeticiones() != null) {
            existingRutina.setRepeticiones(rutinaEntity.getRepeticiones());
        }
        if (rutinaEntity.getSeries() != null) {
            existingRutina.setSeries(rutinaEntity.getSeries());
        }
        if (rutinaEntity.getGymId() != null) {
            existingRutina.setGymId(rutinaEntity.getGymId());
        }

        if (rutinaEntity.getFotosRutina() != null) {
            existingRutina.setFotosRutina(rutinaEntity.getFotosRutina());
        }

        existingRutina.setUpdatedAt(LocalDateTime.now());
        return rutinaRepository.save(existingRutina);
    }

    public void deleteRutinaById(ObjectId id) {
        RutinaEntity rutina = rutinaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede eliminar: Rutina no encontrada con id: " + id));
        rutinaRepository.deleteById(id);
    }

    public List<RutinaEntity> findByNombreEjercicio(String nombreEjercicio) {
        return rutinaRepository.findByFotosRutina_NombreEjercicioIgnoreCase(nombreEjercicio);
    }
}