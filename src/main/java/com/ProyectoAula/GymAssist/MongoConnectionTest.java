package com.ProyectoAula.GymAssist;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConnectionTest {

    @Bean
    CommandLineRunner testMongoConnection(MongoTemplate mongoTemplate) {
        return args -> {
            boolean mongoOk = mongoTemplate.getDb() != null;
            if (mongoOk) {
                System.out.println("✅ Conexión a MongoDB exitosa.");
                System.out.println("📂 Base de datos: " + mongoTemplate.getDb().getName());
            } else {
                System.out.println("❌ No se pudo conectar a MongoDB.");
            }
        };
    }
}
