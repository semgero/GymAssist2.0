package com.ProyectoAula.GymAssist.mongoServices;

import org.springframework.stereotype.Service;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.MedicionesEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import com.ProyectoAula.GymAssist.mongoModels.Intent;
import java.util.Random;



@Service
public class ChatbotService {

    @Autowired
    private IntentService intentService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PlanService planService;

    @Autowired
    private MedicionesService medicionesService;

    public String generateResponse(String message, String username) {

        Intent intent = intentService.detectIntent(message);
        ClientEntity cliente = clienteService.buscarPorUsername(username);

        if (intent == null) {
            return "🤔 No entendí. Prueba con: plan, imc, estado";
        }

        String tag = intent.getTag();

        switch (tag) {

            case "SALUDO":
                return getRandom(intent.getResponses()) +
                       "\n👤 " + cliente.getNombre();

            case "PLAN":
                return "💳 Tu plan es: " +
                        planService.obtenerNombreDelPlan(cliente.getPlanId());

            case "IMC":
                return obtenerIMC(cliente);

            default:
                return getRandom(intent.getResponses());
        }
    }

    private String getRandom(List<String> responses) {
        return responses.get(new Random().nextInt(responses.size()));
    }

    private String obtenerIMC(ClientEntity cliente) {

        Optional<MedicionesEntity> ultima =
                medicionesService.obtenerUltimaMedicion(cliente.getId());

        if (ultima.isEmpty()) {
            return "⚠️ No tienes mediciones registradas.";
        }

        MedicionesEntity m = ultima.get();

        double imc = medicionesService.calcularIMC(
                m.getPeso(),
                m.getEstatura()
        );

        String resultado = medicionesService.interpretarIMC(imc);

        return "📊 IMC: " + String.format("%.2f", imc) +
               "\n📌 Estado: " + resultado;
    }
}