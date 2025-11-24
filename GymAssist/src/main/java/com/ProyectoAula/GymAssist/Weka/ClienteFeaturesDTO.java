package com.ProyectoAula.GymAssist.Weka;

public class ClienteFeaturesDTO {
    private int asistencias4Semanas;
    private String pagoAlDia;
    private int antiguedadMeses;
    private String tipoPlan;

    public ClienteFeaturesDTO(int asistencias4Semanas, String pagoAlDia, int antiguedadMeses, String tipoPlan) {
        this.asistencias4Semanas = asistencias4Semanas;
        this.pagoAlDia = pagoAlDia;
        this.antiguedadMeses = antiguedadMeses;
        this.tipoPlan = tipoPlan;
    }

    public int getAsistencias4Semanas() {
        return asistencias4Semanas;
    }

    public String getPagoAlDia() {
        return pagoAlDia;
    }

    public int getAntiguedadMeses() {
        return antiguedadMeses;
    }

    public String getTipoPlan() {
        return tipoPlan;
    }

    @Override
    public String toString() {
        return "ClienteFeaturesDTO{" +
                "asistencias4Semanas=" + asistencias4Semanas +
                ", pagoAlDia='" + pagoAlDia + '\'' +
                ", antiguedadMeses=" + antiguedadMeses +
                ", tipoPlan='" + tipoPlan + '\'' +
                '}';
    }
}
