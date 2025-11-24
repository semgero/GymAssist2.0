package com.ProyectoAula.GymAssist.mongoModels;

import java.math.BigDecimal;

public class IngresoMensualDTO {
    private String mes;
    private BigDecimal ingresos;
    
    public IngresoMensualDTO() {}
    
    public IngresoMensualDTO(String mes, BigDecimal ingresos) {
        this.mes = mes;
        this.ingresos = ingresos;
    }
    
    // Getters y Setters
    public String getMes() { return mes; }
    public void setMes(String mes) { this.mes = mes; }
    
    public BigDecimal getIngresos() { return ingresos; }
    public void setIngresos(BigDecimal ingresos) { this.ingresos = ingresos; }
}