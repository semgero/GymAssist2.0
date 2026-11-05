package com.ProyectoAula.GymAssist.mobile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tokens JWT (HS256) para la app Flutter. Implementado con javax.crypto para no agregar dependencias.
 * La clave sale de la variable JWT_SECRET; si no existe se genera una al azar (los tokens se
 * invalidan cada vez que el servidor se reinicia).
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64D = Base64.getUrlDecoder();

    private final byte[] secret;
    private final long ttlSegundos;
    private final ObjectMapper mapper = new ObjectMapper();

    public JwtService(@Value("${jwt.secret:}") String secret,
            @Value("${jwt.expiration-hours:72}") long horas) {
        if (secret == null || secret.isBlank()) {
            byte[] r = new byte[32];
            new SecureRandom().nextBytes(r);
            this.secret = r;
            log.warn("JWT_SECRET no está configurado: se generó una clave temporal (los tokens se pierden al reiniciar)");
        } else {
            this.secret = secret.getBytes(StandardCharsets.UTF_8);
        }
        this.ttlSegundos = horas * 3600;
    }

    public String generar(String username, String role) {
        try {
            long ahora = System.currentTimeMillis() / 1000;
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", username);
            payload.put("role", role);
            payload.put("iat", ahora);
            payload.put("exp", ahora + ttlSegundos);
            String header = B64.encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));
            String body = B64.encodeToString(mapper.writeValueAsBytes(payload));
            return header + "." + body + "." + firmar(header + "." + body);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo generar el token", e);
        }
    }

    /** Devuelve los datos del token si la firma es válida y no ha vencido. */
    public Optional<Map<String, Object>> validar(String token) {
        try {
            String[] partes = token.split("\\.");
            if (partes.length != 3) return Optional.empty();
            String esperada = firmar(partes[0] + "." + partes[1]);
            if (!MessageDigest.isEqual(esperada.getBytes(StandardCharsets.UTF_8),
                    partes[2].getBytes(StandardCharsets.UTF_8))) {
                return Optional.empty();
            }
            Map<String, Object> claims = mapper.readValue(B64D.decode(partes[1]), new TypeReference<Map<String, Object>>() {});
            Object exp = claims.get("exp");
            if (!(exp instanceof Number) || ((Number) exp).longValue() < System.currentTimeMillis() / 1000) {
                return Optional.empty();
            }
            return Optional.of(claims);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private String firmar(String datos) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return B64.encodeToString(mac.doFinal(datos.getBytes(StandardCharsets.UTF_8)));
    }
}
