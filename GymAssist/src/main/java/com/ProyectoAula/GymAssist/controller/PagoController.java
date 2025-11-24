/*package com.ProyectoAula.GymAssist.controller;

import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;
import com.ProyectoAula.GymAssist.mongoModels.PlanEntity;
import com.ProyectoAula.GymAssist.mongoRepository.PlanRepository;
import com.ProyectoAula.GymAssist.mongoRepository.ClientRepository;
import com.mercadopago.MercadoPago;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.Preference;
import com.mercadopago.resources.datastructures.preference.BackUrls;
import com.mercadopago.resources.datastructures.preference.Item;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    private final PlanRepository planRepository;
    private final ClientRepository clientRepository;

    @Value("${mercadopago.access-token}")
    private String accessToken;

    public PagoController(PlanRepository planRepository, ClientRepository clientRepository) {
        this.planRepository = planRepository;
        this.clientRepository = clientRepository;
    }

    @GetMapping("/pago")
    public String mostrarVistaPago() {
        return "pago";
    }

    @GetMapping("/crear-preferencia/{clienteId}")
    @ResponseBody
    public String crearPreferenciaConCliente(@PathVariable String clienteId) throws MPException {
        MercadoPago.SDK.setAccessToken(accessToken);

        // Convertir String a ObjectId
        ObjectId clientObjectId;
        try {
            clientObjectId = new ObjectId(clienteId);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("ID de cliente inválido: " + clienteId);
        }

        // Buscar el cliente
        ClientEntity cliente = clientRepository.findById(clientObjectId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + clienteId));

        // Verificar que el cliente tenga un planId
        ObjectId planId = cliente.getPlanId();
        if (planId == null) {
            throw new RuntimeException("El cliente no tiene un plan asignado");
        }

        // Buscar el plan usando el planId del cliente
        PlanEntity plan = planRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado con ID: " + planId));

        // Crear el item para Mercado Pago
        Item item = new Item()
                .setTitle("Plan: " + plan.getNombre())
                .setDescription(plan.getDescripcion())
                .setQuantity(1)
                .setCurrencyId("COP")
                .setUnitPrice(plan.getPrecio().floatValue());

        // Crear y configurar la preferencia
        Preference preference = new Preference();
        preference.appendItem(item);
        preference.setBackUrls(new BackUrls()
                .setSuccess("http://localhost:8080/pagos/exito")
                .setFailure("http://localhost:8080/pagos/error")
                .setPending("http://localhost:8080/pagos/pendiente"));
        preference.setNotificationUrl("http://localhost:8080/pagos/notificacion");
        
        // Guardar metadata para identificar el pago después
        preference.setExternalReference(clienteId);

        preference.save();
        return preference.getInitPoint();
    }

    // Endpoint alternativo: crear preferencia usando planId directamente
    @GetMapping("/crear-preferencia-por-plan/{planId}")
    @ResponseBody
    public String crearPreferenciaPorPlan(@PathVariable String planId) throws MPException {
        MercadoPago.SDK.setAccessToken(accessToken);

        // Convertir String a ObjectId
        ObjectId planObjectId;
        try {
            planObjectId = new ObjectId(planId);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("ID de plan inválido: " + planId);
        }

        // Buscar el plan
        PlanEntity plan = planRepository.findById(planObjectId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado con ID: " + planId));

        // Crear el item
        Item item = new Item()
                .setTitle("Plan: " + plan.getNombre())
                .setDescription(plan.getDescripcion())
                .setQuantity(1)
                .setCurrencyId("COP")
                .setUnitPrice(plan.getPrecio().floatValue());

        // Crear preferencia
        Preference preference = new Preference();
        preference.appendItem(item);
        preference.setBackUrls(new BackUrls()
                .setSuccess("http://localhost:8080/pagos/exito")
                .setFailure("http://localhost:8080/pagos/error")
                .setPending("http://localhost:8080/pagos/pendiente"));
        preference.setNotificationUrl("http://localhost:8080/pagos/notificacion");
        preference.setExternalReference("plan-" + planId);

        preference.save();
        return preference.getInitPoint();
    }

    // Endpoint para pago directo (sin BD)
    @GetMapping("/crear-preferencia-directa")
    @ResponseBody
    public String crearPreferenciaDirecta(
            @RequestParam String nombrePlan,
            @RequestParam Double precio,
            @RequestParam(required = false) String descripcion) throws MPException {
        
        MercadoPago.SDK.setAccessToken(accessToken);

        Item item = new Item()
                .setTitle("Plan: " + nombrePlan)
                .setDescription(descripcion != null ? descripcion : "Plan de gimnasio")
                .setQuantity(1)
                .setCurrencyId("COP")
                .setUnitPrice(precio.floatValue());

        Preference preference = new Preference();
        preference.appendItem(item);
        preference.setBackUrls(new BackUrls()
                .setSuccess("http://localhost:8080/pagos/exito")
                .setFailure("http://localhost:8080/pagos/error")
                .setPending("http://localhost:8080/pagos/pendiente"));
        preference.setNotificationUrl("http://localhost:8080/pagos/notificacion");

        preference.save();
        return preference.getInitPoint();
    }

    @GetMapping("/exito")
    public String pagoExitoso() {
        return "pago-exito"; // Retorna pago-exito.html
    }

    @GetMapping("/error")
    public String pagoError() {
        return "pago-error"; // Retorna pago-error.html
    }

    @GetMapping("/pendiente")
    public String pagoPendiente() {
        return "pago-pendiente"; // Retorna pago-pendiente.html
    }

    @PostMapping("/notificacion")
    @ResponseBody
    public String recibirNotificacion(@RequestBody String body) {
        System.out.println("📩 Notificación de MP: " + body);
        // Aquí puedes procesar la notificación y actualizar el estado del cliente
        return "OK";
    }
}*/