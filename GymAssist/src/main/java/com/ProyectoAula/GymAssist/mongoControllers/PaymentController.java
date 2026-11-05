package com.ProyectoAula.GymAssist.mongoControllers;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;
import com.ProyectoAula.GymAssist.mongoServices.ClienteService;
import com.ProyectoAula.GymAssist.mongoServices.MercadoPagoService;
import com.ProyectoAula.GymAssist.mongoServices.PagoMembresiaService;
import com.ProyectoAula.GymAssist.mongoServices.PlanService;

import java.security.Principal;
import java.time.LocalDate;

@Controller
@RequestMapping("/Api/Payment")
public class PaymentController {

    private final ClienteService clienteService;
    private final PlanService planService;
    private final MercadoPagoService mercadoPagoService;
    private final PagoMembresiaService pagoMembresia;

    public PaymentController(ClienteService clienteService, PlanService planService,
            MercadoPagoService mercadoPagoService, PagoMembresiaService pagoMembresia) {
        this.clienteService = clienteService;
        this.planService = planService;
        this.mercadoPagoService = mercadoPagoService;
        this.pagoMembresia = pagoMembresia;
    }

    /** Solo se permite volver a la app (Flutter) en direcciones conocidas, para evitar redirecciones abiertas. */
    static boolean urlDeAppValida(String url) {
        if (url == null || url.isBlank()) return false;
        String u = url.toLowerCase();
        return u.startsWith("http://localhost") || u.startsWith("http://127.0.0.1") || u.startsWith("gymassist://");
    }

    static String volverALaApp(String app, String estado) {
        String sep = app.contains("?") ? "&" : "?";
        return "redirect:" + app + sep + "pago=" + estado;
    }

    @GetMapping("/checkout")
    public String checkout(Principal principal, Model model) {
        try {
            System.out.println("🔄 Iniciando checkout para: " + principal.getName());

            ClientEntity cliente = clienteService.buscarPorUsername(principal.getName());
            PlanEntity plan = planService.getPlanById(cliente.getPlanId())
                    .orElseThrow(() -> new RuntimeException("Plan not found for client"));
            System.out.println("✅ Plan encontrado: " + plan.getNombre() + " - $" +
                    plan.getPrecio());

            String initPoint = mercadoPagoService.createSubscription(cliente, plan);
            String publicKey = mercadoPagoService.getPublicKey();

            System.out.println("🔑 Public Key completa: " + publicKey);

            if (publicKey == null || publicKey.length() < 20) {
                throw new RuntimeException("Public Key incompleta: " + publicKey);
            }

            model.addAttribute("initPoint", initPoint);
            model.addAttribute("plan", plan);
            model.addAttribute("cliente", cliente);
            model.addAttribute("publicKey", publicKey);

            System.out.println("✅ Checkout listo - Preference: " + initPoint);
            return "checkout";

        } catch (Exception e) {
            System.err.println("❌ Error en checkout: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "error-payment";
        }
    }

    /**
     * Mercado Pago redirige aquí al terminar. Antes activaba al cliente sin verificar nada; ahora consulta
     * el pago en Mercado Pago y solo activa si está aprobado. Si el pago se hizo desde Flutter (parámetro
     * "app"), redirige de vuelta a la app.
     */
    @GetMapping("/success")
    public String paymentSuccess(@RequestParam(value = "external_reference", required = false) String externalReference,
            @RequestParam(value = "collection_id", required = false) String collectionId,
            @RequestParam(value = "payment_id", required = false) String paymentId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "app", required = false) String app,
            Principal principal,
            Model model) {
        try {
            String id = (paymentId != null && !paymentId.equals("null")) ? paymentId : collectionId;
            PagoMembresiaService.Resultado r = pagoMembresia.procesarPago(id);
            ClientEntity cliente = r.cliente();
            if (cliente == null && externalReference != null && ObjectId.isValid(externalReference)) {
                cliente = clienteService.buscarClientePorId(new ObjectId(externalReference));
            }
            if (cliente == null && principal != null) {
                cliente = clienteService.buscarPorUsername(principal.getName());
            }
            if (urlDeAppValida(app)) {
                return volverALaApp(app, r.aprobado() ? "aprobado" : (r.estado() == null ? "desconocido" : r.estado()));
            }
            if (cliente == null) {
                model.addAttribute("error", "No se pudo identificar tu cuenta. Contacta soporte.");
                return "payment-success";
            }
            if (!r.aprobado()) {
                model.addAttribute("error", "Mercado Pago aún no confirma el pago (estado: " + r.estado() + "). Se activará apenas llegue la confirmación.");
            }
            model.addAttribute("cliente", cliente);
            model.addAttribute("paymentId", id);
            model.addAttribute("clienteId", cliente.getId().toString());
            return "payment-success";
        } catch (Exception e) {
            System.err.println("❌ Error en success: " + e.getMessage());
            if (urlDeAppValida(app)) return volverALaApp(app, "error");
            model.addAttribute("error", "Error procesando el pago: " + e.getMessage());
            return "payment-success";
        }
    }

    @GetMapping("/failure")
    public String paymentFailure(@RequestParam(value = "app", required = false) String app) {
        System.out.println("❌ Pago fallido");
        if (urlDeAppValida(app)) return volverALaApp(app, "rechazado");
        return "payment-failure";
    }

    @GetMapping("/pending")
    public String paymentPending(@RequestParam(value = "app", required = false) String app) {
        System.out.println("⏳ Pago pendiente");
        if (urlDeAppValida(app)) return volverALaApp(app, "pendiente");
        return "payment-pending";
    }

    @GetMapping("/test-expiracion")
    @ResponseBody
    public String testExpiracion() {
        try {
            ClientEntity cliente = clienteService.buscarPorUsername("test3");

            if (cliente != null) {
                cliente.setFechaFinMembresia(LocalDate.now().minusDays(1));
                clienteService.actualizarCliente(cliente);

                String resultado = clienteService.verificarExpiracionesManualmente();

                return "<h3>Prueba de Expiración</h3>" +
                        "<p>Cliente de prueba: " + cliente.getNombre() + "</p>" +
                        "<p>Fecha fin establecida: " + LocalDate.now().minusDays(1) + "</p>" +
                        "<p>Resultado: " + resultado + "</p>" +
                        "<p><a href='/Api/Cliente/lista'>Ver lista de clientes</a></p>";
            } else {
                return "❌ Cliente de prueba no encontrado";
            }
        } catch (Exception e) {
            return "❌ Error en prueba: " + e.getMessage();
        }
    }

    @GetMapping("/probar-expiracion")
    @ResponseBody
    public String probarExpiracionSimple(Principal principal) {
        try {
            if (principal == null) {
                return "❌ Inicia sesión primero";
            }

            ClientEntity cliente = clienteService.buscarPorUsername(principal.getName());

            clienteService.verificarExpiracionesManualmente();

            ClientEntity clienteActualizado = clienteService.buscarPorUsername(principal.getName());

            return "<h3>Resultado Verificación</h3>" +
                    "<p><strong>Cliente:</strong> " + cliente.getNombre() + "</p>" +
                    "<p><strong>Estado actual:</strong> " + clienteActualizado.getEstado() + "</p>" +
                    "<p><strong>Fecha fin:</strong> " + clienteActualizado.getFechaFinMembresia() + "</p>" +
                    "<br><p><em>El sistema verifica automáticamente cada día a las 6:00 AM</em></p>";

        } catch (Exception e) {
            return "❌ Error: " + e.getMessage();
        }
    }
}