package com.ProyectoAula.GymAssist.mobile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.GimnasiosEntity;
import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;
import com.ProyectoAula.GymAssist.mongoModels.RutinaEntity;
import com.ProyectoAula.GymAssist.mongoRepository.ClientRepository;
import com.ProyectoAula.GymAssist.mongoServices.AdminService;
import com.ProyectoAula.GymAssist.mongoServices.ClienteService;
import com.ProyectoAula.GymAssist.mongoServices.DashboardService;
import com.ProyectoAula.GymAssist.mongoServices.GimnasiosServices;
import com.ProyectoAula.GymAssist.mongoServices.PagoMembresiaService;
import com.ProyectoAula.GymAssist.mongoServices.PlanService;
import com.ProyectoAula.GymAssist.mongoServices.RutinaService;
import com.ProyectoAula.GymAssist.ml.PrediccionMLService;

/** API del administrador para la app Flutter (requiere token con rol ADMIN). */
@RestController
@RequestMapping("/api/m/admin")
public class MobileAdminController {

    private final MobileContext ctx;
    private final GimnasiosServices gymService;
    private final ClienteService clienteService;
    private final PlanService planService;
    private final RutinaService rutinaService;
    private final ClientRepository clients;
    private final PrediccionMLService ml;
    private final DashboardService dashboardService;
    private final AdminService adminService;
    private final PagoMembresiaService pagoMembresia;

    public MobileAdminController(MobileContext ctx, GimnasiosServices gymService, ClienteService clienteService,
            PlanService planService, RutinaService rutinaService, ClientRepository clients, PrediccionMLService ml,
            DashboardService dashboardService, AdminService adminService, PagoMembresiaService pagoMembresia) {
        this.pagoMembresia = pagoMembresia;
        this.ml = ml;
        this.dashboardService = dashboardService;
        this.adminService = adminService;
        this.ctx = ctx;
        this.gymService = gymService;
        this.clienteService = clienteService;
        this.planService = planService;
        this.rutinaService = rutinaService;
        this.clients = clients;
    }

    // ------------------------------------------------------------------ gimnasio
    @GetMapping("/gimnasio")
    public Map<String, Object> gimnasio() {
        return MobileJson.gym(ctx.gymDelAdmin());
    }

    @PostMapping("/gimnasio")
    public Map<String, Object> registrarGimnasio(@RequestBody Map<String, String> b) {
        var u = ctx.usuario();
        var admin = ctx.admin(u).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "No eres administrador"));
        if (ctx.gymDe(u).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya tienes un gimnasio registrado");
        GimnasiosEntity g = new GimnasiosEntity();
        g.setNombreGymnasio(requerido(b, "nombre"));
        g.setNit(entero(b.get("nit"), "NIT"));
        g.setDireccion(requerido(b, "direccion"));
        g.setDescripcion(b.getOrDefault("descripcion", ""));
        g.setAdminId(admin.getId());
        return MobileJson.gym(gymService.createGym(g));
    }

    // ------------------------------------------------------------------ resumen
    @GetMapping("/resumen")
    public Map<String, Object> resumen() {
        GimnasiosEntity gym = ctx.gymDelAdmin();
        List<ClientEntity> lista = clienteService.listarClientesPorGym(gym.getId());
        Map<ObjectId, PlanEntity> planes = planesPorId(gym);
        LocalDate hoy = LocalDate.now();
        long activos = 0, pendientes = 0, suspendidos = 0, porVencer = 0;
        double ingresosMensuales = 0;
        for (ClientEntity c : lista) {
            var e = c.getEstado() == null ? ClientEntity.EstadoCliente.PENDIENTE : c.getEstado();
            switch (e) {
                case ACTIVO -> {
                    activos++;
                    PlanEntity p = planes.get(c.getPlanId());
                    if (p != null && p.getPrecio() != null && p.getDuracion() != null && p.getDuracion() > 0) {
                        ingresosMensuales += p.getPrecio() / p.getDuracion();
                    }
                    if (c.getFechaFinMembresia() != null) {
                        long dias = ChronoUnit.DAYS.between(hoy, c.getFechaFinMembresia());
                        if (dias >= 0 && dias <= 7) porVencer++;
                    }
                }
                case PENDIENTE -> pendientes++;
                case SUSPENDIDO -> suspendidos++;
            }
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("gimnasio", MobileJson.gym(gym));
        m.put("total", lista.size());
        m.put("activos", activos);
        m.put("pendientes", pendientes);
        m.put("suspendidos", suspendidos);
        m.put("porVencer", porVencer);
        m.put("ingresosMensualesEstimados", Math.round(ingresosMensuales));
        m.put("planes", planes.size());
        m.put("rutinas", rutinaService.getRutinasByGymId(gym.getId()).size());
        return m;
    }

    /** Mismos datos que el dashboard de la web (ingresos, nuevos, por vencer, tendencia de 6 meses). */
    @GetMapping("/dashboard")
    public Object dashboard() {
        return dashboardService.obtenerDatosDashboard(ctx.gymDelAdmin().getId());
    }

    @PutMapping("/cuenta")
    public Map<String, Object> actualizarCuenta(@RequestBody Map<String, String> b) {
        String correo = requerido(b, "correo").toLowerCase();
        String password = b.getOrDefault("password", "");
        if (!password.isBlank() && password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 6 caracteres");
        }
        adminService.actualizarCorreoYPasswordAdmin(correo, password, ctx.username());
        return Map.of("ok", true);
    }

    // ------------------------------------------------------------------ clientes
    @GetMapping("/clientes")
    public List<Map<String, Object>> clientes() {
        GimnasiosEntity gym = ctx.gymDelAdmin();
        Map<ObjectId, PlanEntity> planes = planesPorId(gym);
        return clienteService.listarClientesPorGym(gym.getId()).stream()
                .map(c -> MobileJson.cliente(c, planes.get(c.getPlanId())))
                .collect(Collectors.toList());
    }

    @PostMapping("/clientes")
    public Map<String, Object> crearCliente(@RequestBody Map<String, String> b) {
        GimnasiosEntity gym = ctx.gymDelAdmin();
        ObjectId planId = MobileContext.oid(requerido(b, "planId"));
        PlanEntity plan = planService.getPlanById(planId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "El plan no existe"));
        ctx.verificarMismoGym(plan.getGymId(), gym);
        String username = requerido(b, "username").toLowerCase();
        String password = requerido(b, "password");
        if (password.length() < 6) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 6 caracteres");
        clienteService.crearCliente(requerido(b, "nombre"), requerido(b, "correo").toLowerCase(), requerido(b, "documento"),
                largo(b.get("telefono"), "Teléfono"), plan.getNombre(), username, password, planId);
        ClientEntity c = clienteService.buscarPorUsername(username);
        return MobileJson.cliente(c, plan);
    }

    /** Cambia el estado: ACTIVO, PENDIENTE o SUSPENDIDO. */
    @PutMapping("/clientes/{id}/estado")
    public Map<String, Object> cambiarEstado(@PathVariable String id, @RequestBody Map<String, String> b) {
        ClientEntity c = clienteDelGym(id);
        ClientEntity.EstadoCliente estado;
        try {
            estado = ClientEntity.EstadoCliente.valueOf(requerido(b, "estado").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado inválido");
        }
        clienteService.cambiarEstadoCliente(c.getId(), estado);
        ClientEntity actualizado = clienteService.buscarClientePorId(c.getId());
        return MobileJson.cliente(actualizado, ctx.planDe(actualizado).orElse(null));
    }

    /** Registra un pago en efectivo/transferencia: activa la membresía desde hoy por la duración del plan. */
    @PostMapping("/clientes/{id}/pago-manual")
    public Map<String, Object> pagoManual(@PathVariable String id) {
        ClientEntity c = clienteDelGym(id);
        if (c.getPlanId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cliente no tiene plan");
        ClientEntity actualizado = pagoMembresia.activar(c, "manual:" + System.currentTimeMillis());
        return MobileJson.cliente(actualizado, ctx.planDe(actualizado).orElse(null));
    }

    @GetMapping("/clientes/{id}/asistencias")
    public List<Map<String, Object>> asistenciasCliente(@PathVariable String id) {
        return MobileJson.asistencias(clienteDelGym(id));
    }

    /**
     * Clientes del gimnasio en el formato que espera la API de Python (FastAPI, POST /predecir/lote).
     * Flutter los toma de aquí y se los envía directamente a la red neuronal.
     */
    @GetMapping("/ml/clientes")
    public List<Map<String, Object>> clientesParaPrediccion() {
        GimnasiosEntity gym = ctx.gymDelAdmin();
        Map<String, PlanEntity> planes = new java.util.HashMap<>();
        for (PlanEntity p : planService.getPlanesByGimnasioId(gym.getId())) planes.put(p.getId().toHexString(), p);
        List<Map<String, Object>> out = new ArrayList<>();
        for (ClientEntity c : clienteService.listarClientesPorGym(gym.getId())) {
            Map<String, Object> m = new LinkedHashMap<>(ml.aJson(c, planes));
            m.put("correo", c.getCorreo());
            m.put("telefono", MobileJson.txt(c.getTelefono()));
            out.add(m);
        }
        return out;
    }

    // ------------------------------------------------------------------ planes
    @GetMapping("/planes")
    public List<Map<String, Object>> planes() {
        GimnasiosEntity gym = ctx.gymDelAdmin();
        List<Map<String, Object>> out = new ArrayList<>();
        for (PlanEntity p : planService.getPlanesByGimnasioId(gym.getId())) {
            Map<String, Object> m = MobileJson.plan(p);
            m.put("suscriptores", clients.countByPlanId(p.getId()));
            out.add(m);
        }
        return out;
    }

    @PostMapping("/planes")
    public Map<String, Object> crearPlan(@RequestBody Map<String, String> b) {
        GimnasiosEntity gym = ctx.gymDelAdmin();
        PlanEntity p = new PlanEntity();
        llenarPlan(p, b);
        p.setGymId(gym.getId());
        return MobileJson.plan(planService.createPlan(p));
    }

    @PutMapping("/planes/{id}")
    public Map<String, Object> editarPlan(@PathVariable String id, @RequestBody Map<String, String> b) {
        PlanEntity actual = planDelGym(id);
        PlanEntity p = new PlanEntity();
        llenarPlan(p, b);
        p.setGymId(actual.getGymId());
        return MobileJson.plan(planService.updatePlan(actual.getId(), p));
    }

    @DeleteMapping("/planes/{id}")
    public Map<String, Object> borrarPlan(@PathVariable String id) {
        PlanEntity p = planDelGym(id);
        planService.deletePlanById(p.getId());
        return Map.of("ok", true);
    }

    // ------------------------------------------------------------------ rutinas
    @GetMapping("/rutinas")
    public List<Map<String, Object>> rutinas() {
        GimnasiosEntity gym = ctx.gymDelAdmin();
        return rutinaService.getRutinasByGymId(gym.getId()).stream().map(MobileJson::rutina).collect(Collectors.toList());
    }

    /** multipart/form-data: nombre, grupoMuscular, repeticiones, series, fotos (0..n archivos), descripciones (0..n). */
    @PostMapping(value = "/rutinas", consumes = "multipart/form-data")
    public Map<String, Object> crearRutina(@RequestParam String nombre, @RequestParam String grupoMuscular,
            @RequestParam String repeticiones, @RequestParam String series,
            @RequestParam(value = "fotos", required = false) List<MultipartFile> fotos,
            @RequestParam(value = "descripciones", required = false) List<String> descripciones) {
        GimnasiosEntity gym = ctx.gymDelAdmin();
        RutinaEntity r = new RutinaEntity();
        r.setNombreEjercicio(nombre.trim());
        r.setGrupoMuscular(grupoMuscular.trim());
        r.setRepeticiones(repeticiones.trim());
        r.setSeries(series.trim());
        r.setGymId(gym.getId());
        r.setCreatedAt(LocalDateTime.now());
        r.setUpdatedAt(LocalDateTime.now());
        return MobileJson.rutina(rutinaService.createRutina(r,
                fotos == null ? List.of() : fotos, descripciones == null ? List.of() : descripciones));
    }

    @PutMapping("/rutinas/{id}")
    public Map<String, Object> editarRutina(@PathVariable String id, @RequestBody Map<String, String> b) {
        RutinaEntity actual = rutinaDelGym(id);
        RutinaEntity cambios = new RutinaEntity();
        cambios.setNombreEjercicio(b.get("nombre"));
        cambios.setGrupoMuscular(b.get("grupoMuscular"));
        cambios.setRepeticiones(b.get("repeticiones"));
        cambios.setSeries(b.get("series"));
        return MobileJson.rutina(rutinaService.updateRutina(actual.getId(), cambios));
    }

    @DeleteMapping("/rutinas/{id}")
    public Map<String, Object> borrarRutina(@PathVariable String id) {
        RutinaEntity r = rutinaDelGym(id);
        rutinaService.deleteRutinaById(r.getId());
        return Map.of("ok", true);
    }

    // ------------------------------------------------------------------ utilidades
    private Map<ObjectId, PlanEntity> planesPorId(GimnasiosEntity gym) {
        return planService.getPlanesByGimnasioId(gym.getId()).stream()
                .collect(Collectors.toMap(PlanEntity::getId, Function.identity(), (a, b) -> a));
    }

    private ClientEntity clienteDelGym(String id) {
        GimnasiosEntity gym = ctx.gymDelAdmin();
        ClientEntity c = clienteService.buscarClientePorId(MobileContext.oid(id));
        ctx.verificarMismoGym(ctx.gymIdDe(c), gym);
        return c;
    }

    private PlanEntity planDelGym(String id) {
        GimnasiosEntity gym = ctx.gymDelAdmin();
        PlanEntity p = planService.getPlanById(MobileContext.oid(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan no encontrado"));
        ctx.verificarMismoGym(p.getGymId(), gym);
        return p;
    }

    private RutinaEntity rutinaDelGym(String id) {
        GimnasiosEntity gym = ctx.gymDelAdmin();
        RutinaEntity r = rutinaService.getRutinaById(MobileContext.oid(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rutina no encontrada"));
        ctx.verificarMismoGym(r.getGymId(), gym);
        return r;
    }

    private static void llenarPlan(PlanEntity p, Map<String, String> b) {
        p.setNombre(requerido(b, "nombre"));
        p.setDescripcion(b.getOrDefault("descripcion", ""));
        double precio;
        try {
            precio = Double.parseDouble(requerido(b, "precio").replace(".", "").replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Precio inválido");
        }
        if (precio <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El precio debe ser mayor que 0");
        p.setPrecio(precio);
        int meses = entero(b.get("duracionMeses"), "Duración");
        if (meses < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La duración mínima es 1 mes");
        p.setDuracion(meses);
    }

    static String requerido(Map<String, String> b, String campo) {
        String v = b.get(campo);
        if (v == null || v.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el campo: " + campo);
        return v.trim();
    }

    static Integer entero(String v, String nombre) {
        try {
            return Integer.valueOf(v == null ? "" : v.trim());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, nombre + " debe ser un número");
        }
    }

    static Long largo(String v, String nombre) {
        try {
            return Long.valueOf(v == null ? "" : v.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, nombre + " debe ser un número");
        }
    }
}
