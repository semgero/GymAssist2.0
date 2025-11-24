package com.ProyectoAula.GymAssist.mongoModels;

public class ClienteResumenDTO {
    
    private long activos;
    private long suspendidos;
    private long total;

    public ClienteResumenDTO(long activos, long suspendidos, long total) {
        this.activos = activos;
        this.suspendidos = suspendidos;
        this.total = total;
    }

    public long getActivos() {
        return activos;
    }

    public long getSuspendidos() {
        return suspendidos;
    }

    public long getTotal() {
        return total;
    }
}  
