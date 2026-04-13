package com.ProyectoAula.GymAssist.mongoModels;

public class ChatbotResponse {
    private String response;

    public ChatbotResponse(String response) {
        this.response = response;
    }

    public String getResponse() { return response; }
}