package com.ProyectoAula.GymAssist.Weka;

import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoRepository.ClientRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/api/features")
public class ClienteFeaturesController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClienteFeaturesService featuresService;

    @Autowired
    private ClientePredictionService predictionService;

    @GetMapping("/{id}")
    public Object obtenerFeaturesCliente(@PathVariable ObjectId id) {
        Optional<ClientEntity> clienteOpt = clientRepository.findById(id);

        if (clienteOpt.isEmpty()) {
            return "❌ Cliente no encontrado con ID: " + id;
        }

        ClientEntity cliente = clienteOpt.get();
        ClienteFeaturesDTO features = featuresService.generarFeatures(cliente);

        return features;
    }

    @GetMapping("/predecir/{id}")
    public Object predecirCliente(@PathVariable ObjectId id) {
        Optional<ClientEntity> clienteOpt = clientRepository.findById(id);
        if (clienteOpt.isEmpty()) {
            return "❌ Cliente no encontrado con ID: " + id;
        }

        ClientEntity cliente = clienteOpt.get();
        ClienteFeaturesDTO features = featuresService.generarFeatures(cliente);
        String prediccion = predictionService.predecirRenovacion(features);

        return Map.of(
                "cliente", cliente.getNombre(),
                "features", features,
                "prediccion", prediccion);
    }
}
