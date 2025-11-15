package com.ProyectoAula.GymAssist.Weka;

import weka.classifiers.trees.J48;

public class TestWeka {
    
    public static void main(String[] args) {
        J48 tree = new J48();
        System.out.println(tree.getClass().getName() + " Created successfully!");
    }
}
