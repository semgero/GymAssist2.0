package com.ProyectoAula.GymAssist.ml;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Enciende automáticamente la API de Python (FastAPI + TensorFlow) cuando arranca Spring Boot,
 * y la apaga cuando Spring Boot se detiene. Así no hay que abrir otra terminal con uvicorn.
 *
 * - Si la API ya está corriendo (por ejemplo, la abriste a mano), no hace nada.
 * - Busca la carpeta prediccion-ml y usa el Python de su entorno virtual (.venv).
 * - La salida de la API se guarda en prediccion-ml/api.log.
 * - Se desactiva con ml.api.autostart=false (variable ML_API_AUTOSTART=false), por ejemplo en Railway.
 */
@Component
public class ApiPythonLauncher {

    private static final Logger log = LoggerFactory.getLogger(ApiPythonLauncher.class);

    @Value("${ml.api.autostart:true}")
    private boolean autostart;

    @Value("${ml.api.dir:}")
    private String carpetaConfigurada;

    @Value("${ml.api.url:http://localhost:8000}")
    private String urlApi;

    private Process proceso;

    @PostConstruct
    public void iniciar() {
        if (!autostart) {
            log.info("Arranque automático de la API de Python desactivado (ml.api.autostart=false)");
            return;
        }
        // En otro hilo para no retrasar el arranque de Spring Boot
        Thread hilo = new Thread(this::arrancarApi, "arranque-api-python");
        hilo.setDaemon(true);
        hilo.start();
    }

    private void arrancarApi() {
        try {
            if (apiResponde()) {
                log.info("🧠 La API de predicción ya está corriendo en {}", urlApi);
                return;
            }
            Path carpeta = buscarCarpeta();
            if (carpeta == null) {
                log.warn("No se encontró la carpeta prediccion-ml. Enciende la API a mano o configura ml.api.dir");
                return;
            }
            String python = buscarPython(carpeta);
            int puerto = URI.create(urlApi).getPort();
            if (puerto <= 0) puerto = 8000;

            ProcessBuilder pb = new ProcessBuilder(python, "-m", "uvicorn", "app.main:app",
                    "--host", "127.0.0.1", "--port", String.valueOf(puerto));
            pb.directory(carpeta.toFile());
            pb.redirectErrorStream(true);
            pb.redirectOutput(carpeta.resolve("api.log").toFile());
            pb.environment().put("PYTHONUNBUFFERED", "1");
            pb.environment().put("TF_CPP_MIN_LOG_LEVEL", "2");

            log.info("🧠 Encendiendo la API de predicción: {} (carpeta {})", python, carpeta);
            proceso = pb.start();

            // Esperar a que responda (TensorFlow tarda unos segundos en cargar)
            for (int i = 0; i < 90; i++) {
                Thread.sleep(1000);
                if (!proceso.isAlive()) {
                    log.error("❌ La API de Python se cerró al arrancar. Revisa {}", carpeta.resolve("api.log"));
                    return;
                }
                if (apiResponde()) {
                    log.info("✅ API de predicción lista en {} ({} s)", urlApi, i + 1);
                    return;
                }
            }
            log.warn("La API de Python no respondió en 90 s. Revisa {}", carpeta.resolve("api.log"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("❌ No se pudo encender la API de Python: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void detener() {
        if (proceso != null && proceso.isAlive()) {
            log.info("Apagando la API de predicción...");
            proceso.descendants().forEach(ProcessHandle::destroy);
            proceso.destroy();
        }
    }

    private boolean apiResponde() {
        try {
            HttpURLConnection con = (HttpURLConnection) URI.create(urlApi + "/salud").toURL().openConnection();
            con.setConnectTimeout(1000);
            con.setReadTimeout(2000);
            int codigo = con.getResponseCode();
            con.disconnect();
            return codigo == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private Path buscarCarpeta() {
        List<Path> candidatas = new ArrayList<>();
        if (carpetaConfigurada != null && !carpetaConfigurada.isBlank()) {
            candidatas.add(Paths.get(carpetaConfigurada));
        }
        Path actual = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
        candidatas.add(actual.resolve("prediccion-ml"));          // si se ejecuta desde la raíz del repo
        candidatas.add(actual.resolveSibling("prediccion-ml"));   // si se ejecuta desde GymAssist/
        for (Path c : candidatas) {
            if (Files.isRegularFile(c.resolve("app").resolve("main.py"))) {
                return c.normalize();
            }
        }
        return null;
    }

    private String buscarPython(Path carpeta) {
        Path windows = carpeta.resolve(".venv").resolve("Scripts").resolve("python.exe");
        Path linux = carpeta.resolve(".venv").resolve("bin").resolve("python");
        if (Files.isRegularFile(windows)) return windows.toString();
        if (Files.isRegularFile(linux)) return linux.toString();
        log.warn("No se encontró prediccion-ml/.venv; se usará el Python del sistema");
        return File.separatorChar == '\\' ? "python" : "python3";
    }
}
