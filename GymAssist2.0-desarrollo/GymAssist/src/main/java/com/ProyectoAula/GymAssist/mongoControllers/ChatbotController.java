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

        String response = chatbotService.generateResponse(
                request.getMessage(),
                principal.getName()
        );

        return new ChatbotResponse(response);
    }
}