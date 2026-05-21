package com.ProyectoAula.GymAssist.mongoServices;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoService {

    private static final Logger logger = LoggerFactory.getLogger(MercadoPagoService.class);

    private final String accessToken;
    private final String publicKey;

    public MercadoPagoService(
            @Value("${mercadopago.access.token}") String accessToken,
            @Value("${mercadopago.public.key}") String publicKey) {

        this.accessToken = accessToken;
        this.publicKey = publicKey;

        try {
            MercadoPagoConfig.setAccessToken(this.accessToken);
            logger.info("MercadoPago configurado correctamente con SDK 2.x");
        } catch (Exception e) {
            logger.error("Error configurando MercadoPago", e);
        }
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String createSubscription(ClientEntity cliente, PlanEntity plan) {
        try {
            logger.info("🔄 Creando suscripción para: {}", cliente.getCorreo());

            PreferenceClient client = new PreferenceClient();

            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .title("Suscripción GymAssist: " + plan.getNombre())
                    .quantity(1)
                    .unitPrice(new BigDecimal(plan.getPrecio()))
                    .currencyId("COP")
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);

            PreferencePayerRequest payerRequest = PreferencePayerRequest.builder()
                    .email(cliente.getCorreo())
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("https://gymassist.co/Api/Payment/success")
                    .failure("https://gymassist.co/Api/Payment/failure")
                    .pending("https://gymassist.co/Api/Payment/pending")
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .payer(payerRequest)
                    .backUrls(backUrls)
                    .notificationUrl("https://gymassist.co/Api/Payment/webhook")
                    .externalReference(cliente.getId().toString()) // ✅ ESTA ES LA CLAVE
                    .metadata(Map.of(
                            "user_id", cliente.getId().toString(),
                            "plan_id", plan.getId().toString(),
                            "email", cliente.getCorreo()))
                    .autoReturn("approved")
                    .build();

            Preference preference = client.create(preferenceRequest);

            logger.info("✅ Preferencia creada exitosamente: {}", preference.getId());
            return preference.getInitPoint();

        } catch (MPApiException apiException) {
            logger.error("❌ Error API MercadoPago: {} - {}",
                    apiException.getApiResponse().getStatusCode(),
                    apiException.getApiResponse().getContent());
            throw new RuntimeException("Error MercadoPago: " + apiException.getApiResponse().getContent());
        } catch (MPException e) {
            logger.error("❌ Error MercadoPago: {}", e.getMessage());
            throw new RuntimeException("Error MercadoPago: " + e.getMessage());
        } catch (Exception e) {
            logger.error("❌ Error inesperado: {}", e.getMessage());
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public boolean verificarConexion() {
        try {
            MercadoPagoConfig.setAccessToken(accessToken);

            PreferenceClient client = new PreferenceClient();
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .title("Test Connection")
                    .quantity(1)
                    .unitPrice(new BigDecimal("100"))
                    .currencyId("COP")
                    .build();

            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(item);

            PreferenceRequest request = PreferenceRequest.builder()
                    .items(items)
                    .build();

            client.create(request);
            return true;
        } catch (Exception e) {
            logger.error("❌ Error verificando conexión: {}", e.getMessage());
            return false;
        }
    }
}