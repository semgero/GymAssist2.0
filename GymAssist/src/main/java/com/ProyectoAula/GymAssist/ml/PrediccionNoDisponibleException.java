package com.ProyectoAula.GymAssist.ml;

/**
 * Se lanza cuando la API de predicción en Python (FastAPI + TensorFlow) no responde.
 */
public class PrediccionNoDisponibleException extends RuntimeException {

    public PrediccionNoDisponibleException(String url, Throwable causa) {
        super("La API de predicción (Python + TensorFlow) no está disponible en " + url
                + ". Enciéndela con: uvicorn app.main:app --port 8000 (dentro de la carpeta prediccion-ml)", causa);
    }
}
