package com.example.anna.sensorswithfrags;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;


import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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


public class GyroFragment extends Fragment implements SensorEventListener {
    private TextView resultsTextView;
    private Button registrationManageButton;

    private SensorManager sensorManager;
    private Sensor gyroscope;
    private boolean isRecording;
    private PowerManager.WakeLock mWakeLock;
    private XYSeries xSeries, ySeries, zSeries;
    private GraphicalView chartView;
    int startTime;
    private LinearLayout chartLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PowerManager powerManager = (PowerManager) this.getActivity().getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "app:tag");
        sensorManager = (SensorManager) this.getActivity().getSystemService(Context.SENSOR_SERVICE);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);





    }
    private void checkSensorsAndMemory() {
        PackageManager packageManager = this.getActivity().getPackageManager();
        boolean hasgyroscope= packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_GYROSCOPE);

        if (!hasgyroscope) {
            Toast.makeText(this.getActivity(),"device has not gyroscope",Toast.LENGTH_LONG).show();
        }

    }


    @Override
    public View onCreateView( LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.results_fragment, container, false);

        resultsTextView = v.findViewById(R.id.results);
        chartLayout = v.findViewById(R.id.chart);
        checkSensorsAndMemory();
        registrationManageButton = v.findViewById(R.id.registration_button);
        registrationManageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRecording =! isRecording;
                if (!isRecording){
                    mWakeLock.release();
                    registrationManageButton.setText(R.string.start);
                }
                else {
                    mWakeLock.acquire();
                    registrationManageButton.setText(R.string.stop);
                }
            }
        });
        chartInit();
        return v;
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (getActivity()==null){
            return;
        }
        if (isRecording) {
            if (startTime == 0){
                startTime = (int) (sensorEvent.timestamp/1000000000);
                Log.i("AcceleroFragment",String.valueOf(startTime));

            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                float ax = sensorEvent.values[0];
                float ay = sensorEvent.values[1];
                float az = sensorEvent.values[2];
                int time = (int) (sensorEvent.timestamp / 1000000000);
                xSeries.add(time, ax);
                ySeries.add(time, ay);
                zSeries.add(time, az);
                this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chartView.repaint();

                    }
                });


                resultsTextView.setText(String.format("x: %s y: %s z: %s", ax,ay,az));
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
