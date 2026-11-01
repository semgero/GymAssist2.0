package com.ProyectoAula.GymAssist.mongoServices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GroqApiClient {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.model}")
    private String model;

    private final RestTemplate restTemplate;

    public GroqApiClient() {
        this.restTemplate = new RestTemplate();
    }

    @SuppressWarnings("unchecked")
    public String getChatCompletion(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("${")) {
            return "Error: La API Key de GROQ no está configurada.";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            Map<String, Object> response = restTemplate.postForObject(apiUrl, entity, Map.class);
            
            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (!choices.isEmpty()) {
                    Map<String, Object> firstChoice = choices.get(0);
                    Map<String, String> messageMap = (Map<String, String>) firstChoice.get("message");
                    return messageMap.get("content");
                }
            }
            return "Lo siento, no obtuve una respuesta válida del modelo.";
        } catch (HttpStatusCodeException e) {
            int codigo = e.getStatusCode().value();
            System.err.println("Error de la API de GROQ (" + codigo + "): " + e.getResponseBodyAsString());
            if (codigo == 401) {
                return "⚠️ La API Key de GROQ no es válida. Revisa la variable de entorno GROQ_API_KEY.";
            }
            if (codigo == 429) {
                return "⚠️ Se alcanzó el límite de uso de GROQ. Intenta de nuevo en unos minutos.";
            }
            if (codigo == 400 || codigo == 404) {
                return "⚠️ El modelo '" + model + "' no está disponible en GROQ. Revisa groq.model en application.properties.";
            }
            return "Lo siento, el servicio de inteligencia artificial respondió con un error (" + codigo + ").";
        } catch (ResourceAccessException e) {
            System.err.println("No se pudo conectar con GROQ: " + e.getMessage());
            return "⚠️ No hay conexión con el servicio de inteligencia artificial (GROQ).";
        } catch (Exception e) {
            System.err.println("Error calling GROQ API: " + e.getMessage());
            return "Lo siento, el servicio de inteligencia artificial no está disponible actualmente.";
        }
    }
}
