package com.ProyectoAula.GymAssist.mongoServices;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    
    private final S3Client s3Client;
    private final String bucketName;

    // Nombres estándar de variables de entorno
    private static final String AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID";
    private static final String AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY";
    private static final String AWS_DEFAULT_REGION = "AWS_DEFAULT_REGION";
    private static final String AWS_BUCKET = "AWS_BUCKET";

    public S3Service(
            @Value("${aws.access-key-id:#{null}}") String accessKey,
            @Value("${aws.secret-access-key:#{null}}") String secretKey,
            @Value("${aws.region:#{null}}") String region,
            @Value("${aws.bucket:#{null}}") String bucket) {
        
        // 1. Intenta obtener valores de parámetros, luego de variables de entorno
        this.bucketName = bucket != null ? bucket : getEnvVariable(AWS_BUCKET, true);
        String resolvedAccessKey = accessKey != null ? accessKey : getEnvVariable(AWS_ACCESS_KEY_ID, true);
        String resolvedSecretKey = secretKey != null ? secretKey : getEnvVariable(AWS_SECRET_ACCESS_KEY, true);
        String resolvedRegion = region != null ? region : getEnvVariable(AWS_DEFAULT_REGION, false);
        
        // 2. Establece región por defecto si no se proporcionó
        if (resolvedRegion == null || resolvedRegion.isEmpty()) {
            resolvedRegion = "us-east-1";
            logger.warn("Usando región por defecto: {}", resolvedRegion);
        }

        // 3. Configura el cliente S3
        AwsBasicCredentials credentials = AwsBasicCredentials.create(resolvedAccessKey, resolvedSecretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(resolvedRegion))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        logger.info("S3Service configurado correctamente para el bucket: {}", this.bucketName);
    }

    private String getEnvVariable(String varName, boolean required) {
        String value = System.getenv(varName);
        if (required && (value == null || value.isEmpty())) {
            String errorMsg = String.format("Variable de entorno requerida '%s' no está configurada", varName);
            logger.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
        return value;
    }

    public String subirImagen(String nombreArchivo, Path archivo) {
        logger.debug("Subiendo archivo {} al bucket {}", nombreArchivo, bucketName);
        
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(nombreArchivo)
                .build(), archivo);

        return String.format("https://%s.s3.amazonaws.com/%s", bucketName, nombreArchivo);
    }

    public void eliminarImagen(String nombreArchivo) {
        logger.debug("Eliminando archivo {} del bucket {}", nombreArchivo, bucketName);
        
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(nombreArchivo)
                .build());
    }

    // Método para verificar conexión (útil para diagnóstico)
    public boolean verificarConexion() {
        try {
            s3Client.listBuckets();
            return true;
        } catch (Exception e) {
            logger.error("Error al verificar conexión con S3", e);
            return false;
        }
    }
}