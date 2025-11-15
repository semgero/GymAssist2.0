package com.ProyectoAula.GymAssist.Weka;

import com.ProyectoAula.GymAssist.mongoModels.ClientEntity;

import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Date;

@Service
public class ClienteFeaturesService {

    /**
     * Calcula cuántas asistencias ha tenido el cliente en las últimas 4 semanas.
     */
    public int calcularAsistencias4Semanas(ClientEntity cliente) {
        LocalDate hace4Semanas = LocalDate.now().minusWeeks(4);
        if (cliente.getAsistencias() == null)
            return 0;

        return (int) cliente.getAsistencias().stream()
                .filter(a -> {
                    Object fechaObj = (Object) a.getFecha(); // <- importante: asignar a Object
                    if (fechaObj == null)
                        return false;

                    LocalDate fechaAsistencia = null;

                    if (fechaObj instanceof LocalDate) {
                        fechaAsistencia = (LocalDate) fechaObj;
                    } else if (fechaObj instanceof Date) {
                        fechaAsistencia = ((Date) fechaObj)
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                    } else if (fechaObj instanceof Instant) {
                        fechaAsistencia = ((Instant) fechaObj)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                    } else if (fechaObj instanceof LocalDateTime) {
                        fechaAsistencia = ((LocalDateTime) fechaObj).toLocalDate();
                    } else if (fechaObj instanceof OffsetDateTime) {
                        fechaAsistencia = ((OffsetDateTime) fechaObj).toLocalDate();
                    } else if (fechaObj instanceof String) {
                        // Intentar parsear ISO como "2025-09-03T05:00:00.000+00:00"
                        try {
                            // primero intento OffsetDateTime
                            OffsetDateTime odt = OffsetDateTime.parse((String) fechaObj);
                            fechaAsistencia = odt.toLocalDate();
                        } catch (DateTimeParseException e1) {
                            try {
                                // fallback a LocalDate
                                fechaAsistencia = LocalDate.parse((String) fechaObj);
                            } catch (DateTimeParseException e2) {
                                return false;
                            }
                        }
                    } else {
                        return false;
                    }

                    return fechaAsistencia.isAfter(hace4Semanas);
                })
                .count();
    }

    /**
     * Verifica si el cliente tiene su membresía al día.
     * Si la fecha fin de la membresía es posterior a hoy → "Si", de lo contrario →
     * "No".
     */
    public String calcularPagoAlDia(ClientEntity cliente) {
        LocalDate hoy = LocalDate.now();
        LocalDate fin = cliente.getFechaFinMembresia();

        if (fin == null)
            return "No";
        return fin.isAfter(hoy) ? "Si" : "No";
    }

    /**
     * Calcula la cantidad de meses de antigüedad del cliente.
     */
    public int calcularAntiguedadMeses(ClientEntity cliente) {
        Object ingresoObj = (Object) cliente.getFechaIngresoCliente();
        if (ingresoObj == null)
            return 0;

        LocalDate ingreso;

        if (ingresoObj instanceof LocalDate) {
            ingreso = (LocalDate) ingresoObj;
        } else if (ingresoObj instanceof Date) {
            ingreso = ((Date) ingresoObj)
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } else if (ingresoObj instanceof Instant) {
            ingreso = ((Instant) ingresoObj)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } else if (ingresoObj instanceof OffsetDateTime) {
            ingreso = ((OffsetDateTime) ingresoObj).toLocalDate();
        } else if (ingresoObj instanceof String) {
            try {
                OffsetDateTime odt = OffsetDateTime.parse((String) ingresoObj);
                ingreso = odt.toLocalDate();
            } catch (DateTimeParseException e) {
                try {
                    ingreso = LocalDate.parse((String) ingresoObj);
                } catch (DateTimeParseException ex) {
                    return 0;
                }
            }
        } else {
            return 0;
        }

        return (int) ChronoUnit.MONTHS.between(ingreso, LocalDate.now());
    }

    /**
     * Determina el tipo de plan según el campo "mensualidad".
     * Si contiene "trimestral" → Trimestral, en cualquier otro caso → Mensual.
     */
    public String calcularTipoPlan(ClientEntity cliente) {
        String mensualidad = cliente.getMensualidad();
        if (mensualidad == null)
            return "Mensual";

        mensualidad = mensualidad.toLowerCase();
        if (mensualidad.contains("trim"))
            return "Trimestral";

        return "Mensual";
    }

    /**
     * Devuelve un objeto con todos los atributos ya calculados.
     */
    public ClienteFeaturesDTO generarFeatures(ClientEntity cliente) {
        int asistencias = calcularAsistencias4Semanas(cliente);
        String pago = calcularPagoAlDia(cliente);
        int antiguedad = calcularAntiguedadMeses(cliente);
        String tipoPlan = calcularTipoPlan(cliente);

        return new ClienteFeaturesDTO(asistencias, pago, antiguedad, tipoPlan);
    }
}
