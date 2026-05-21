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
                // Obtener todos los planes del gym
                List<PlanEntity> planes = planService.getPlanesByGimnasioId(gymId);
                List<ObjectId> planIds = planes.stream().map(PlanEntity::getId).toList();
                
                // Obtener todos los clientes del gym a través de los planes
                List<ClientEntity> clientes = planIds.isEmpty() ? new ArrayList<>() : clientRepository.findByPlanIdIn(planIds);

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

                // 6. Nuevas métricas
                long clientesActivos = calcularClientesActivos(clientes);
                BigDecimal ingresosTotales = calcularIngresosTotales(clientes);
                BigDecimal ingresosSemanales = calcularIngresosSemanales(clientes);

                return new DashboardDTO(
                                ingresosMesActual,
                                clientesNuevosEsteMes,
                                clientesActivos,
                                ingresosTotales,
                                ingresosSemanales,
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
                List<IngresoMensualDTO> tendencia = new ArrayList<>();

                // Últimos 6 meses
                for (int i = 5; i >= 0; i--) {
                        YearMonth mes = YearMonth.now().minusMonths(i);
                        BigDecimal ingresos = calcularIngresosPorMes(clientes, mes);
                        tendencia.add(new IngresoMensualDTO(mes.getMonth().toString(), ingresos));
                }

                return tendencia;
        }

        private BigDecimal calcularIngresosPorMes(List<ClientEntity> clientes, YearMonth mes) {
                return clientes.stream()
                                .filter(cliente -> {
                                        LocalDate fechaInicio = cliente.getFechaInicioMembresia();
                                        return fechaInicio != null && YearMonth.from(fechaInicio).equals(mes);
                                })
                                .map(cliente -> planService.getPlanById(cliente.getPlanId())
                                                .map(plan -> BigDecimal.valueOf(plan.getPrecio()))
                                                .orElse(BigDecimal.ZERO))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private long calcularClientesActivos(List<ClientEntity> clientes) {
                return clientes.stream()
                                .filter(c -> c.getEstado() == ClientEntity.EstadoCliente.ACTIVO)
                                .count();
        }

        private BigDecimal calcularIngresosTotales(List<ClientEntity> clientes) {
                return clientes.stream()
                                .map(cliente -> planService.getPlanById(cliente.getPlanId())
                                                .map(plan -> BigDecimal.valueOf(plan.getPrecio()))
                                                .orElse(BigDecimal.ZERO))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private BigDecimal calcularIngresosSemanales(List<ClientEntity> clientes) {
                LocalDate hoy = LocalDate.now();
                LocalDate inicioSemana = hoy.minusDays(hoy.getDayOfWeek().getValue() - 1); // Lunes
                LocalDate finSemana = inicioSemana.plusDays(6); // Domingo

                return clientes.stream()
                                .filter(cliente -> {
                                        LocalDate fechaInicio = cliente.getFechaInicioMembresia();
                                        return fechaInicio != null &&
                                                        !fechaInicio.isBefore(inicioSemana) &&
                                                        !fechaInicio.isAfter(finSemana);
                                })
                                .map(cliente -> planService.getPlanById(cliente.getPlanId())
                                                .map(plan -> BigDecimal.valueOf(plan.getPrecio()))
                                                .orElse(BigDecimal.ZERO))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
}