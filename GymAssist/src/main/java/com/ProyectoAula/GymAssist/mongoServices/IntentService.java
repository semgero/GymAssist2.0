package com.ProyectoAula.GymAssist.mongoServices;

import org.springframework.stereotype.Service;
import java.util.List;
import com.ProyectoAula.GymAssist.mongoModels.Intent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;



@Service
public class IntentService {

    private List<Intent> intents;

    @PostConstruct
    public void init() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        InputStream is = getClass()
                .getClassLoader()
                .getResourceAsStream("intents.json");

        JsonNode root = mapper.readTree(is);
        intents = Arrays.asList(
                mapper.treeToValue(root.get("intents"), Intent[].class)
        );
    }

    public Intent detectIntent(String message) {

        message = message.toLowerCase();

        for (Intent intent : intents) {
            for (String pattern : intent.getPatterns()) {
                if (message.contains(pattern)) {
                    return intent;
                }
            }
        }

        return null;
    }
}