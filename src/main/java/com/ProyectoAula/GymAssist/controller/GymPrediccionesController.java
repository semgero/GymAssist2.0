package com.ProyectoAula.GymAssist.controller;

import com.ProyectoAula.GymAssist.Weka.ClienteFeaturesDTO;
import com.ProyectoAula.GymAssist.Weka.ClienteFeaturesService;
import com.ProyectoAula.GymAssist.Weka.ClientePredictionService;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoRepository.ClientRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gym-predicciones")
@CrossOrigin(origins = "*")
public class GymPrediccionesController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClienteFeaturesService featuresService;

    @Autowired
    private ClientePredictionService predictionService;

    /**
     * Obtiene todos los clientes de un gym con sus predicciones
     * GET /api/gym-predicciones/{gymId}
     */
    @GetMapping("/{gymId}")
    public ResponseEntity<?> obtenerPrediccionesGym(@PathVariable String gymId) {
        try {
            ObjectId gymObjectId = new ObjectId(gymId);
            
            // Obtener todos los clientes del gimnasio
            List<ClientEntity> clientes = clientRepository.findByGymId(gymObjectId);
            
            if (clientes.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "mensaje", "No hay clientes registrados en este gimnasio",
                    "total", 0,
                    "clientes", new ArrayList<>()
                ));
            }
            
            // Procesar cada cliente y generar predicción
            List<Map<String, Object>> clientesConPrediccion = new ArrayList<>();
            int renovaran = 0;
            int noRenovaran = 0;
            int enRiesgo = 0;
            
            for (ClientEntity cliente : clientes) {
                try {
                    // Generar features del cliente
                    ClienteFeaturesDTO features = featuresService.generarFeatures(cliente);
                    
                    // Realizar predicción (retorna "Permanece" o "Abandona")
                    String prediccionWeka = predictionService.predecirRenovacion(features);
                    
                    // Normalizar predicción: convertir a "Si" o "No"
                    String prediccionNormalizada = normalizarPrediccion(prediccionWeka);
                    
                    // Determinar estado de riesgo
                    String estadoRiesgo = determinarRiesgo(cliente, prediccionNormalizada, features);
                    
                    // Contar estadísticas
                    if ("Si".equals(prediccionNormalizada)) {
                        renovaran++;
                    } else {
                        noRenovaran++;
                        if (cliente.getEstado() == ClientEntity.EstadoCliente.ACTIVO) {
                            enRiesgo++;
                        }
                    }
                    
                    // Crear objeto con información del cliente
                    Map<String, Object> clienteInfo = new HashMap<>();
                    clienteInfo.put("id", cliente.getId().toHexString());
                    clienteInfo.put("nombre", cliente.getNombre());
                    clienteInfo.put("correo", cliente.getCorreo());
                    clienteInfo.put("telefono", cliente.getTelefono());
                    clienteInfo.put("estado", cliente.getEstado().name());
                    clienteInfo.put("prediccion", prediccionNormalizada); // "Si" o "No"
                    clienteInfo.put("prediccionOriginal", prediccionWeka); // "Permanece" o "Abandona"
                    clienteInfo.put("estadoRiesgo", estadoRiesgo);
                    
                    // Features
                    Map<String, Object> featuresMap = new HashMap<>();
                    featuresMap.put("asistencias4Semanas", features.getAsistencias4Semanas());
                    featuresMap.put("pagoAlDia", features.getPagoAlDia());
                    featuresMap.put("antiguedadMeses", features.getAntiguedadMeses());
                    featuresMap.put("tipoPlan", features.getTipoPlan());
                    clienteInfo.put("features", featuresMap);
                    
                    // Recomendación personalizada
                    clienteInfo.put("recomendacion", generarRecomendacion(cliente, prediccionNormalizada, features));
                    
                    clientesConPrediccion.add(clienteInfo);
                    
                } catch (Exception e) {
                    System.err.println("Error procesando cliente " + cliente.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Preparar respuesta con estadísticas
            Map<String, Object> response = new HashMap<>();
            response.put("gymId", gymId);
            response.put("total", clientes.size());
            response.put("procesados", clientesConPrediccion.size());
            
            // Estadísticas
            Map<String, Integer> estadisticas = new HashMap<>();
            estadisticas.put("renovaran", renovaran);
            estadisticas.put("noRenovaran", noRenovaran);
            estadisticas.put("enRiesgo", enRiesgo);
            response.put("estadisticas", estadisticas);
            
            response.put("clientes", clientesConPrediccion);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "ID de gimnasio inválido",
                "mensaje", e.getMessage()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error al obtener predicciones",
                "mensaje", e.getMessage()
            ));
        }
    }
    
    /**
     * Normaliza la predicción de Weka a formato estándar
     * "Permanece" -> "Si"
     * "Abandona" -> "No"
     */
    private String normalizarPrediccion(String prediccionWeka) {
        if (prediccionWeka == null) {
            return "No";
        }
        
        // Convertir a minúsculas y eliminar espacios
        String pred = prediccionWeka.trim().toLowerCase();
        
        // Mapear diferentes posibles valores
        if (pred.contains("permanece") || pred.equals("si") || pred.equals("yes") || pred.equals("1")) {
            return "Si";
        } else if (pred.contains("abandona") || pred.equals("no") || pred.equals("0")) {
            return "No";
        }
        
        // Por defecto, si no se reconoce
        return "No";
    }
    
    /**
     * Determina el nivel de riesgo del cliente
     */
    private String determinarRiesgo(ClientEntity cliente, String prediccion, ClienteFeaturesDTO features) {
        if ("Si".equals(prediccion)) {
            if (features.getAsistencias4Semanas() >= 12) {
                return "BAJO"; // Cliente muy comprometido
            } else {
                return "MEDIO"; // Renovará pero no muy activo
            }
        } else {
            if (cliente.getEstado() == ClientEntity.EstadoCliente.ACTIVO) {
                if (features.getAsistencias4Semanas() < 4) {
                    return "CRITICO"; // Cliente activo que no asiste y no renovará
                } else {
                    return "ALTO"; // Cliente activo pero no renovará
                }
            } else {
                return "ESPERADO"; // Ya está suspendido o pendiente
            }
        }
    }
    
    /**
     * Genera una recomendación personalizada para cada cliente
     */
    private String generarRecomendacion(ClientEntity cliente, String prediccion, ClienteFeaturesDTO features) {
        if ("Si".equals(prediccion)) {
            if (features.getAsistencias4Semanas() >= 12) {
                return "Cliente ideal. Considerar ofrecerle plan premium o descuento por referidos.";
            } else if (features.getAntiguedadMeses() >= 6) {
                return "Cliente leal. Mantener comunicación y ofrecer beneficios adicionales.";
            } else {
                return "Cliente con buen potencial. Programar seguimiento mensual.";
            }
        } else {
            if (cliente.getEstado() == ClientEntity.EstadoCliente.ACTIVO) {
                if (features.getAsistencias4Semanas() < 2) {
                    return "URGENTE: Contactar inmediatamente. Ofrecer sesión personalizada gratuita.";
                } else if ("No".equals(features.getPagoAlDia())) {
                    return "Ofrecer plan de pagos flexible o descuento por pronto pago.";
                } else {
                    return "Programar llamada. Indagar motivos y ofrecer beneficios especiales.";
                }
            } else {
                return "Cliente inactivo. Considerar campaña de reactivación con descuento.";
            }
        }
    }
    
    /**
     * Obtiene estadísticas resumidas del gimnasio
     */
    @GetMapping("/{gymId}/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas(@PathVariable String gymId) {
        try {
            ObjectId gymObjectId = new ObjectId(gymId);
            List<ClientEntity> clientes = clientRepository.findByGymId(gymObjectId);
            
            int activos = 0, pendientes = 0, suspendidos = 0;
            int conPagoAlDia = 0;
            double promedioAsistencias = 0;
            
            for (ClientEntity cliente : clientes) {
                switch (cliente.getEstado()) {
                    case ACTIVO: activos++; break;
                    case PENDIENTE: pendientes++; break;
                    case SUSPENDIDO: suspendidos++; break;
                }
                
                ClienteFeaturesDTO features = featuresService.generarFeatures(cliente);
                if ("Si".equals(features.getPagoAlDia())) {
                    conPagoAlDia++;
                }
                promedioAsistencias += features.getAsistencias4Semanas();
            }
            
            if (clientes.size() > 0) {
                promedioAsistencias /= clientes.size();
            }
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalClientes", clientes.size());
            stats.put("activos", activos);
            stats.put("pendientes", pendientes);
            stats.put("suspendidos", suspendidos);
            stats.put("conPagoAlDia", conPagoAlDia);
            stats.put("promedioAsistencias4Semanas", String.format("%.1f", promedioAsistencias));
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error al obtener estadísticas",
                "mensaje", e.getMessage()
            ));
        }
    }
}