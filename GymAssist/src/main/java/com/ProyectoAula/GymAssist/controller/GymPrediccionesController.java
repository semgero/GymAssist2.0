package com.ProyectoAula.GymAssist.controller;

import com.ProyectoAula.GymAssist.ml.ClienteFeaturesDTO;
import com.ProyectoAula.GymAssist.ml.ClienteFeaturesService;
import com.ProyectoAula.GymAssist.ml.PrediccionMLService;
import com.ProyectoAula.GymAssist.ml.PrediccionNoDisponibleException;
import com.ProyectoAula.GymAssist.mongoServices.ClienteService;
import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private ClienteService clienteService;

    @Autowired
    private ClienteFeaturesService featuresService;

    @Autowired
    private PrediccionMLService mlService;

    /**
     * Obtiene todos los clientes de un gym con sus predicciones
     * GET /api/gym-predicciones/{gymId}
     *
     * Ahora usa la red neuronal LSTM (TensorFlow) de la API de Python.
     * La respuesta mantiene los mismos campos de antes (prediccion, prediccionOriginal,
     * estadoRiesgo, features, recomendacion) y agrega:
     *   - probabilidadAbandono (0 a 1)
     *   - factores: las 3 razones principales del riesgo
     *   - asistenciaSemanal: visitas por semana (últimas 12 semanas)
     *   - modelo: qué modelo hizo la predicción
     * Si la API de Python está apagada responde 503 con un mensaje claro.
     */
    @GetMapping("/{gymId}")
    public ResponseEntity<?> obtenerPrediccionesGym(@PathVariable String gymId) {
        try {
            ObjectId gymObjectId = new ObjectId(gymId);

            // Obtener todos los clientes del gimnasio
            List<ClientEntity> clientes = clienteService.listarClientesPorGym(gymObjectId);

            if (clientes.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "mensaje", "No hay clientes registrados en este gimnasio",
                    "total", 0,
                    "clientes", new ArrayList<>()
                ));
            }

            // Una sola llamada a la API de Python para todo el gimnasio
            Map<String, Map<String, Object>> predicciones = mlService.predecirLote(clientes);

            List<Map<String, Object>> clientesConPrediccion = new ArrayList<>();
            int renovaran = 0;
            int noRenovaran = 0;
            int enRiesgo = 0;
            double sumaProbabilidad = 0;
            int conProbabilidad = 0;
            String modelo = null;

            for (ClientEntity cliente : clientes) {
                try {
                    Map<String, Object> pred = predicciones.get(cliente.getId().toHexString());
                    if (pred == null) continue;
                    modelo = (String) pred.get("modelo");

                    // "Se va" / "Permanece"
                    String prediccionOriginal = (String) pred.get("prediccion");
                    // Normalizar predicción: convertir a "Si" (renueva) o "No"
                    String prediccionNormalizada = "Permanece".equalsIgnoreCase(prediccionOriginal) ? "Si" : "No";

                    Double probabilidad = pred.get("probabilidadAbandono") instanceof Number n ? n.doubleValue() : null;
                    String estadoRiesgo = determinarRiesgo(cliente, prediccionNormalizada, pred);

                    // Contar estadísticas
                    if ("Si".equals(prediccionNormalizada)) {
                        renovaran++;
                    } else {
                        noRenovaran++;
                        if (cliente.getEstado() == ClientEntity.EstadoCliente.ACTIVO) {
                            enRiesgo++;
                        }
                    }
                    if (probabilidad != null) {
                        sumaProbabilidad += probabilidad;
                        conProbabilidad++;
                    }

                    Map<String, Object> clienteInfo = new HashMap<>();
                    clienteInfo.put("id", cliente.getId().toHexString());
                    clienteInfo.put("nombre", cliente.getNombre());
                    clienteInfo.put("correo", cliente.getCorreo());
                    clienteInfo.put("telefono", cliente.getTelefono());
                    clienteInfo.put("estado", cliente.getEstado().name());
                    clienteInfo.put("prediccion", prediccionNormalizada); // "Si" o "No"
                    clienteInfo.put("prediccionOriginal", prediccionOriginal); // "Permanece" o "Se va"
                    clienteInfo.put("estadoRiesgo", estadoRiesgo);
                    clienteInfo.put("features", pred.get("features"));
                    clienteInfo.put("recomendacion", pred.get("recomendacion") != null
                            ? pred.get("recomendacion")
                            : recomendacionBasica(cliente, prediccionNormalizada));

                    // Campos nuevos del modelo de TensorFlow
                    clienteInfo.put("probabilidadAbandono", probabilidad);
                    clienteInfo.put("factores", pred.get("factores"));
                    clienteInfo.put("asistenciaSemanal", pred.get("asistenciaSemanal"));
                    clienteInfo.put("modelo", pred.get("modelo"));

                    clientesConPrediccion.add(clienteInfo);

                } catch (Exception e) {
                    System.err.println("Error procesando cliente " + cliente.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Ordenar: primero los de mayor probabilidad de abandono
            clientesConPrediccion.sort((a, b) -> Double.compare(
                    a.get("probabilidadAbandono") instanceof Number n1 ? n1.doubleValue() : 0,
                    b.get("probabilidadAbandono") instanceof Number n2 ? n2.doubleValue() : 0) * -1);

            Map<String, Object> response = new HashMap<>();
            response.put("gymId", gymId);
            response.put("total", clientes.size());
            response.put("procesados", clientesConPrediccion.size());
            response.put("modelo", modelo);

            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("renovaran", renovaran);
            estadisticas.put("noRenovaran", noRenovaran);
            estadisticas.put("enRiesgo", enRiesgo);
            estadisticas.put("probabilidadPromedio",
                    conProbabilidad > 0 ? Math.round(sumaProbabilidad / conProbabilidad * 1000) / 1000.0 : null);
            response.put("estadisticas", estadisticas);

            response.put("clientes", clientesConPrediccion);

            return ResponseEntity.ok(response);

        } catch (PrediccionNoDisponibleException e) {
            return noDisponible(e);
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
     * Nivel de riesgo según la probabilidad de la red neuronal
     * (BAJO / MEDIO / ALTO / CRITICO). Los clientes no activos que se
     * van se marcan como ESPERADO, igual que antes.
     */
    private String determinarRiesgo(ClientEntity cliente, String prediccion, Map<String, Object> pred) {
        boolean activo = cliente.getEstado() == ClientEntity.EstadoCliente.ACTIVO;
        if ("No".equals(prediccion) && !activo) return "ESPERADO";
        Object riesgo = pred.get("riesgo");
        return riesgo != null ? riesgo.toString() : "MEDIO";
    }

    private ResponseEntity<Map<String, Object>> noDisponible(PrediccionNoDisponibleException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                "error", "Servicio de predicción no disponible",
                "mensaje", e.getMessage()));
    }

    private String recomendacionBasica(ClientEntity cliente, String prediccion) {
        if ("Si".equals(prediccion)) return "Mantener comunicación y ofrecer beneficios adicionales.";
        if (cliente.getEstado() == ClientEntity.EstadoCliente.ACTIVO)
            return "Programar llamada. Indagar motivos y ofrecer beneficios especiales.";
        return "Cliente inactivo. Considerar campaña de reactivación con descuento.";
    }

    /**
     * Predicción detallada de un solo cliente.
     * GET /api/gym-predicciones/cliente/{clienteId}
     */
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<?> prediccionCliente(@PathVariable String clienteId) {
        try {
            ClientEntity cliente = clienteService.buscarClientePorId(new ObjectId(clienteId));
            if (cliente == null) return ResponseEntity.notFound().build();
            return ResponseEntity.ok(mlService.predecir(cliente));
        } catch (PrediccionNoDisponibleException e) {
            return noDisponible(e);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Mejor asignación de ofertas de retención con un presupuesto (Pyomo).
     * GET /api/gym-predicciones/{gymId}/ofertas?presupuesto=500000&maxOfertas=10
     */
    @GetMapping("/{gymId}/ofertas")
    public ResponseEntity<?> optimizarOfertas(@PathVariable String gymId,
                                              @RequestParam(defaultValue = "500000") double presupuesto,
                                              @RequestParam(required = false) Integer maxOfertas,
                                              @RequestParam(defaultValue = "6") double mesesValor) {
        try {
            List<ClientEntity> clientes = clienteService.listarClientesPorGym(new ObjectId(gymId));
            if (clientes.isEmpty()) {
                return ResponseEntity.ok(Map.of("mensaje", "No hay clientes en este gimnasio", "asignaciones", List.of()));
            }
            return ResponseEntity.ok(mlService.optimizarOfertas(clientes, presupuesto, maxOfertas, mesesValor));
        } catch (PrediccionNoDisponibleException e) {
            return noDisponible(e);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "No se pudo optimizar las ofertas",
                    "mensaje", e.getMessage()));
        }
    }

    /**
     * Reentrena la red neuronal con los clientes reales del gimnasio.
     * POST /api/gym-predicciones/{gymId}/reentrenar
     */
    @PostMapping("/{gymId}/reentrenar")
    public ResponseEntity<?> reentrenar(@PathVariable String gymId) {
        try {
            List<ClientEntity> clientes = clienteService.listarClientesPorGym(new ObjectId(gymId));
            return ResponseEntity.ok(mlService.reentrenar(clientes));
        } catch (PrediccionNoDisponibleException e) {
            return noDisponible(e);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Estado y métricas del modelo de Python.
     * GET /api/gym-predicciones/modelo/estado
     */
    @GetMapping("/modelo/estado")
    public ResponseEntity<?> estadoModelo() {
        Map<String, Object> r = new HashMap<>(mlService.salud());
        try {
            r.put("comparacion", mlService.metricas());
        } catch (Exception ignored) {
            // la API no está disponible o aún no hay métricas
        }
        return ResponseEntity.ok(r);
    }

    /**
     * Obtiene estadísticas resumidas del gimnasio
     */
    @GetMapping("/{gymId}/estadisticas")
    public ResponseEntity<?> obtenerEstadisticas(@PathVariable String gymId) {
        try {
            ObjectId gymObjectId = new ObjectId(gymId);
            List<ClientEntity> clientes = clienteService.listarClientesPorGym(gymObjectId);

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
