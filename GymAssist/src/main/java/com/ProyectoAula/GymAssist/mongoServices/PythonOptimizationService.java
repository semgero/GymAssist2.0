package com.ProyectoAula.GymAssist.mongoServices;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Service;

import com.ProyectoAula.GymAssist.models.ModeloRequest;
import com.ProyectoAula.GymAssist.models.ModeloResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PythonOptimizationService {

   private final ObjectMapper mapper = new ObjectMapper();

    public ModeloResponse resolver(ModeloRequest request) {

        try {

            // CONVERTIR REQUEST A JSON
            String json = mapper.writeValueAsString(request);

            System.out.println("JSON ENVIADO:");
            System.out.println(json);

            // RUTA DEL SCRIPT
            String scriptPath = new File(
                    "GymAssist/src/main/resources/python/modelo_optimizacion.py"
            ).getAbsolutePath();

            System.out.println("SCRIPT PATH: " + scriptPath);

            // CREAR PROCESO PYTHON
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python",
                    scriptPath
            );

            // IMPORTANTE:
            // NO mezclar stderr con stdout
            processBuilder.redirectErrorStream(false);

            Process process = processBuilder.start();

            // ENVIAR JSON A PYTHON
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                            process.getOutputStream(),
                            StandardCharsets.UTF_8
                    ))) {

                writer.write(json);
                writer.flush();
            }

            // LEER STDOUT (resultado JSON)
            BufferedReader outputReader = new BufferedReader(
                    new InputStreamReader(
                            process.getInputStream(),
                            StandardCharsets.UTF_8
                    )
            );

            StringBuilder outputBuilder = new StringBuilder();

            String line;

            while ((line = outputReader.readLine()) != null) {
                outputBuilder.append(line);
            }

            // LEER STDERR (errores/debug)
            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(
                            process.getErrorStream(),
                            StandardCharsets.UTF_8
                    )
            );

            StringBuilder errorBuilder = new StringBuilder();

            while ((line = errorReader.readLine()) != null) {
                errorBuilder.append(line).append("\n");
            }

            int exitCode = process.waitFor();

            String output = outputBuilder.toString();
            String errors = errorBuilder.toString();

            System.out.println("PYTHON STDOUT:");
            System.out.println(output);

            if (!errors.isEmpty()) {
                System.out.println("PYTHON STDERR:");
                System.out.println(errors);
            }

            // SI PYTHON FALLÓ
            if (exitCode != 0) {

                throw new RuntimeException(
                        "Python devolvió un error:\n" + errors
                );
            }

            // VALIDAR RESPUESTA
            if (output.isBlank()) {

                throw new RuntimeException(
                        "Python no devolvió resultados"
                );
            }

            // CONVERTIR JSON A OBJETO JAVA
            return mapper.readValue(output, ModeloResponse.class);

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Error ejecutando Pyomo",
                    e
            );
        }
    }
}