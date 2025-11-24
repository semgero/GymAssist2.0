package com.ProyectoAula.GymAssist.mongoModels;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class DashboardDTO {
    private BigDecimal ingresosMesActual;
    private int clientesNuevosEsteMes;
    private int suscripcionesPorVencer;
    private Map<String, Long> distribucionEstados;
    private List<IngresoMensualDTO> tendenciaIngresos;
    private long clientesActivos;
    private BigDecimal ingresosTotales;
    private BigDecimal ingresosSemanales;

    // Constructores, getters y setters
    public DashboardDTO() {
    }

    public DashboardDTO(BigDecimal ingresosMesActual, int clientesNuevosEsteMes,
            long clientesActivos, BigDecimal ingresosTotales, BigDecimal ingresosSemanales,
            int suscripcionesPorVencer, Map<String, Long> distribucionEstados,
            List<IngresoMensualDTO> tendenciaIngresos) {
        this.ingresosMesActual = ingresosMesActual;
        this.clientesNuevosEsteMes = clientesNuevosEsteMes;
        this.clientesActivos = clientesActivos;
        this.ingresosTotales = ingresosTotales;
        this.ingresosSemanales = ingresosSemanales;
        this.suscripcionesPorVencer = suscripcionesPorVencer;
        this.distribucionEstados = distribucionEstados;
        this.tendenciaIngresos = tendenciaIngresos;
    }

    // Getters y Setters
    public BigDecimal getIngresosMesActual() {
        return ingresosMesActual;
    }

    public void setIngresosMesActual(BigDecimal ingresosMesActual) {
        this.ingresosMesActual = ingresosMesActual;
    }

    public int getClientesNuevosEsteMes() {
        return clientesNuevosEsteMes;
    }

    public void setClientesNuevosEsteMes(int clientesNuevosEsteMes) {
        this.clientesNuevosEsteMes = clientesNuevosEsteMes;
    }

    public long getClientesActivos() {
        return clientesActivos;
    }

    public void setClientesActivos(long clientesActivos) {
        this.clientesActivos = clientesActivos;
    }

    public BigDecimal getIngresosTotales() {
        return ingresosTotales;
    }

    public void setIngresosTotales(BigDecimal ingresosTotales) {
        this.ingresosTotales = ingresosTotales;
    }

    public BigDecimal getIngresosSemanales() {
        return ingresosSemanales;
    }

    public void setIngresosSemanales(BigDecimal ingresosSemanales) {
        this.ingresosSemanales = ingresosSemanales;
    }

    public int getSuscripcionesPorVencer() {
        return suscripcionesPorVencer;
    }

    public void setSuscripcionesPorVencer(int suscripcionesPorVencer) {
        this.suscripcionesPorVencer = suscripcionesPorVencer;
    }

    public Map<String, Long> getDistribucionEstados() {
        return distribucionEstados;
    }

    public void setDistribucionEstados(Map<String, Long> distribucionEstados) {
        this.distribucionEstados = distribucionEstados;
    }

    public List<IngresoMensualDTO> getTendenciaIngresos() {
        return tendenciaIngresos;
    }

    public void setTendenciaIngresos(List<IngresoMensualDTO> tendenciaIngresos) {
        this.tendenciaIngresos = tendenciaIngresos;
    }
}