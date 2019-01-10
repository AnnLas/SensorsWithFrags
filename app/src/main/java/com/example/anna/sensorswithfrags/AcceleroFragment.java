package com.example.anna.sensorswithfrags;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;


import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;



public class AcceleroFragment extends Fragment implements SensorEventListener {
    private TextView resultsTextView;
    private Button registrationManageButton;
    private ImageView saveButton;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isRecording;
    private PowerManager.WakeLock mWakeLock;
    private XYSeries xSeries, ySeries, zSeries;
    private GraphicalView chartView;
    private int startTime;
    private TextView stepsTextView;
    private LinearLayout chartLayout;
    private ArrayList <Double> yResults;
    private ArrayList <Double> zResults;
    private ArrayList <Double> xResults;
    private static String TAG = "AcceleroFragment";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PowerManager powerManager = (PowerManager) this.getActivity().getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "app:tag");
        sensorManager = (SensorManager) this.getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        yResults = new ArrayList<>();
        zResults = new ArrayList<>();
        xResults = new ArrayList<>();

    }

    private void checkSensorsAndMemory() {
        PackageManager packageManager = this.getActivity().getPackageManager();
        boolean hasAcceleroemeter = packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);
        boolean hasExternalMemory = Boolean.getBoolean(Environment.MEDIA_MOUNTED);

        if (!hasAcceleroemeter) {
            Toast.makeText(this.getActivity(), "device has not accelerometer", Toast.LENGTH_LONG).show();
        }
        if (!hasExternalMemory) {
            Toast.makeText(this.getActivity(), "device has not external memory", Toast.LENGTH_LONG).show();
        }
        if (hasExternalMemory) {
            Toast.makeText(this.getActivity(), "device has external memory", Toast.LENGTH_LONG).show();
        }

    }

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.results_fragment, container, false);
        stepsTextView = v.findViewById(R.id.steps_number);
        resultsTextView = v.findViewById(R.id.results);
        chartLayout = v.findViewById(R.id.chart);
        checkSensorsAndMemory();
        registrationManageButton = v.findViewById(R.id.registration_button);
        saveButton = v.findViewById(R.id.save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveResults();
            }
        });
        registrationManageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRecording = !isRecording;
                if (!isRecording) {
                    registrationManageButton.setText(R.string.start);
                    mWakeLock.release();
                } else {
                    mWakeLock.acquire();
                    registrationManageButton.setText(R.string.stop);
                }
            }
        });
        chartInit();
        return v;
    }

    private void saveResults() {
        File root = android.os.Environment.getExternalStorageDirectory();
        File dir = new File (root.getAbsolutePath() + "/download");
        dir.mkdirs();
        File file = new File(dir, "results.txt");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            PrintWriter printWriter = new PrintWriter(fileOutputStream);
            for (int i=0; i<xSeries.getXYMap().size();i++){
                printWriter.print(xSeries.getX(i)+'\t');
                printWriter.print(xSeries.getY(i)+'\t');
                printWriter.print(ySeries.getY(i)+'\t');
                printWriter.print(zSeries.getY(i)+'\n');
                printWriter.println();

            }
            printWriter.flush();
            printWriter.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "******* File not found. Did you" +
                    " add a WRITE_EXTERNAL_STORAGE permission to the   manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (isRecording) {
            if (startTime == 0) {
                startTime = (int) (sensorEvent.timestamp / 1000000000);
                Log.i("AcceleroFragment", String.valueOf(startTime));

            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float ax = sensorEvent.values[0];
                float ay = sensorEvent.values[1];
                float az = sensorEvent.values[2];
                int time = (int) (sensorEvent.timestamp / 1000000000) - startTime;

                xSeries.add(time, ax);
                ySeries.add(time, ay);
                zSeries.add(time, az);
                yResults.add((double)ay);
                zResults.add((double)az);
                xResults.add((double)ax);

                Podometer podometer = new Podometer();
                stepsTextView.setText("steps: "+String.valueOf(podometer.stepCount(xResults,yResults, zResults)));

                Log.i(TAG,String.valueOf("steps  "+podometer.stepCount(xResults, yResults, zResults)));

                this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chartView.repaint();

                    }
                });


                resultsTextView.setText(String.format("x: %s y: %s z: %s", ax, ay, az));
                System.out.println(sensorEvent.timestamp);

            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    private void chartInit() {
        xSeries = new XYSeries("x");
        ySeries = new XYSeries("y");
        zSeries = new XYSeries("z");

        XYSeriesRenderer xRenderer = new XYSeriesRenderer();
        xRenderer.setLineWidth(2);
        xRenderer.setColor(Color.BLUE);
        xRenderer.setPointStyle(PointStyle.CIRCLE);

        XYSeriesRenderer yRenderer = new XYSeriesRenderer();
        yRenderer.setLineWidth(2);
        yRenderer.setColor(Color.RED);
        yRenderer.setPointStyle(PointStyle.CIRCLE);

        XYSeriesRenderer zRenderer = new XYSeriesRenderer();
        zRenderer.setLineWidth(2);
        zRenderer.setColor(Color.GREEN);
        zRenderer.setPointStyle(PointStyle.CIRCLE);


        XYMultipleSeriesRenderer multipleSeriesRenderer = new XYMultipleSeriesRenderer();
        multipleSeriesRenderer.addSeriesRenderer(xRenderer);
        multipleSeriesRenderer.addSeriesRenderer(yRenderer);
        multipleSeriesRenderer.addSeriesRenderer(zRenderer);
        multipleSeriesRenderer.setYAxisMax(10);
        multipleSeriesRenderer.setYAxisMin(-10);
        multipleSeriesRenderer.setShowGrid(true);
        multipleSeriesRenderer.setBackgroundColor(Color.DKGRAY);

        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        dataset.addSeries(xSeries);
        dataset.addSeries(ySeries);
        dataset.addSeries(zSeries);


        chartView = ChartFactory.getLineChartView(this.getActivity(), dataset, multipleSeriesRenderer);
        chartLayout.addView(chartView);

    }
}
