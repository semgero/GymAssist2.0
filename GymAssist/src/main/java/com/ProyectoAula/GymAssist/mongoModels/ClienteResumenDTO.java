package com.ProyectoAula.GymAssist.mongoModels;

public class ClienteResumenDTO {
    
    private long activos;
    private long pendientes;
    private long suspendidos;
    private long total;

    public ClienteResumenDTO(long activos, long pendientes, long suspendidos, long total) {
        this.activos = activos;
        this.pendientes = pendientes;
        this.suspendidos = suspendidos;
        this.total = total;
    }

    public long getActivos() {
        return activos;
    }

    public long getPendientes() {
        return pendientes;
    }

    public long getSuspendidos() {
        return suspendidos;
    }

    public long getTotal() {
        return total;
    }
}  
