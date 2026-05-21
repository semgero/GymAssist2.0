package com.ProyectoAula.GymAssist.mongoServices;

import org.springframework.stereotype.Service;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.MedicionesEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
@Service
public class ChatbotService {

    @Autowired
    private GroqApiClient groqApiClient;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private PlanService planService;

    @Autowired
    private MedicionesService medicionesService;

    public String generateResponse(String message, String username) {

        ClientEntity cliente = clienteService.buscarPorUsername(username);
        
        String planNombre = "Ninguno";
        if (cliente.getPlanId() != null) {
            planNombre = planService.obtenerNombreDelPlan(cliente.getPlanId());
        }

        String estadoFisico = obtenerEstadoFisico(cliente);
        int numeroAsistencias = (cliente.getAsistencias() != null) ? cliente.getAsistencias().size() : 0;

        String systemPrompt = String.format(
            "Eres un asistente de inteligencia artificial amigable, servicial y experto llamado GymAssistBot. " +
            "Estás diseñado para ayudar a los clientes del gimnasio GymAssist. " +
            "Actualmente estás hablando con el cliente: %s. " +
            "Información del cliente:\n" +
            "- Plan actual: %s\n" +
            "- %s\n" +
            "- Total de asistencias al gimnasio: %d\n\n" +
            "Instrucciones:\n" +
            "1. Puedes tener conversaciones generales y resolver dudas de cualquier tipo.\n" +
            "2. Sin embargo, si te preguntan sobre su cuenta, plan, asistencias, estado físico o gimnasio, utiliza la información proporcionada.\n" +
            "3. Mantén tus respuestas concisas, amigables y motivadoras (usa emojis apropiados).\n" +
            "4. Habla siempre en español.",
            cliente.getNombre(), planNombre, estadoFisico, numeroAsistencias
        );

        return groqApiClient.getChatCompletion(systemPrompt, message);
    }

    private String obtenerEstadoFisico(ClientEntity cliente) {
        Optional<MedicionesEntity> ultima = medicionesService.obtenerUltimaMedicion(cliente.getId());

        if (ultima.isEmpty()) {
            return "El cliente no tiene mediciones registradas actualmente.";
        }

        MedicionesEntity m = ultima.get();
        double imc = medicionesService.calcularIMC(m.getPeso(), m.getEstatura());
        String resultado = medicionesService.interpretarIMC(imc);

        return String.format("Último IMC registrado: %.2f (Estado: %s).", imc, resultado);
    }
}