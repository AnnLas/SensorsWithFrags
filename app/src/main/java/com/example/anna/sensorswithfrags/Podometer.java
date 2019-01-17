package com.example.anna.sensorswithfrags;


        import org.achartengine.model.XYSeries;


public class Podometer {

    public int stepCount(XYSeries xValues, XYSeries yValues, XYSeries zValues) {
        int numberOfSteps = 0;
        double mean = 0;

        double[] vectorLength = new double[yValues.getItemCount()];

        for (int i = 0; i < yValues.getItemCount(); i++) {

            double length = Math.sqrt(xValues.getY(i) * xValues.getY(i) + yValues.getY(i) * yValues.getY(i) + zValues.getY(i) * zValues.getY(i));

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