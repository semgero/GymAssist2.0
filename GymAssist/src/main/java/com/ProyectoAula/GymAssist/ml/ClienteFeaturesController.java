package com.ProyectoAula.GymAssist.ml;

import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoRepository.ClientRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/features")
public class ClienteFeaturesController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClienteFeaturesService featuresService;

    @Autowired
    private PrediccionMLService mlService;

    /** Las 4 variables básicas del cliente. GET /api/features/{id} */
    @GetMapping("/{id}")
    public Object obtenerFeaturesCliente(@PathVariable ObjectId id) {
        Optional<ClientEntity> clienteOpt = clientRepository.findById(id);
        if (clienteOpt.isEmpty()) {
            return "❌ Cliente no encontrado con ID: " + id;
        }
        return featuresService.generarFeatures(clienteOpt.get());
    }

    /** Predicción de la red neuronal para un cliente. GET /api/features/predecir/{id} */
    @GetMapping("/predecir/{id}")
    public ResponseEntity<?> predecirCliente(@PathVariable ObjectId id) {
        Optional<ClientEntity> clienteOpt = clientRepository.findById(id);
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Cliente no encontrado con ID: " + id));
        }
        try {
            return ResponseEntity.ok(mlService.predecir(clienteOpt.get()));
        } catch (PrediccionNoDisponibleException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Servicio de predicción no disponible", "mensaje", e.getMessage()));
        }
    }
}
