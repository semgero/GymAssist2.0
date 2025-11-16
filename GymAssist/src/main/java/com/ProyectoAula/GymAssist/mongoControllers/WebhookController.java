package com.ProyectoAula.GymAssist.mongoControllers;

import java.util.Map;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;

import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;
import com.ProyectoAula.GymAssist.mongoServices.ClienteService;
import com.ProyectoAula.GymAssist.mongoServices.PlanService;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;

@RestController
@RequestMapping("/Api/Payment")
public class WebhookController {

    private final ClienteService clienteService;
    private final PlanService planService;
    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    private static final String WEBHOOK_SECRET = "5ed13a61fa1b5d04294ca5b73d33716f84c8d43e25d8eeb35226f151cc86577e";
    private static final String AUTH_HEADER = "X-Secret-Token";

    public WebhookController(ClienteService clienteService, PlanService planService) {
        this.clienteService = clienteService;
        this.planService = planService;
    }

    @RequestMapping(value = "/webhook", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = AUTH_HEADER, required = false) String authHeader,
            @RequestBody(required = false) Map<String, Object> payload) {

        try {
            if (!isAuthenticated(authHeader)) {
                logger.warn("❌ Intento de acceso no autorizado al webhook - Token: {}", authHeader);
            }

            if (payload == null || payload.isEmpty()) {
                logger.info("✅ Webhook funcionando correctamente - Prueba GET de Mercado Pago");
                return ResponseEntity.ok("Webhook is ready to receive notifications");
            }

            logger.info("📨 Webhook recibido - Tipo: {}", payload.get("type"));

            String type = (String) payload.get("type");

            if ("payment".equals(type)) {
                Map<String, Object> data = (Map<String, Object>) payload.get("data");
                String paymentId = (String) data.get("id");

                logger.info("💳 Procesando pago: {}", paymentId);

                Payment payment = getPaymentDetails(paymentId);

                logger.info("📊 Estado del pago {}: {}", paymentId, payment.getStatus());
                if ("approved".equals(payment.getStatus())) {
                    String userId = payment.getExternalReference();
                    if (userId == null || userId.isEmpty()) {
                        Map<String, Object> metadata = payment.getMetadata();
                        if (metadata != null) {
                            userId = (String) metadata.get("user_id");
                            logger.info("🔄 Usando user_id de metadata: {}", userId);
                        }
                    } else {
                        logger.info("✅ Usando external_reference: {}", userId);
                    }
                    if (userId != null && !userId.isEmpty()) {
                        activarCliente(userId);
                        logger.info("✅ Cliente activado vía webhook: {}", userId);
                    } else {
                        logger.warn("⚠️ user_id no encontrado en external_reference ni metadata del pago {}",
                                paymentId);
                    }
                } else {
                    logger.info("⏳ Pago {} no aprobado, estado: {}", paymentId, payment.getStatus());
                }
            }
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            logger.error("❌ Error procesando webhook: {}", e.getMessage());
            return ResponseEntity.status(500).body("Error processing webhook");
        }
    }

    private boolean isAuthenticated(String authHeader) {
        logger.info("🔓 Autenticación desactivada temporalmente para pruebas");
        return true;
    }

    private Payment getPaymentDetails(String paymentId) {
        try {
            PaymentClient client = new PaymentClient();
            Long paymentIdLong;
            try {
                paymentIdLong = Long.parseLong(paymentId);
            } catch (NumberFormatException e) {
                logger.error("❌ ID de pago inválido: {}", paymentId);
                throw new RuntimeException("ID de pago inválido: " + paymentId);
            }
            Payment payment = client.get(paymentIdLong);
            if (payment == null) {
                throw new RuntimeException("No se pudo obtener información del pago " + paymentId);
            }

            return payment;

        } catch (Exception e) {
            logger.error("❌ Error obteniendo detalles del pago {}: {}", paymentId, e.getMessage());
            throw new RuntimeException("Error obteniendo detalles del pago: " + e.getMessage());
        }
    }

    private void activarCliente(String userId) {
        try {
            ObjectId userObjectId = new ObjectId(userId);

            ClientEntity cliente = clienteService.buscarClientePorId(userObjectId);

            if (cliente != null) {
                if (cliente.getEstado() != ClientEntity.EstadoCliente.ACTIVO) {
                    cliente.setSubscriptionStatus("ACTIVE");
                    cliente.setEstado(ClientEntity.EstadoCliente.ACTIVO);
                    cliente.setFechaInicioMembresia(java.time.LocalDate.now());

                    PlanEntity plan = planService.getPlanById(cliente.getPlanId())
                            .orElseThrow(() -> new RuntimeException("Plan not found for client: " + userId));
                    cliente.setFechaFinMembresia(java.time.LocalDate.now().plusMonths(plan.getDuracion()));

                    clienteService.actualizarCliente(cliente);
                    logger.info("🎉 Cliente activado automáticamente: {} ({})", cliente.getNombre(), userId);
                } else {
                    logger.info("ℹ️ Cliente ya estaba activo: {}", userId);
                }
            } else {
                logger.error("❌ Cliente no encontrado con ID: {}", userId);
            }
        } catch (Exception e) {
            logger.error("❌ Error activando cliente {}: {}", userId, e.getMessage());
        }
    }

    @GetMapping("/webhook-test")
    public ResponseEntity<String> testWebhook() {
        return ResponseEntity.ok("Webhook endpoint is active. Use POST method with X-Secret-Token header");
    }
}