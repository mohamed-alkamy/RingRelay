package com.example.stepcounterapp;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private SensorEventListener stepListener;
    private TextView stepCountText;
    private int stepCount = 0;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        stepCountText = findViewById(R.id.stepCountText);

        // Load previous step count
        sharedPreferences = getSharedPreferences("StepPrefs", MODE_PRIVATE);
        stepCount = sharedPreferences.getInt("step_count", 0);
        updateStepUI();

        // Initialize SensorManager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Check if Step Detector Sensor is available
        if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) != null) {
            stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        } else {
            Toast.makeText(this, "Step Detector sensor not available!", Toast.LENGTH_LONG).show();
            return;
        }

        // Request runtime permission for activity recognition (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.ACTIVITY_RECOGNITION }, 1);
        } else {
            startStepTracking();
        }
    }

    private void startStepTracking() {
        // Define the step listener
        stepListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event != null) {
                    incrementStep();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Not needed for this sensor
            }
        };

        // Register the listener
        sensorManager.registerListener(stepListener, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI);
    }

    private void incrementStep() {
        stepCount++;
        updateStepUI();

        // Save updated step count
        sharedPreferences.edit().putInt("step_count", stepCount).apply();
    }

    private void updateStepUI() {
        stepCountText.setText("Steps: " + stepCount);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the listener to save battery
        if (stepListener != null) {
            sensorManager.unregisterListener(stepListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startStepTracking();
        } else {
            Toast.makeText(this, "Permission denied. Cannot track steps.", Toast.LENGTH_LONG).show();
        }
    }
}
