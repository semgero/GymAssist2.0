package com.ProyectoAula.GymAssist.mongoControllers;

import com.ProyectoAula.GymAssist.mongoModels.ChatbotRequest;
import com.ProyectoAula.GymAssist.mongoModels.ChatbotResponse;
import com.ProyectoAula.GymAssist.mongoServices.ChatbotService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/chatbot")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping
    public ChatbotResponse chat(@RequestBody ChatbotRequest request,
                                Principal principal) {

        if (principal == null) {
            return new ChatbotResponse("⚠️ Debes iniciar sesión como cliente para usar el chat.");
        }
        try {
            String response = chatbotService.generateResponse(
                    request.getMessage(),
                    principal.getName()
            );
            return new ChatbotResponse(response);
        } catch (Exception e) {
            System.err.println("Error en el chatbot: " + e.getMessage());
            return new ChatbotResponse("⚠️ No pude responder: " + e.getMessage());
        }
    }
}