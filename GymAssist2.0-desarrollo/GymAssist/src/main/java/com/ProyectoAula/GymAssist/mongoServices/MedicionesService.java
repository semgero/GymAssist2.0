package com.ProyectoAula.GymAssist.mongoServices;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.ProyectoAula.GymAssist.mongoModels.MedicionesEntity;
import com.ProyectoAula.GymAssist.mongoRepository.MedicionesRepository;

@Service
public class MedicionesService {
    
    @Autowired
    private MedicionesRepository medicionesRepository;

    public MedicionesEntity guardarMedicion(Double peso, Double estatura, ObjectId clienteId) {
        MedicionesEntity medicion = new MedicionesEntity();
        medicion.setPeso(peso);
        medicion.setEstatura(estatura);
        medicion.setClienteId(clienteId); // Asegúrate de que clienteId en la entidad sea tipo Long
        medicion.setFechaRegistro(LocalDateTime.now());
        return medicionesRepository.save(medicion);
    }

    public double calcularIMC(Double peso, Double estatura) {
        return peso / (estatura * estatura);
    }

    public String interpretarIMC(double imc) {
        if (imc < 18.5) return "Peso muy bajo";
        else if (imc < 24.9) return "Peso normal";
        else if (imc < 29.9) return "Sobrepeso";
        else return "Obesidad";
    }

    public Optional<MedicionesEntity> obtenerUltimaMedicion(ObjectId clienteId) {
        return medicionesRepository.findTopByClienteIdOrderByFechaRegistroDesc(clienteId);
    }

    public List<MedicionesEntity> obtenerHistorial(ObjectId clienteId) {
    return medicionesRepository.findByClienteIdOrderByFechaRegistroDesc(clienteId);
}
}
