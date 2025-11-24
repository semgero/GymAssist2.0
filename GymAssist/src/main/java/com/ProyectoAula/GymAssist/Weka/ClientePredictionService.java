package com.ProyectoAula.GymAssist.Weka;

import org.springframework.stereotype.Service;
import weka.classifiers.Classifier;
import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import java.io.File;

@Service
public class ClientePredictionService {

    private static final String MODEL_PATH = "/modeloEntrenado.model";
    private static final String DATASET_PATH = "/dataset.arff";

    public String predecirRenovacion(ClienteFeaturesDTO features) {
        try {
            // Cargar dataset base para estructura de atributos desde el classpath
            java.io.InputStream datasetStream = getClass().getResourceAsStream(DATASET_PATH);
            if (datasetStream == null) {
                throw new RuntimeException("No se encontró el archivo de dataset: " + DATASET_PATH);
            }
            DataSource source = new DataSource(datasetStream);
            Instances dataset = source.getDataSet();
            dataset.setClassIndex(dataset.numAttributes() - 1);

            // Crear instancia vacía con los mismos atributos
            double[] vals = new double[dataset.numAttributes()];

            // Asignar features (orden según tu dataset)
            vals[0] = features.getAsistencias4Semanas();
            vals[1] = features.getPagoAlDia().equals("Si") ? 1.0 : 0.0;
            vals[2] = features.getAntiguedadMeses();

            // tipoPlan: Mensual / Trimestral
            vals[3] = dataset.attribute(3).indexOfValue(features.getTipoPlan());

            DenseInstance instance = new DenseInstance(1.0, vals);
            instance.setDataset(dataset);

            // Cargar modelo entrenado desde el classpath
            java.io.InputStream modelStream = getClass().getResourceAsStream(MODEL_PATH);
            if (modelStream == null) {
                throw new RuntimeException("No se encontró el archivo del modelo: " + MODEL_PATH);
            }
            Classifier model = (Classifier) weka.core.SerializationHelper.read(modelStream);

            // Obtener predicción
            double resultado = model.classifyInstance(instance);
            String clasePredicha = dataset.classAttribute().value((int) resultado);

            return clasePredicha;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error en la prediccion: " + e.getMessage();
        }
    }
}
