package com.ProyectoAula.GymAssist.mongoServices;

import com.ProyectoAula.GymAssist.mongoModels.RutinaEntity;
import com.ProyectoAula.GymAssist.mongoRepository.RutinaRepository;
import com.ProyectoAula.GymAssist.utils.GrupoMuscularConstants;
import com.ProyectoAula.GymAssist.utils.LevenshteinUtils;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
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

    private static final Logger logger = LoggerFactory.getLogger(RutinaService.class);

    // Al crear una rutina se limpian las listas en caché para que aparezca de inmediato
    @Caching(evict = {
            @CacheEvict(value = "rutinasPorGymId", allEntries = true),
            @CacheEvict(value = "rutinasPorGrupo", allEntries = true),
            @CacheEvict(value = "rutinasPorGrupoYGym", allEntries = true)
    })
    public RutinaEntity createRutina(RutinaEntity rutina, List<MultipartFile> archivos, List<String> descripciones) {
        logger.info("📥 Creando nueva rutina para gymId: {}", rutina.getGymId());
        List<RutinaEntity.FotoRutina> fotos = new ArrayList<>();

        for (int i = 0; i < archivos.size(); i++) {
            MultipartFile archivo = archivos.get(i);
            if (!archivo.isEmpty()) {
                try {
                    String nombreArchivo = System.currentTimeMillis() + "_" + archivo.getOriginalFilename();
                    Path rutaTemp = Path.of(System.getProperty("java.io.tmpdir"), nombreArchivo);
                    archivo.transferTo(rutaTemp.toFile());

                    String urlImagen = s3Service.subirImagen(nombreArchivo, rutaTemp);
                    logger.debug("✅ Imagen subida correctamente: {}", urlImagen);

                    fotos.add(new RutinaEntity.FotoRutina(
                            nombreArchivo,
                            (i < descripciones.size()) ? descripciones.get(i) : "Sin descripción",
                            urlImagen,
                            archivo.getContentType()));
                } catch (IOException e) {
                    logger.error("❌ Error al subir la imagen a S3: {}", e.getMessage());
                    throw new RuntimeException("Error al subir la imagen a S3: " + e.getMessage());
                }
            }
        }

        rutina.setFotosRutina(fotos);
        RutinaEntity saved = rutinaRepository.save(rutina);
        logger.info("✅ Rutina guardada con ID: {}", saved.getId());
        return saved;
    }

    // Caché Redis (30 min). Si este log no aparece, la respuesta salió de Redis.
    @Cacheable(value = "rutinasPorGymId", key = "#gymId.toHexString()", condition = "#gymId != null")
    public List<RutinaEntity> getRutinasByGymId(ObjectId gymId) {
        logger.info("📦 Buscando rutinas por gymId: {} en MongoDB (se guardará en Redis)", gymId);
        return rutinaRepository.findByGymId(gymId);
    }

    @Cacheable(value = "rutinaPorId", key = "#id.toHexString()", condition = "#id != null", unless = "#result == null")
    public Optional<RutinaEntity> getRutinaById(ObjectId id) {
        logger.info("📦 Buscando rutina por ID: {} en MongoDB (se guardará en Redis)", id);
        return rutinaRepository.findById(id);
    }

    @Cacheable(value = "rutinasPorGrupo", key = "#grupoMuscular", condition = "#grupoMuscular != null")
    public List<RutinaEntity> getRutinasPorGrupoMuscular(String grupoMuscular) {
        logger.info("📦 Buscando rutinas por grupo muscular: {} en MongoDB (se guardará en Redis)", grupoMuscular);
        return rutinaRepository.findByGrupoMuscular(grupoMuscular);
    }

    public String sugerirGrupoMuscular(String entradaUsuario) {
        String mejorCoincidencia = null;
        int menorDistancia = Integer.MAX_VALUE;

        for (String grupo : GrupoMuscularConstants.GRUPOS_MUSCULARES_VALIDOS) {
            int distancia = LevenshteinUtils.calcularDistanciaLevenshtein(entradaUsuario.toLowerCase(),
                    grupo.toLowerCase());
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                mejorCoincidencia = grupo;
            }
        }

        logger.debug("🔎 Sugerencia para '{}': {} (Distancia: {})", entradaUsuario, mejorCoincidencia, menorDistancia);
        return (menorDistancia <= 2) ? mejorCoincidencia : null;
    }

    //@Cacheable(value = "rutinasPorGrupo", key = "#grupoUsuario")
    public List<RutinaEntity> buscarRutinasPorGrupoMuscular(String grupoUsuario) {
        logger.info("🔍 Buscando rutinas con entrada de usuario: {}", grupoUsuario);
        String grupoCorregido = sugerirGrupoMuscular(grupoUsuario);

        if (grupoCorregido != null) {
            logger.info("✅ Grupo corregido: {}", grupoCorregido);
            List<RutinaEntity> rutinas = rutinaRepository.findByGrupoMuscular(grupoCorregido);
            logger.debug("📊 Rutinas encontradas: {}", rutinas.size());
            return rutinas;
        } else {
            logger.warn("⚠️ No se encontró un grupo muscular válido para: {}", grupoUsuario);
            return List.of();
        }
    }

    @Cacheable(value = "rutinasPorGrupoYGym", key = "#grupoMuscular.toLowerCase() + '_' + #gymId.toHexString()", condition = "#grupoMuscular != null && #gymId != null")
    public List<RutinaEntity> findByGrupoMuscularAndGymId(String grupoMuscular, ObjectId gymId) {
        logger.info("📦 Buscando rutinas por grupo: {} y gymId: {} en MongoDB (se guardará en Redis)", grupoMuscular, gymId);
        return rutinaRepository.findByGrupoMuscularIgnoreCaseAndGymId(grupoMuscular, gymId);
    }

    @Caching(evict = {
            @CacheEvict(value = "rutinaPorId", key = "#id.toHexString()"),
            @CacheEvict(value = "rutinasPorGymId", allEntries = true),
            @CacheEvict(value = "rutinasPorGrupo", allEntries = true),
            @CacheEvict(value = "rutinasPorGrupoYGym", allEntries = true)
    })
    public RutinaEntity updateRutina(ObjectId id, RutinaEntity rutinaEntity) {
        logger.info("✏️ Actualizando rutina con ID: {}", id);
        RutinaEntity existingRutina = rutinaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rutina no encontrada con id: " + id));

        if (rutinaEntity.getNombreEjercicio() != null)
            existingRutina.setNombreEjercicio(rutinaEntity.getNombreEjercicio());
        if (rutinaEntity.getGrupoMuscular() != null)
            existingRutina.setGrupoMuscular(rutinaEntity.getGrupoMuscular());
        if (rutinaEntity.getRepeticiones() != null)
            existingRutina.setRepeticiones(rutinaEntity.getRepeticiones());
        if (rutinaEntity.getSeries() != null)
            existingRutina.setSeries(rutinaEntity.getSeries());
        if (rutinaEntity.getGymId() != null)
            existingRutina.setGymId(rutinaEntity.getGymId());
        if (rutinaEntity.getFotosRutina() != null)
            existingRutina.setFotosRutina(rutinaEntity.getFotosRutina());

        existingRutina.setUpdatedAt(LocalDateTime.now());
        RutinaEntity updated = rutinaRepository.save(existingRutina);
        logger.info("✅ Rutina actualizada: {}", updated.getId());
        return updated;
    }

    @Caching(evict = {
            @CacheEvict(value = "rutinaPorId", key = "#id.toHexString()"),
            @CacheEvict(value = "rutinasPorGymId", allEntries = true),
            @CacheEvict(value = "rutinasPorGrupo", allEntries = true),
            @CacheEvict(value = "rutinasPorGrupoYGym", allEntries = true)
    })
    public void deleteRutinaById(ObjectId id) {
        logger.warn("🗑️ Eliminando rutina con ID: {}", id);
        RutinaEntity rutina = rutinaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se puede eliminar: Rutina no encontrada con id: " + id));
        rutinaRepository.deleteById(id);
        logger.info("✅ Rutina eliminada: {}", id);
    }
}
