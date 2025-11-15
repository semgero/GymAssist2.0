package com.ProyectoAula.GymAssist.Weka;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import org.apache.velocity.tools.config.Data;

import weka.classifiers.trees.J48;

public class EntrenadorWeka {
    public static void main(String[] args) {
        try {
            // Ruta al archivo ARFF
            String ruta = "C:/Users/pes20/Downloads/ProyectoAula/gymResultado.arff";
            
            // Cargar los datos
            DataSource source = new DataSource(ruta);
            Instances data = source.getDataSet();

            // Indicar cual es la columna que queremos predecir (ultima columna)
            if (data.classIndex() == -1) {
                data.setClassIndex(data.numAttributes() - 1);
            }

            // Crear y entrenar el clasificador J48
            J48 tree = new J48();
            tree.buildClassifier(data);

            // Mostrar el árbol y los resultados
            System.out.println("\n=== Resultados del Clasificador J48 ===\n" + tree.toString());
        
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
