package com.example.anna.sensorswithfrags;


        import java.util.ArrayList;


public class Podometer {

    public int stepCount(ArrayList<Double> xValues, ArrayList<Double> yValues, ArrayList<Double> zValues) {
        int numberOfSteps = 0;
        double mean = 0;

        double[] vectorLength = new double[yValues.size()];

        for (int i = 0; i < yValues.size(); i++) {

            double length = Math.sqrt(xValues.get(i) * xValues.get(i) + yValues.get(i) * yValues.get(i) + zValues.get(i) * zValues.get(i));

            vectorLength[i] = length;


            for (double d : vectorLength) {
                if (d > 11) {
                    numberOfSteps++;
                }
            }
        }
        return numberOfSteps;
    }

}