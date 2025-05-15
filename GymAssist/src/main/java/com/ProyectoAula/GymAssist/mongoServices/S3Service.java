package com.ProyectoAula.GymAssist.mongoServices;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.nio.file.Path;
import org.springframework.stereotype.Service;

@Service
public class S3Service {

    private final S3Client s3Client;

    private static final String AWS_ACCESS_KEY_ENV = "AWS_ACCESS_KEY";
    private static final String AWS_SECRET_KEY_ENV = "AWS_SECRET_KEY";
    private static final String AWS_REGION_ENV = "AWS_REGION";
    private static final String AWS_BUCKET_ENV = "AWS_BUCKET";

    public S3Service() {
        String accessKey = System.getenv(AWS_ACCESS_KEY_ENV);
        String secretKey = System.getenv(AWS_SECRET_KEY_ENV);
        String region = System.getenv(AWS_REGION_ENV);
        String bucket = System.getenv(AWS_BUCKET_ENV);

        if (accessKey == null || accessKey.isEmpty()) {
            throw new IllegalStateException("AWS_ACCESS_KEY environment variable is missing.");
        }
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalStateException("AWS_SECRET_KEY environment variable is missing.");
        }
        if (region == null || region.isEmpty()) {
            region = "us-east-1";
        }
        if (bucket == null || bucket.isEmpty()) {
            throw new IllegalStateException("AWS_BUCKET environment variable is missing.");
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    public String subirImagen(String nombreArchivo, Path archivo) {
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(System.getenv(AWS_BUCKET_ENV))
                .key(nombreArchivo)
                .build(), archivo);

        return "https://" + System.getenv(AWS_BUCKET_ENV) + ".s3.amazonaws.com/" + nombreArchivo;
    }

    public void eliminarImagen(String nombreArchivo) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(System.getenv(AWS_BUCKET_ENV))
                .key(nombreArchivo)
                .build());
    }

}
