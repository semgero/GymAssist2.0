package com.ProyectoAula.GymAssist.Weka;

import weka.core.Instances;
import weka.core.DenseInstance;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.J48;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

public class EntrenarEvaluarPredecir {

    // 1) Entrena un modelo J48 a partir de un archivo ARFF
    public static Classifier entrenar(String rutaArff) throws Exception {
        DataSource source = new DataSource(rutaArff);
        Instances data = source.getDataSet();
        if (data.classIndex() == -1)
            data.setClassIndex(data.numAttributes() - 1);

        J48 tree = new J48();
        tree.buildClassifier(data);
        return tree;
    }

    // 2) Evalúa el modelo usando validación cruzada
    public static Evaluation evaluarConCrossValidation(Classifier cls, Instances data, int folds) throws Exception {
        if (data.classIndex() == -1)
            data.setClassIndex(data.numAttributes() - 1);

        Evaluation eval = new Evaluation(data);
        eval.crossValidateModel(cls, data, folds, new Random(1));
        return eval;
    }

    // 3) Guarda el modelo en disco
    public static void guardarModelo(Classifier cls, String rutaModelo) throws Exception {
        weka.core.SerializationHelper.write(rutaModelo, cls);
    }

    // 4) Carga el modelo desde disco
    public static Classifier cargarModelo(String rutaModelo) throws Exception {
        return (Classifier) weka.core.SerializationHelper.read(rutaModelo);
    }

    // 5) Predice una instancia nueva a partir de valores dados
    public static double predecirUnRegistro(Classifier model, Instances headerData, double[] valores) throws Exception {
        if (headerData.classIndex() == -1)
            headerData.setClassIndex(headerData.numAttributes() - 1);

        DenseInstance nuevaInstancia = new DenseInstance(headerData.numAttributes());
        for (int i = 0; i < headerData.numAttributes(); i++) {
            if (i == headerData.classIndex()) {
                nuevaInstancia.setMissing(i); // la clase se predice
            } else {
                nuevaInstancia.setValue(i, valores[i]);
            }
        }

        nuevaInstancia.setDataset(headerData);
        return model.classifyInstance(nuevaInstancia);
    }

    // 6) Método principal: entrena, evalúa, guarda, carga y predice
    public static void main(String[] args) {
        try {
            String rutaArff = "C:/Users/pes20/Downloads/ProyectoAula/gymResultado.arff";
            String rutaModelo = "C:/Users/pes20/Downloads/ProyectoAula/modeloJ48.model";

            // ENTRENAR
            System.out.println("Entrenando...");
            Classifier modelo = entrenar(rutaArff);

            // CARGAR DATOS
            DataSource source = new DataSource(rutaArff);
            Instances data = source.getDataSet();
            if (data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);

            // EVALUAR
            System.out.println("Evaluando con 5-fold cross validation...");
            Evaluation eval = evaluarConCrossValidation(modelo, data, 5);
            System.out.println(eval.toSummaryString("\n=== Resumen de evaluación ===\n", false));
            System.out.println(eval.toClassDetailsString("\n=== Detalle por clase ===\n"));
            System.out.println(eval.toMatrixString("\n=== Matriz de confusión ===\n"));

            // GUARDAR Y CARGAR MODELO
            guardarModelo(modelo, rutaModelo);
            System.out.println("Modelo guardado en: " + rutaModelo);
            Classifier modeloCargado = cargarModelo(rutaModelo);
            System.out.println("Modelo cargado OK");

            // CREAR NUEVO REGISTRO SEGÚN TU ARFF
            // Orden: [asistencias_4_sem, pago_al_dia, antiguedad_meses, tipo_plan, clase]
            double[] valoresEjemplo = new double[data.numAttributes()];

            valoresEjemplo[0] = 12.0; // asistencias_4_sem
            valoresEjemplo[1] = data.attribute(1).indexOfValue("Si"); // pago_al_dia = "Si"
            valoresEjemplo[2] = 8.0; // antiguedad_meses
            valoresEjemplo[3] = data.attribute(3).indexOfValue("Mensual"); // tipo_plan = "Mensual"
            valoresEjemplo[data.classIndex()] = Double.NaN; // clase -> se predice

            // PREDICCIÓN
            double predIndex = predecirUnRegistro(modeloCargado, data, valoresEjemplo);
            String predClase = data.classAttribute().value((int) predIndex);

            System.out.println("Predicción para el ejemplo: " + predClase);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
