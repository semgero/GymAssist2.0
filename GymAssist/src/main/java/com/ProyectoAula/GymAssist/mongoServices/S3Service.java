package com.ProyectoAula.GymAssist.mongoServices;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import java.nio.file.Path;
import org.springframework.stereotype.Service;

@Service
public class S3Service {
    private final S3Client s3Client;

    public S3Service() {
        String accessKey = System.getenv("AWS_ACCESS_KEY");
        String secretKey = System.getenv("AWS_SECRET_KEY");
        String region = System.getenv("AWS_REGION");

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    public String subirImagen(String nombreArchivo, Path archivo) {
        s3Client.putObject(PutObjectRequest.builder()
                .bucket(System.getenv("AWS_BUCKET"))
                .key(nombreArchivo)
                .build(), archivo);

        return "https://" + System.getenv("AWS_BUCKET") + ".s3.amazonaws.com/" + nombreArchivo;
    }
}
