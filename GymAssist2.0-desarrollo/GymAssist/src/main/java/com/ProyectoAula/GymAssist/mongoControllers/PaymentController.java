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
import com.ProyectoAula.GymAssist.mongoServices.PlanService;

import java.security.Principal;
import java.time.LocalDate;

@Controller
@RequestMapping("/Api/Payment")
public class PaymentController {

    private final ClienteService clienteService;
    private final PlanService planService;
    private final MercadoPagoService mercadoPagoService;

    public PaymentController(ClienteService clienteService, PlanService planService,
            MercadoPagoService mercadoPagoService) {
        this.clienteService = clienteService;
        this.planService = planService;
        this.mercadoPagoService = mercadoPagoService;
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

    @GetMapping("/success")
    public String paymentSuccess(@RequestParam(value = "external_reference", required = false) String externalReference,
            @RequestParam(value = "collection_id", required = false) String collectionId,
            @RequestParam(value = "payment_id", required = false) String paymentId,
            @RequestParam(value = "status", required = false) String status,
            Principal principal,
            Model model) {
        try {
            System.out.println("🔍 Parámetros recibidos:");
            System.out.println("   - external_reference: " + externalReference);
            System.out.println("   - payment_id: " + paymentId);
            System.out.println("   - status: " + status);

            String userId = null;
            ClientEntity cliente = null;

            if (externalReference != null && !externalReference.equals("null")) {
                userId = externalReference;
                ObjectId userObjectId = new ObjectId(userId);
                cliente = clienteService.buscarClientePorId(userObjectId);
                System.out.println("✅ Cliente encontrado por external_reference: " + userId);
            }

            if (cliente == null && principal != null) {
                cliente = clienteService.buscarPorUsername(principal.getName());
                if (cliente != null) {
                    userId = cliente.getId().toString();
                    System.out.println("🔄 Cliente encontrado por Principal: " + principal.getName());
                }
            }

            if (cliente == null) {
                System.err.println("❌ No se pudo identificar al cliente");
                model.addAttribute("error", "No se pudo identificar tu cuenta. Contacta soporte.");
                return "payment-success";
            }

            if (cliente.getEstado() != ClientEntity.EstadoCliente.ACTIVO) {
                cliente.setSubscriptionStatus("ACTIVE");
                cliente.setEstado(ClientEntity.EstadoCliente.ACTIVO);
                cliente.setFechaInicioMembresia(java.time.LocalDate.now());

                PlanEntity plan = planService.getPlanById(cliente.getPlanId())
                        .orElseThrow(() -> new RuntimeException("Plan not found for client"));
                cliente.setFechaFinMembresia(java.time.LocalDate.now().plusMonths(plan.getDuracion()));

                clienteService.actualizarCliente(cliente);
                System.out.println("🎉 Cliente activado: " + cliente.getNombre());
            } else {
                System.out.println("ℹ️ Cliente ya estaba activo: " + cliente.getNombre());
            }

            model.addAttribute("cliente", cliente);
            model.addAttribute("paymentId", paymentId);
            model.addAttribute("clienteId", cliente.getId().toString());

            return "payment-success";

        } catch (Exception e) {
            System.err.println("❌ Error en success: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Error procesando el pago: " + e.getMessage());
            return "payment-success";
        }
    }

    @GetMapping("/failure")
    public String paymentFailure() {
        System.out.println("❌ Pago fallido");
        return "payment-failure";
    }

    @GetMapping("/pending")
    public String paymentPending() {
        System.out.println("⏳ Pago pendiente");
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