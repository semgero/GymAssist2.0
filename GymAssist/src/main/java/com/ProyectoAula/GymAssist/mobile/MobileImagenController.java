package com.ProyectoAula.GymAssist.mobile;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ProyectoAula.GymAssist.mongoServices.S3Service;

/**
 * Sirve las fotos de las rutinas desde S3 a través del backend:
 *   GET /api/m/imagenes?key=<nombreArchivo>
 * Así Flutter (también en Chrome) las muestra sin configurar CORS en el bucket ni hacerlo público.
 */
@RestController
@RequestMapping("/api/m/imagenes")
public class MobileImagenController {

    private static final Logger log = LoggerFactory.getLogger(MobileImagenController.class);
    private final S3Service s3;

    public MobileImagenController(S3Service s3) {
        this.s3 = s3;
    }

    @GetMapping
    public ResponseEntity<byte[]> imagen(@RequestParam String key) {
        if (key.isBlank() || key.contains("..") || key.startsWith("/")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            var obj = s3.descargarImagen(key);
            String tipo = obj.response().contentType();
            MediaType mt;
            try {
                mt = (tipo == null || tipo.contains("octet-stream")) ? porExtension(key) : MediaType.parseMediaType(tipo);
            } catch (Exception e) {
                mt = porExtension(key);
            }
            return ResponseEntity.ok()
                    .contentType(mt)
                    .cacheControl(CacheControl.maxAge(Duration.ofDays(7)).cachePublic())
                    .body(obj.asByteArray());
        } catch (Exception e) {
            log.warn("No se pudo leer la imagen {} de S3: {}", key, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    private static MediaType porExtension(String key) {
        String k = key.toLowerCase();
        if (k.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (k.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (k.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        return MediaType.IMAGE_JPEG;
    }
}
