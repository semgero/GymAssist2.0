package com.ProyectoAula.GymAssist.mongoServices;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Map;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;

/**
 * Activa membresías a partir de pagos de Mercado Pago. Lo usan el webhook, la página de regreso
 * (/Api/Payment/success) y la app Flutter. SIEMPRE consulta el pago en Mercado Pago antes de activar
 * (no se confía en los parámetros de la URL) y no procesa dos veces el mismo pago.
 */
@Service
public class PagoMembresiaService {

    public static final ZoneId ZONA = ZoneId.of("America/Bogota");
    private static final Logger log = LoggerFactory.getLogger(PagoMembresiaService.class);

    private final ClienteService clienteService;
    private final PlanService planService;

    public PagoMembresiaService(ClienteService clienteService, PlanService planService) {
        this.clienteService = clienteService;
        this.planService = planService;
    }

    /** Resultado de procesar un pago: estado en Mercado Pago y el cliente (si se identificó). */
    public record Resultado(String estado, ClientEntity cliente) {
        public boolean aprobado() {
            return "approved".equals(estado);
        }
    }

    public Resultado procesarPago(String paymentId) {
        if (paymentId == null || paymentId.isBlank() || "null".equals(paymentId)) return new Resultado("unknown", null);
        Payment pago;
        try {
            pago = new PaymentClient().get(Long.parseLong(paymentId.trim()));
        } catch (Exception e) {
            log.error("❌ No se pudo consultar el pago {} en Mercado Pago: {}", paymentId, e.getMessage());
            return new Resultado("error", null);
        }
        String estado = pago.getStatus();
        String clienteId = pago.getExternalReference();
        if (clienteId == null || clienteId.isBlank()) {
            Map<String, Object> meta = pago.getMetadata();
            if (meta != null && meta.get("user_id") != null) clienteId = String.valueOf(meta.get("user_id"));
        }
        ClientEntity cliente = null;
        try {
            if (clienteId != null && ObjectId.isValid(clienteId)) cliente = clienteService.buscarClientePorId(new ObjectId(clienteId));
        } catch (Exception e) {
            log.warn("⚠️ Cliente {} del pago {} no encontrado", clienteId, paymentId);
        }
        log.info("💳 Pago {} estado={} cliente={}", paymentId, estado, clienteId);
        if ("approved".equals(estado) && cliente != null) {
            cliente = activar(cliente, "pago:" + paymentId);
        }
        return new Resultado(estado, cliente);
    }

    /**
     * Activa (o renueva) la membresía por la duración del plan. Si ya estaba activa y vigente,
     * suma la duración a la fecha de fin. `referencia` evita procesar dos veces el mismo pago.
     */
    public ClientEntity activar(ClientEntity c, String referencia) {
        if (referencia != null && referencia.equals(c.getMercadoPagoPreferenceId())) {
            log.info("ℹ️ El pago {} ya se había aplicado al cliente {}", referencia, c.getId());
            return c;
        }
        PlanEntity plan = c.getPlanId() == null ? null : planService.getPlanById(c.getPlanId()).orElse(null);
        int meses = (plan == null || plan.getDuracion() == null || plan.getDuracion() < 1) ? 1 : plan.getDuracion();
        LocalDate hoy = LocalDate.now(ZONA);
        boolean vigente = c.getEstado() == ClientEntity.EstadoCliente.ACTIVO
                && c.getFechaFinMembresia() != null && c.getFechaFinMembresia().isAfter(hoy);
        LocalDate base = vigente ? c.getFechaFinMembresia() : hoy;
        if (!vigente) c.setFechaInicioMembresia(hoy);
        c.setFechaFinMembresia(base.plusMonths(meses));
        c.setEstado(ClientEntity.EstadoCliente.ACTIVO);
        c.setSubscriptionStatus("ACTIVE");
        if (referencia != null) c.setMercadoPagoPreferenceId(referencia);
        clienteService.actualizarCliente(c);
        log.info("🎉 Membresía activa: {} hasta {}", c.getNombre(), c.getFechaFinMembresia());
        return c;
    }
}
