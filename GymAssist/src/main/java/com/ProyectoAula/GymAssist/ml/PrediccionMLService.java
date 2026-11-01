package com.ProyectoAula.GymAssist.ml;

import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.MedicionesEntity;
import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;
import com.ProyectoAula.GymAssist.mongoRepository.MedicionesRepository;
import com.ProyectoAula.GymAssist.mongoRepository.PlanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;

/**
 * Conecta Spring Boot con la API de predicción en Python (FastAPI + TensorFlow).
 *
 * - Arma el JSON de cada cliente (asistencias, membresía, plan, mediciones).
 * - Llama a la API (por defecto http://localhost:8000, configurable con ml.api.url).
 * - Si la API de Python no está disponible, lanza PrediccionNoDisponibleException
 *   con un mensaje claro.
 */
@Service
public class PrediccionMLService {

    private static final Logger log = LoggerFactory.getLogger(PrediccionMLService.class);
    private static final ParameterizedTypeReference<Map<String, Object>> MAPA = new ParameterizedTypeReference<>() {};

    private final RestClient api;
    private final RestClient apiLenta; // para reentrenar (puede tardar varios minutos)
    private final PlanRepository planRepository;
    private final MedicionesRepository medicionesRepository;
    private final String urlApi;

    public PrediccionMLService(@Value("${ml.api.url:http://localhost:8000}") String urlApi,
                               PlanRepository planRepository,
                               MedicionesRepository medicionesRepository) {
        this.api = crearCliente(urlApi, 60_000);
        this.apiLenta = crearCliente(urlApi, 900_000);
        this.planRepository = planRepository;
        this.medicionesRepository = medicionesRepository;
        this.urlApi = urlApi;
    }

    private static RestClient crearCliente(String url, int timeoutLecturaMs) {
        SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(3_000);
        f.setReadTimeout(timeoutLecturaMs);
        return RestClient.builder().baseUrl(url).requestFactory(f).build();
    }

    // ------------------------------------------------------------------ JSON del cliente

    /** Convierte un cliente de MongoDB al formato que espera la API de Python. */
    public Map<String, Object> aJson(ClientEntity c, Map<String, PlanEntity> planes) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", c.getId().toHexString());
        m.put("nombre", c.getNombre());
        m.put("estado", c.getEstado() != null ? c.getEstado().name() : null);
        m.put("fechaIngreso", texto(c.getFechaIngresoCliente()));
        m.put("fechaInicioMembresia", texto(c.getFechaInicioMembresia()));
        m.put("fechaFinMembresia", texto(c.getFechaFinMembresia()));

        PlanEntity plan = c.getPlanId() != null ? planes.get(c.getPlanId().toHexString()) : null;
        if (plan != null) {
            m.put("tipoPlan", plan.getNombre());
            m.put("duracionPlanMeses", plan.getDuracion());
            m.put("precioPlan", plan.getPrecio());
        } else {
            m.put("tipoPlan", c.getMensualidad());
        }

        List<Map<String, Object>> asistencias = new ArrayList<>();
        if (c.getAsistencias() != null) {
            for (ClientEntity.Asistencia a : c.getAsistencias()) {
                if (a == null || a.getFecha() == null) continue;
                Map<String, Object> am = new HashMap<>();
                am.put("fecha", a.getFecha().toString());
                am.put("musculos", a.getMusculos() != null ? a.getMusculos() : List.of());
                asistencias.add(am);
            }
        }
        m.put("asistencias", asistencias);

        List<Map<String, Object>> mediciones = new ArrayList<>();
        try {
            for (MedicionesEntity med : medicionesRepository.findByClienteIdOrderByFechaRegistroDesc(c.getId())) {
                if (med.getFechaRegistro() == null) continue;
                Map<String, Object> mm = new HashMap<>();
                mm.put("fecha", med.getFechaRegistro().toLocalDate().toString());
                mm.put("peso", med.getPeso());
                mm.put("estatura", med.getEstatura());
                mediciones.add(mm);
            }
        } catch (Exception e) {
            log.warn("No se pudieron leer las mediciones del cliente {}: {}", c.getId(), e.getMessage());
        }
        m.put("mediciones", mediciones);
        return m;
    }

    private static String texto(Object fecha) {
        return fecha != null ? fecha.toString() : null;
    }

    private Map<String, PlanEntity> planesDe(List<ClientEntity> clientes) {
        Set<org.bson.types.ObjectId> ids = new HashSet<>();
        for (ClientEntity c : clientes) if (c.getPlanId() != null) ids.add(c.getPlanId());
        Map<String, PlanEntity> planes = new HashMap<>();
        planRepository.findAllById(ids).forEach(p -> planes.put(p.getId().toHexString(), p));
        return planes;
    }

    private List<Map<String, Object>> aJson(List<ClientEntity> clientes) {
        Map<String, PlanEntity> planes = planesDe(clientes);
        List<Map<String, Object>> lista = new ArrayList<>();
        for (ClientEntity c : clientes) lista.add(aJson(c, planes));
        return lista;
    }

    // ------------------------------------------------------------------ llamadas a la API

    /** ¿Está encendida la API de Python? */
    public Map<String, Object> salud() {
        try {
            return api.get().uri("/salud").retrieve().body(MAPA);
        } catch (Exception e) {
            return Map.of("estado", "no_disponible", "mensaje", "La API de Python no responde: " + e.getMessage());
        }
    }

    /**
     * Predice todos los clientes en una sola llamada.
     * Devuelve un mapa idCliente -> resultado (probabilidadAbandono, prediccion, riesgo, factores, ...).
     */
    @SuppressWarnings("unchecked")
    public Map<String, Map<String, Object>> predecirLote(List<ClientEntity> clientes) {
        Map<String, Map<String, Object>> salida = new LinkedHashMap<>();
        if (clientes.isEmpty()) return salida;
        try {
            Map<String, Object> resp = api.post().uri("/predecir/lote")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("clientes", aJson(clientes)))
                    .retrieve().body(MAPA);
            for (Object o : (List<Object>) resp.get("resultados")) {
                Map<String, Object> r = (Map<String, Object>) o;
                r.put("modelo", "Red híbrida LSTM (TensorFlow) v" + resp.get("modeloVersion"));
                salida.put((String) r.get("id"), r);
            }
            return salida;
        } catch (Exception e) {
            log.error("La API de predicción en Python no responde ({}): {}", urlApi, e.getMessage());
            throw new PrediccionNoDisponibleException(urlApi, e);
        }
    }

    public Map<String, Object> predecir(ClientEntity cliente) {
        return predecirLote(List.of(cliente)).get(cliente.getId().toHexString());
    }

    /** Pide a Python (Pyomo) la mejor asignación de ofertas de retención con un presupuesto. */
    public Map<String, Object> optimizarOfertas(List<ClientEntity> clientes, double presupuesto,
                                                Integer maxOfertas, double mesesValor) {
        Map<String, Object> body = new HashMap<>();
        body.put("clientes", aJson(clientes));
        body.put("presupuesto", presupuesto);
        body.put("maxOfertas", maxOfertas);
        body.put("mesesValor", mesesValor);
        try {
            return api.post().uri("/optimizar-ofertas").contentType(MediaType.APPLICATION_JSON)
                    .body(body).retrieve().body(MAPA);
        } catch (Exception e) {
            throw new PrediccionNoDisponibleException(urlApi, e);
        }
    }

    /** Envía los clientes reales para que el modelo se reentrene con ellos. */
    public Map<String, Object> reentrenar(List<ClientEntity> clientes) {
        try {
            return apiLenta.post().uri("/reentrenar").contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("clientes", aJson(clientes))).retrieve().body(MAPA);
        } catch (Exception e) {
            throw new PrediccionNoDisponibleException(urlApi, e);
        }
    }

    public Map<String, Object> metricas() {
        try {
            return api.get().uri("/modelo/metricas").retrieve().body(MAPA);
        } catch (Exception e) {
            throw new PrediccionNoDisponibleException(urlApi, e);
        }
    }
}
