/*package com.ProyectoAula.GymAssist.mongoServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ProyectoAula.GymAssist.mongoModels.RutinaEntity;

import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void guardarRutina(String clave, RutinaEntity rutina) {
        redisTemplate.opsForValue().set(clave, rutina, Duration.ofHours(2)); // Guardar como JSON
    }

    public RutinaEntity obtenerRutina(String clave) {
        return (RutinaEntity) redisTemplate.opsForValue().get(clave);
    }
}*/