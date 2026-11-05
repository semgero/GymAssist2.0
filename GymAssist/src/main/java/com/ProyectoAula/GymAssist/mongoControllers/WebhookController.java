package com.ProyectoAula.GymAssist.mongoControllers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ProyectoAula.GymAssist.mongoServices.PagoMembresiaService;

/**
 * Notificaciones de Mercado Pago. Acepta los dos formatos que envía:
 *   - Webhook:  POST {"type":"payment","data":{"id":"123"}}   (o ?type=payment&data.id=123)
 *   - IPN:      POST ?topic=payment&id=123
 * Siempre responde 200 para que Mercado Pago no reintente sin fin; el pago se verifica consultándolo.
 * La ruta es pública en SecurityConfig.
 */
@RestController
@RequestMapping("/Api/Payment")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    private final PagoMembresiaService pagos;

    public WebhookController(PagoMembresiaService pagos) {
        this.pagos = pagos;
    }

    @RequestMapping(value = "/webhook", method = { RequestMethod.GET, RequestMethod.POST })
    public ResponseEntity<String> handleWebhook(@RequestParam Map<String, String> query,
            @RequestBody(required = false) Map<String, Object> payload) {
        try {
            String tipo = primero(valor(payload, "type"), valor(payload, "topic"), query.get("type"), query.get("topic"));
            String id = null;
            if (payload != null && payload.get("data") instanceof Map<?, ?> data && data.get("id") != null) {
                id = String.valueOf(data.get("id"));
            }
            id = primero(id, query.get("data.id"), query.get("id"));
            logger.info("📨 Webhook Mercado Pago tipo={} id={}", tipo, id);

            if (tipo != null && tipo.contains("payment") && id != null) {
                var r = pagos.procesarPago(id);
                logger.info("✅ Webhook procesado: pago {} -> {}", id, r.estado());
            }
            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            logger.error("❌ Error procesando webhook: {}", e.getMessage());
            return ResponseEntity.ok("error registrado");
        }
    }

    @GetMapping("/webhook-test")
    public ResponseEntity<String> testWebhook() {
        return ResponseEntity.ok("Webhook endpoint is active");
    }

    private static String valor(Map<String, Object> m, String k) {
        return m == null || m.get(k) == null ? null : String.valueOf(m.get(k));
    }

    private static String primero(String... vs) {
        for (String v : vs) if (v != null && !v.isBlank()) return v;
        return null;
    }
}
