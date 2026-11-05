package com.ProyectoAula.GymAssist.mobile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.MedicionesEntity;
import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;
import com.ProyectoAula.GymAssist.mongoServices.ClienteService;
import com.ProyectoAula.GymAssist.mongoServices.GimnasiosServices;
import com.ProyectoAula.GymAssist.mongoServices.MedicionesService;
import com.ProyectoAula.GymAssist.mongoServices.MercadoPagoService;
import com.ProyectoAula.GymAssist.mongoServices.PlanService;
import com.ProyectoAula.GymAssist.mongoServices.RutinaService;

/** API del cliente para la app Flutter (requiere token con rol CLIENTE). */
@RestController
@RequestMapping("/api/m/cliente")
public class MobileClienteController {

    private final MobileContext ctx;
    private final ClienteService clienteService;
    private final PlanService planService;
    private final RutinaService rutinaService;
    private final MedicionesService medicionesService;
    private final MercadoPagoService mercadoPago;
    private final GimnasiosServices gymService;
    private final JwtService jwt;

    public MobileClienteController(MobileContext ctx, ClienteService clienteService, PlanService planService,
            RutinaService rutinaService, MedicionesService medicionesService, MercadoPagoService mercadoPago,
            GimnasiosServices gymService, JwtService jwt) {
        this.ctx = ctx;
        this.clienteService = clienteService;
        this.planService = planService;
        this.rutinaService = rutinaService;
        this.medicionesService = medicionesService;
        this.mercadoPago = mercadoPago;
        this.gymService = gymService;
        this.jwt = jwt;
    }

    @GetMapping("/perfil")
    public Map<String, Object> perfil() {
        ClientEntity c = ctx.cliente();
        Map<String, Object> m = new LinkedHashMap<>(MobileJson.cliente(c, ctx.planDe(c).orElse(null)));
        ObjectId gymId = ctx.gymIdDe(c);
        m.put("gimnasio", gymId == null ? null : gymService.getGymById(gymId).map(MobileJson::gym).orElse(null));
        medicionesService.obtenerUltimaMedicion(c.getId()).ifPresent(me -> m.put("ultimaMedicion", medicion(me)));
        return m;
    }

    @PutMapping("/cuenta")
    public Map<String, Object> actualizarCuenta(@RequestBody Map<String, String> b) {
        ClientEntity c = ctx.cliente();
        String correo = b.getOrDefault("correo", c.getCorreo()).trim().toLowerCase();
        String username = b.getOrDefault("username", c.getUsername()).trim().toLowerCase();
        String password = b.getOrDefault("password", "");
        if (!password.isBlank() && password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 6 caracteres");
        }
        clienteService.actualizarDatosPersonales(correo, username, password, c.getUsername());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);
        m.put("token", jwt.generar(username, "CLIENTE")); // si cambió el usuario, el token viejo ya no sirve
        return m;
    }

    // ------------------------------------------------------------------ planes y pago
    @GetMapping("/planes")
    public List<Map<String, Object>> planes() {
        ObjectId gymId = ctx.gymIdDe(ctx.cliente());
        if (gymId == null) return List.of();
        return planService.getPlanesByGimnasioId(gymId).stream().map(MobileJson::plan).collect(Collectors.toList());
    }

    @PutMapping("/plan")
    public Map<String, Object> cambiarPlan(@RequestBody Map<String, String> b) {
        ClientEntity c = ctx.cliente();
        PlanEntity nuevo = planService.getPlanById(MobileContext.oid(MobileAdminController.requerido(b, "planId")))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan no encontrado"));
        ObjectId gymActual = ctx.gymIdDe(c);
        if (gymActual != null && !gymActual.equals(nuevo.getGymId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ese plan es de otro gimnasio");
        }
        c.setPlanId(nuevo.getId());
        c.setMensualidad(nuevo.getNombre());
        clienteService.actualizarCliente(c);
        return MobileJson.cliente(c, nuevo);
    }

    /** Crea el cobro en Mercado Pago y devuelve el link (initPoint) que Flutter abre en el navegador. */
    @PostMapping("/pago")
    public Map<String, Object> pagar() {
        ClientEntity c = ctx.cliente();
        PlanEntity plan = ctx.planDe(c)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Primero elige un plan"));
        String initPoint = mercadoPago.createSubscription(c, plan);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("initPoint", initPoint);
        m.put("plan", MobileJson.plan(plan));
        return m;
    }

    // ------------------------------------------------------------------ rutinas
    @GetMapping("/rutinas")
    public List<Map<String, Object>> rutinas(@RequestParam(required = false) String grupo) {
        ClientEntity c = activo();
        ObjectId gymId = ctx.gymIdDe(c);
        if (gymId == null) return List.of();
        return rutinaService.getRutinasByGymId(gymId).stream()
                .filter(r -> grupo == null || grupo.isBlank() || grupo.equalsIgnoreCase(r.getGrupoMuscular()))
                .map(MobileJson::rutina)
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------ asistencia
    @GetMapping("/asistencias")
    public Map<String, Object> asistencias() {
        ClientEntity c = ctx.cliente();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("inasistencias", c.getInasistencias());
        m.put("asistencias", MobileJson.asistencias(c));
        m.put("hoyRegistrada", c.getAsistencias() != null
                && c.getAsistencias().stream().anyMatch(a -> LocalDate.now().equals(a.getFecha())));
        return m;
    }

    @PostMapping("/asistencias")
    public Map<String, Object> registrarAsistencia(@RequestBody Map<String, Object> b) {
        ClientEntity c = activo();
        List<String> musculos = new ArrayList<>();
        if (b.get("musculos") instanceof List<?> l) {
            for (Object o : l) if (o != null && !o.toString().isBlank()) musculos.add(o.toString());
        }
        if (musculos.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona al menos un músculo trabajado");
        }
        LocalDate hoy = LocalDate.now();
        if (c.getAsistencias() != null && c.getAsistencias().stream().anyMatch(a -> hoy.equals(a.getFecha()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya registraste tu asistencia de hoy");
        }
        clienteService.registrarAsistencia(c.getId(), hoy, musculos);
        return asistencias();
    }

    // ------------------------------------------------------------------ mediciones (IMC)
    @GetMapping("/mediciones")
    public List<Map<String, Object>> mediciones() {
        ClientEntity c = ctx.cliente();
        return medicionesService.obtenerHistorial(c.getId()).stream().map(this::medicion).collect(Collectors.toList());
    }

    /** {peso: kg, estatura: metros (si llega en cm, se convierte)} */
    @PostMapping("/mediciones")
    public Map<String, Object> guardarMedicion(@RequestBody Map<String, Object> b) {
        ClientEntity c = ctx.cliente();
        double peso = numero(b.get("peso"), "Peso");
        double estatura = numero(b.get("estatura"), "Estatura");
        if (estatura > 3) estatura = estatura / 100.0;
        if (peso < 20 || peso > 400) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El peso debe estar entre 20 y 400 kg");
        if (estatura < 0.5 || estatura > 2.5) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La estatura debe estar entre 0,5 y 2,5 m");
        return medicion(medicionesService.guardarMedicion(peso, estatura, c.getId()));
    }

    // ------------------------------------------------------------------ utilidades
    private Map<String, Object> medicion(MedicionesEntity me) {
        double imc = medicionesService.calcularIMC(me.getPeso(), me.getEstatura());
        return MobileJson.medicion(me, imc, medicionesService.interpretarIMC(imc));
    }

    /** Las rutinas y la asistencia solo están disponibles con la membresía activa (igual que en la web). */
    private ClientEntity activo() {
        ClientEntity c = ctx.cliente();
        if (c.getEstado() != ClientEntity.EstadoCliente.ACTIVO) {
            throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Tu membresía no está activa. Realiza el pago para continuar.");
        }
        return c;
    }

    private static double numero(Object v, String nombre) {
        try {
            return Double.parseDouble(String.valueOf(v).replace(",", "."));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, nombre + " debe ser un número");
        }
    }
}
