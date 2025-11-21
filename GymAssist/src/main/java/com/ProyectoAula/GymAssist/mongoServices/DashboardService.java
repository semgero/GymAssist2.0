package com.ProyectoAula.GymAssist.mongoServices;

import com.ProyectoAula.GymAssist.mongoModels.*;
import com.ProyectoAula.GymAssist.mongoRepository.ClientRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PlanService planService;

    public DashboardDTO obtenerDatosDashboard(ObjectId gymId) {
        // Obtener todos los clientes del gym
        List<ClientEntity> clientes = clientRepository.findByGymId(gymId);

        // 1. Ingresos del mes actual
        BigDecimal ingresosMesActual = calcularIngresosMesActual(clientes);

        // 2. Clientes nuevos este mes
        int clientesNuevosEsteMes = calcularClientesNuevosEsteMes(clientes);

        // 3. Suscripciones por vencer (próximos 7 días)
        int suscripcionesPorVencer = calcularSuscripcionesPorVencer(clientes);

        // 4. Distribución de estados
        Map<String, Long> distribucionEstados = calcularDistribucionEstados(clientes);

        // 5. Tendencia de ingresos (últimos 6 meses)
        List<IngresoMensualDTO> tendenciaIngresos = calcularTendenciaIngresos(clientes);

        return new DashboardDTO(
                ingresosMesActual,
                clientesNuevosEsteMes,
                suscripcionesPorVencer,
                distribucionEstados,
                tendenciaIngresos);
    }

    private BigDecimal calcularIngresosMesActual(List<ClientEntity> clientes) {
        YearMonth mesActual = YearMonth.now();

        return clientes.stream()
                .filter(cliente -> cliente.getEstado() == ClientEntity.EstadoCliente.ACTIVO)
                .filter(cliente -> {
                    LocalDate fechaInicio = cliente.getFechaInicioMembresia();
                    return fechaInicio != null &&
                            YearMonth.from(fechaInicio).equals(mesActual);
                })
                .map(cliente -> planService.getPlanById(cliente.getPlanId())
                        .map(plan -> BigDecimal.valueOf(plan.getPrecio()))
                        .orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int calcularClientesNuevosEsteMes(List<ClientEntity> clientes) {
        YearMonth mesActual = YearMonth.now();

        return (int) clientes.stream()
                .filter(cliente -> {
                    LocalDate fechaIngreso = cliente.getFechaIngresoCliente();
                    return fechaIngreso != null &&
                            YearMonth.from(fechaIngreso).equals(mesActual);
                })
                .count();
    }

    // Agrega validación para fechas null en este método también:
    private int calcularSuscripcionesPorVencer(List<ClientEntity> clientes) {
        LocalDate hoy = LocalDate.now();
        LocalDate en7Dias = hoy.plusDays(7);

        return (int) clientes.stream()
                .filter(cliente -> cliente.getEstado() == ClientEntity.EstadoCliente.ACTIVO)
                .filter(cliente -> {
                    LocalDate fechaFin = cliente.getFechaFinMembresia();
                    return fechaFin != null &&
                            !fechaFin.isBefore(hoy) &&
                            !fechaFin.isAfter(en7Dias);
                })
                .count();
    }

    private Map<String, Long> calcularDistribucionEstados(List<ClientEntity> clientes) {
        return clientes.stream()
                .collect(Collectors.groupingBy(
                        cliente -> cliente.getEstado().name(),
                        Collectors.counting()));
    }

    private List<IngresoMensualDTO> calcularTendenciaIngresos(List<ClientEntity> clientes) {
        // Simulación - en producción esto vendría de una tabla de pagos
        List<IngresoMensualDTO> tendencia = new ArrayList<>();

        // Últimos 6 meses
        for (int i = 5; i >= 0; i--) {
            YearMonth mes = YearMonth.now().minusMonths(i);
            BigDecimal ingresos = simularIngresosMensuales(clientes, mes);
            tendencia.add(new IngresoMensualDTO(mes.getMonth().toString(), ingresos));
        }

        return tendencia;
    }

    private BigDecimal simularIngresosMensuales(List<ClientEntity> clientes, YearMonth mes) {
        // Esto es una simulación - en producción usarías datos reales de pagos
        Random random = new Random(mes.hashCode());
        return BigDecimal.valueOf(random.nextInt(10000000) + 5000000); // Entre 5M y 15M
    }
}